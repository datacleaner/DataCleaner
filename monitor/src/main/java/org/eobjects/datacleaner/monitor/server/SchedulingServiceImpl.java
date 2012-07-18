/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.datacleaner.monitor.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eobjects.datacleaner.monitor.configuration.JobContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.scheduling.AbstractQuartzJob;
import org.eobjects.datacleaner.monitor.scheduling.ExecuteJob;
import org.eobjects.datacleaner.monitor.scheduling.ExecuteJobListener;
import org.eobjects.datacleaner.monitor.scheduling.SchedulingService;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionIdentifier;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.scheduling.model.TriggerType;
import org.eobjects.datacleaner.monitor.server.jaxb.JaxbExecutionLogReader;
import org.eobjects.datacleaner.monitor.server.jaxb.JaxbScheduleReader;
import org.eobjects.datacleaner.monitor.server.jaxb.JaxbScheduleWriter;
import org.eobjects.datacleaner.monitor.server.jaxb.SaxExecutionIdentifierReader;
import org.eobjects.datacleaner.monitor.shared.model.DCSecurityException;
import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.metamodel.util.Action;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.FileHelper;
import org.eobjects.metamodel.util.Func;
import org.quartz.CronExpression;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Main implementation of the {@link SchedulingService} interface.
 */
@Component("schedulingService")
public class SchedulingServiceImpl implements SchedulingService, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(SchedulingServiceImpl.class);

    public static final String EXTENSION_SCHEDULE_XML = ".schedule.xml";

    private final Repository _repository;
    private final TenantContextFactory _tenantContextFactory;
    private final Scheduler _scheduler;

    private ApplicationContext _applicationContext;

    @Autowired
    public SchedulingServiceImpl(Repository repository, TenantContextFactory tenantContextFactory) {
        _repository = repository;
        _tenantContextFactory = tenantContextFactory;
        _scheduler = createScheduler();
    }

    private Scheduler createScheduler() {
        try {
            StdSchedulerFactory factory = new StdSchedulerFactory();
            Scheduler scheduler = factory.getScheduler();
            return scheduler;
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalStateException("Failed to create scheduler", e);
        }
    }

    public Scheduler getScheduler() {
        return _scheduler;
    }

    public void initialize() {
        final List<RepositoryFolder> tenantFolders = _repository.getFolders();
        for (RepositoryFolder tenantFolder : tenantFolders) {
            final TenantIdentifier tenant = new TenantIdentifier(tenantFolder.getName());
            final List<ScheduleDefinition> schedules = getSchedules(tenant);

            logger.info("Initializing {} schedules for tenant {}", schedules.size(), tenant.getId());

            for (ScheduleDefinition schedule : schedules) {
                initializeSchedule(schedule);
            }
        }

        try {
            _scheduler.start();
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to start scheduler", e);
        }

        logger.info("Schedule initialization done!");
    }

    @Override
    public List<ScheduleDefinition> getSchedules(TenantIdentifier tenant) {
        final TenantContext context = _tenantContextFactory.getContext(tenant);

        final List<String> jobNames = context.getJobNames();
        final List<ScheduleDefinition> schedules = new ArrayList<ScheduleDefinition>(jobNames.size());
        for (String jobName : jobNames) {
            schedules.add(getSchedule(tenant, jobName));
        }
        return schedules;
    }

    private ScheduleDefinition getSchedule(TenantIdentifier tenant, String jobName) {
        final TenantContext context = _tenantContextFactory.getContext(tenant);

        final JobIdentifier jobIdentifier = new JobIdentifier(jobName);
        final JobContext jobContext = context.getJob(jobIdentifier);
        final String datastoreName = jobContext.getSourceDatastoreName();
        final DatastoreIdentifier datastoreIdentifier = new DatastoreIdentifier(datastoreName);

        final RepositoryFolder jobsFolder = context.getJobFolder();

        final RepositoryFile scheduleFile = jobsFolder.getFile(jobName + EXTENSION_SCHEDULE_XML);
        final JaxbScheduleReader reader = new JaxbScheduleReader();

        final ScheduleDefinition schedule;

        if (scheduleFile == null) {
            schedule = new ScheduleDefinition(tenant, jobIdentifier, datastoreIdentifier);
        } else {
            final InputStream inputStream = scheduleFile.readFile();
            try {
                schedule = reader.read(inputStream, jobIdentifier, tenant, datastoreIdentifier);
            } finally {
                FileHelper.safeClose(inputStream);
            }
        }

        return schedule;
    }

    @Override
    public ScheduleDefinition updateSchedule(final TenantIdentifier tenant, final ScheduleDefinition scheduleDefinition) {

        initializeSchedule(scheduleDefinition);

        final String jobName = scheduleDefinition.getJob().getName();

        final TenantContext context = _tenantContextFactory.getContext(tenant);

        final RepositoryFolder jobsFolder = context.getJobFolder();
        final String filename = jobName + EXTENSION_SCHEDULE_XML;
        final RepositoryFile file = jobsFolder.getFile(filename);

        final Action<OutputStream> writeAction = new Action<OutputStream>() {
            @Override
            public void run(OutputStream out) throws Exception {
                JaxbScheduleWriter writer = new JaxbScheduleWriter();
                writer.write(scheduleDefinition, out);
            }
        };

        if (file == null) {
            jobsFolder.createFile(filename, writeAction);
        } else {
            file.writeFile(writeAction);
        }

        return scheduleDefinition;
    }

    private void initializeSchedule(final ScheduleDefinition schedule) {
        final String tenantId = schedule.getTenant().getId();
        final String jobName = schedule.getJob().getName();
        final String jobListenerName = tenantId + "." + jobName;

        try {
            _scheduler.deleteJob(jobName, tenantId);
            _scheduler.removeJobListener(jobListenerName);

            final TriggerType triggerType = schedule.getTriggerType();
            if (triggerType == TriggerType.MANUAL) {
                logger.info("Not scheduling job: {} (manual trigger type)", jobName);
            } else {
                final JobDetail jobDetail = new JobDetail(jobName, tenantId, ExecuteJob.class);
                JobDataMap jobDataMap = jobDetail.getJobDataMap();
                jobDataMap.put(AbstractQuartzJob.APPLICATION_CONTEXT, _applicationContext);
                jobDataMap.put(ExecuteJob.DETAIL_SCHEDULE_DEFINITION, schedule);

                if (triggerType == TriggerType.PERIODIC) {
                    // time based trigger

                    final String scheduleExpression = schedule.getCronExpression();
                    final CronExpression cronExpression = toCronExpression(scheduleExpression);

                    final CronTriggerBean trigger = new CronTriggerBean();
                    trigger.setGroup(tenantId);
                    trigger.setName(jobName);
                    trigger.setStartTime(new Date());
                    trigger.setCronExpression(cronExpression);
                    trigger.setJobDetail(jobDetail);

                    trigger.afterPropertiesSet();

                    logger.info("Adding trigger to scheduler: {} | {}", jobName, cronExpression);
                    _scheduler.scheduleJob(jobDetail, trigger);
                } else {
                    // event based trigger (via a job listener)
                    jobDetail.setDurability(true);
                    _scheduler.addJob(jobDetail, true);

                    final ExecuteJobListener listener = new ExecuteJobListener(jobListenerName, schedule);
                    _scheduler.addGlobalJobListener(listener);
                    logger.info("Adding listener to scheduler: {}", jobListenerName);
                }
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalStateException("Could not configure job scheduling", e);
        }
    }

    protected static CronExpression toCronExpression(String scheduleExpression) {
        scheduleExpression = scheduleExpression.trim();

        final CronExpression cronExpression;

        try {
            if ("@yearly".equals(scheduleExpression) || "@annually".equals(scheduleExpression)) {
                cronExpression = new CronExpression("0 0 0 1 1 ? *");
            } else if ("@monthly".equals(scheduleExpression)) {
                cronExpression = new CronExpression("0 0 0 1 * ?");
            } else if ("@weekly".equals(scheduleExpression)) {
                cronExpression = new CronExpression("0 0 * ? * 1");
            } else if ("@daily".equals(scheduleExpression)) {
                cronExpression = new CronExpression("0 0 0 * * ?");
            } else if ("@hourly".equals(scheduleExpression)) {
                cronExpression = new CronExpression("0 0 * * * ?");
            } else if ("@minutely".equals(scheduleExpression) || "@every_minute".equals(scheduleExpression)) {
                cronExpression = new CronExpression("0 * * * * ?");
            } else {
                cronExpression = new CronExpression(scheduleExpression);
            }
        } catch (ParseException e) {
            throw new IllegalStateException("Failed to parse cron expression: " + scheduleExpression, e);
        }

        if (logger.isInfoEnabled()) {
            logger.info("Cron expression summary ({}): {}", scheduleExpression, cronExpression.getExpressionSummary()
                    .replaceAll("\n", ", "));
        }

        return cronExpression;
    }

    @Override
    public ExecutionLog triggerExecution(TenantIdentifier tenant, JobIdentifier job) {
        final TenantContext context = _tenantContextFactory.getContext(tenant);

        final ScheduleDefinition schedule = getSchedule(tenant, job.getName());

        final ExecutionLog execution = new ExecutionLog(schedule, TriggerType.MANUAL);

        // set the "triggered by" attribute.
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            final String username = authentication.getName();
            execution.setTriggeredBy(username);
        }

        // TODO: Consider using scheduler.triggerJob(...) ... That will also
        // trigger listeners.
        ExecuteJob.executeJob(context, execution);

        return execution;
    }

    @Override
    public ExecutionLog getLatestExecution(TenantIdentifier tenant, JobIdentifier job) {
        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        final RepositoryFolder resultFolder = tenantContext.getResultFolder();

        final RepositoryFile latestFile = resultFolder.getLatestFile(job.getName(),
                FileFilters.ANALYSIS_EXECUTION_LOG_XML.getExtension());

        if (latestFile == null) {
            // no execution available
            return null;
        }

        return readExecutionLogFile(latestFile, tenant);
    }

    @Override
    public List<ExecutionIdentifier> getAllExecutions(TenantIdentifier tenant, JobIdentifier job) {
        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        final RepositoryFolder resultFolder = tenantContext.getResultFolder();
        final List<RepositoryFile> files = resultFolder.getFiles(job.getName(),
                FileFilters.ANALYSIS_EXECUTION_LOG_XML.getExtension());

        final List<ExecutionIdentifier> executionIdentifiers = CollectionUtils.map(files,
                new Func<RepositoryFile, ExecutionIdentifier>() {
                    @Override
                    public ExecutionIdentifier eval(RepositoryFile file) {
                        InputStream in = file.readFile();
                        try {
                            return SaxExecutionIdentifierReader.read(in);
                        } finally {
                            FileHelper.safeClose(in);
                        }
                    }
                });

        return executionIdentifiers;
    }

    @Override
    public ExecutionLog getExecution(TenantIdentifier tenant, ExecutionIdentifier executionIdentifier)
            throws DCSecurityException {
        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        final RepositoryFolder resultFolder = tenantContext.getResultFolder();

        final String resultId = executionIdentifier.getResultId();
        final RepositoryFile file = resultFolder.getFile(resultId
                + FileFilters.ANALYSIS_EXECUTION_LOG_XML.getExtension());
        if (file == null) {
            throw new IllegalArgumentException("No execution with result id: " + resultId);
        }

        return readExecutionLogFile(file, tenant);
    }

    private ExecutionLog readExecutionLogFile(final RepositoryFile file, final TenantIdentifier tenant) {
        final JaxbExecutionLogReader reader = new JaxbExecutionLogReader();
        final InputStream in = file.readFile();
        try {
            return reader.read(in, tenant);
        } finally {
            FileHelper.safeClose(in);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        _applicationContext = applicationContext;
    }

    @Override
    public List<JobIdentifier> getDependentJobCandidates(TenantIdentifier tenant, ScheduleDefinition schedule)
            throws DCSecurityException {
        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        final List<String> jobNames = tenantContext.getJobNames();
        final List<JobIdentifier> result = new ArrayList<JobIdentifier>();
        for (String jobName : jobNames) {
            if (!jobName.equals(schedule.getJob().getName())) {
                result.add(new JobIdentifier(jobName));
            }
        }
        return result;
    }

}
