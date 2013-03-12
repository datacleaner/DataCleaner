/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eobjects.datacleaner.monitor.configuration.JobContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.scheduling.SchedulingService;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionIdentifier;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionStatus;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.scheduling.model.TriggerType;
import org.eobjects.datacleaner.monitor.scheduling.quartz.AbstractQuartzJob;
import org.eobjects.datacleaner.monitor.scheduling.quartz.ExecuteJob;
import org.eobjects.datacleaner.monitor.scheduling.quartz.ExecuteJobListener;
import org.eobjects.datacleaner.monitor.server.jaxb.JaxbException;
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
import org.eobjects.metamodel.util.Func;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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

    /**
     * Alternative constructor, mainly for testing purposes. Will create a local
     * in-memory scheduler.
     * 
     * @param repository
     * @param tenantContextFactory
     */
    public SchedulingServiceImpl(Repository repository, TenantContextFactory tenantContextFactory) {
        this(repository, tenantContextFactory, createScheduler());
    }

    private static Scheduler createScheduler() {
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

    /**
     * Default constructor.
     * 
     * @param repository
     * @param tenantContextFactory
     * @param scheduler
     */
    @Autowired
    public SchedulingServiceImpl(Repository repository, TenantContextFactory tenantContextFactory, Scheduler scheduler) {
        if (repository == null) {
            throw new IllegalArgumentException("Repository cannot be null");
        }
        if (tenantContextFactory == null) {
            throw new IllegalArgumentException("TenantContextFactory cannot be null");
        }
        if (scheduler == null) {
            throw new IllegalArgumentException("Quartz scheduler cannot be null");
        }
        _repository = repository;
        _tenantContextFactory = tenantContextFactory;
        _scheduler = scheduler;
    }

    public Scheduler getScheduler() {
        return _scheduler;
    }

    @PostConstruct
    public void initialize() {
        final List<RepositoryFolder> tenantFolders = _repository.getFolders();
        for (RepositoryFolder tenantFolder : tenantFolders) {
            final TenantIdentifier tenant = new TenantIdentifier(tenantFolder.getName());
            final String tenantId = tenant.getId();

            final List<ScheduleDefinition> schedules = getSchedules(tenant);
            logger.info("Initializing {} schedules for tenant {}", schedules.size(), tenantId);

            for (ScheduleDefinition schedule : schedules) {
                initializeSchedule(schedule);
            }
        }

        try {
            if (!_scheduler.isStarted()) {
                _scheduler.start();
            }
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to start scheduler", e);
        }

        logTriggers();

        logger.info("Schedule initialization done!");
    }

    private void logTriggers() {
        final List<RepositoryFolder> tenantFolders = _repository.getFolders();
        for (RepositoryFolder tenantFolder : tenantFolders) {
            final String tenantId = tenantFolder.getName();
            try {
                final Set<TriggerKey> triggerKeys = _scheduler
                        .getTriggerKeys(GroupMatcher.triggerGroupEquals(tenantId));
                if (triggerKeys == null || triggerKeys.isEmpty()) {
                    logger.info("No triggers initialized for tenant: {}", tenantId);
                } else {
                    for (TriggerKey triggerKey : triggerKeys) {
                        logger.info("Trigger of tenant {}: {}", tenantId, triggerKey);
                    }
                }
            } catch (SchedulerException e) {
                logger.warn("Failed to get triggers of tenant: " + tenantId, e);
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            _scheduler.shutdown();
        } catch (SchedulerException e) {
            logger.error("Failed to shutdown scheduler: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ScheduleDefinition> getSchedules(TenantIdentifier tenant) {
        final TenantContext context = _tenantContextFactory.getContext(tenant);

        final List<String> jobNames = context.getJobNames();
        final List<ScheduleDefinition> schedules = new ArrayList<ScheduleDefinition>(jobNames.size());
        for (String jobName : jobNames) {
            try {
                ScheduleDefinition schedule = getSchedule(tenant, jobName);
                schedules.add(schedule);
            } catch (Exception e) {
                logger.error("Failed to initialize schedule for tenant '" + tenant.getId() + "' job '" + jobName + "'.",
                        e);
            }
        }
        return schedules;
    }

    private ScheduleDefinition getSchedule(final TenantIdentifier tenant, final String jobName) {
        final TenantContext context = _tenantContextFactory.getContext(tenant);

        final JobContext jobContext = context.getJob(jobName);
        final String datastoreName = jobContext.getSourceDatastoreName();
        final DatastoreIdentifier datastoreIdentifier = new DatastoreIdentifier(datastoreName);

        final RepositoryFolder jobsFolder = context.getJobFolder();

        final RepositoryFile scheduleFile = jobsFolder.getFile(jobName + EXTENSION_SCHEDULE_XML);
        final JaxbScheduleReader reader = new JaxbScheduleReader();

        final ScheduleDefinition schedule;

        final JobIdentifier jobIdentifier = new JobIdentifier(jobName);
        if (scheduleFile == null) {
            schedule = new ScheduleDefinition(tenant, jobIdentifier, datastoreIdentifier);
        } else {
            schedule = scheduleFile.readFile(new Func<InputStream, ScheduleDefinition>() {
                @Override
                public ScheduleDefinition eval(InputStream inputStream) {
                    return reader.read(inputStream, jobIdentifier, tenant, datastoreIdentifier);
                }
            });
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
        final JobIdentifier job = schedule.getJob();

        removeSchedule(schedule.getTenant(), job);

        final String tenantId = schedule.getTenant().getId();
        final String jobName = job.getName();
        final String jobListenerName = tenantId + "." + jobName;

        try {

            final TriggerType triggerType = schedule.getTriggerType();
            if (triggerType == TriggerType.MANUAL) {
                logger.info("Not scheduling job: {} (manual trigger type)", jobName);
            } else {
                final JobDetail jobDetail = JobBuilder.newJob(ExecuteJob.class).withIdentity(jobName, tenantId)
                        .storeDurably().build();

                final JobDataMap jobDataMap = jobDetail.getJobDataMap();
                jobDataMap.put(ExecuteJob.DETAIL_SCHEDULE_DEFINITION, schedule);

                if (triggerType == TriggerType.PERIODIC) {
                    // time based trigger

                    final String scheduleExpression = schedule.getCronExpression();
                    final CronExpression cronExpression = toCronExpression(scheduleExpression);

                    final CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule(cronExpression);

                    final CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(jobName, tenantId)
                            .forJob(jobDetail).withSchedule(cronSchedule).startNow().build();

                    logger.info("Adding trigger to scheduler: {} | {}", jobName, cronExpression);
                    _scheduler.scheduleJob(jobDetail, trigger);
                } else {
                    // event based trigger (via a job listener)
                    _scheduler.addJob(jobDetail, true);
                    final ExecuteJobListener listener = new ExecuteJobListener(jobListenerName, schedule);
                    _scheduler.getListenerManager().addJobListener(listener);
                    logger.info("Adding listener to scheduler: {}", jobListenerName);
                }
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalStateException("Failed to schedule job: " + job, e);
        }
    }

    @Override
    public void removeSchedule(TenantIdentifier tenant, JobIdentifier job) throws DCSecurityException {
        logger.info("Removing schedule for job: " + job);
        final String jobName = job.getName();
        final String tenantId = tenant.getId();
        final String jobListenerName = tenantId + "." + jobName;
        try {
            _scheduler.deleteJob(new JobKey(jobName, tenantId));
            _scheduler.getListenerManager().removeJobListener(jobListenerName);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalStateException("Failed to remove job schedule: " + job, e);
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

        final String jobNameToBeTriggered = job.getName();
        final ScheduleDefinition schedule = getSchedule(tenant, jobNameToBeTriggered);
        final ExecutionLog execution = new ExecutionLog(schedule, TriggerType.MANUAL);

        try {
            boolean addJob = true;
            GroupMatcher<JobKey> matcher = GroupMatcher.jobGroupEquals(tenant.getId());
            Set<JobKey> jobKeys = _scheduler.getJobKeys(matcher);
            for (JobKey jobKey : jobKeys) {
                String jobName = jobKey.getName();
                if (jobName.equals(jobNameToBeTriggered)) {
                    addJob = false;
                    break;
                }
            }
            if (addJob) {
                final JobDetail jobDetail = JobBuilder.newJob(ExecuteJob.class)
                        .withIdentity(jobNameToBeTriggered, tenant.getId()).storeDurably().build();
                _scheduler.addJob(jobDetail, true);
            }

            // set the "triggered by" attribute.
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                final String username = authentication.getName();
                execution.setTriggeredBy(username);
            }

            final JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(ExecuteJob.DETAIL_EXECUTION_LOG, execution);

            _scheduler.triggerJob(new JobKey(jobNameToBeTriggered, tenant.getId()), jobDataMap);
        } catch (SchedulerException e) {
            throw new IllegalStateException("Unexpected error invoking scheduler", e);
        }

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

        return readExecutionLogFile(latestFile, tenant, 1);
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
                        ExecutionIdentifier result = file.readFile(new Func<InputStream, ExecutionIdentifier>() {
                            @Override
                            public ExecutionIdentifier eval(InputStream in) {
                                return SaxExecutionIdentifierReader.read(in);
                            }
                        });
                        return result;
                    }
                });

        Collections.sort(executionIdentifiers);

        return executionIdentifiers;
    }

    @Override
    public ExecutionLog getExecution(TenantIdentifier tenant, ExecutionIdentifier executionIdentifier)
            throws DCSecurityException {
        if (executionIdentifier == null) {
            return null;
        }
        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        final RepositoryFolder resultFolder = tenantContext.getResultFolder();

        final String resultId = executionIdentifier.getResultId();
        final RepositoryFile file = resultFolder.getFile(resultId
                + FileFilters.ANALYSIS_EXECUTION_LOG_XML.getExtension());
        if (file == null) {
            throw new IllegalArgumentException("No execution with result id: " + resultId);
        }

        return readExecutionLogFile(file, tenant, 3);
    }

    private ExecutionLog readExecutionLogFile(final RepositoryFile file, final TenantIdentifier tenant,
            final int retries) {
        final JaxbExecutionLogReader reader = new JaxbExecutionLogReader();

        final ExecutionLog result = file.readFile(new Func<InputStream, ExecutionLog>() {
            @Override
            public ExecutionLog eval(InputStream in) {
                try {
                    return reader.read(in, tenant);
                } catch (JaxbException e) {
                    if (retries > 0) {
                        logger.debug("Failed to read execution log in first pass. This could be because it is also being written at this time. Retrying.");
                        return null;
                    } else {
                        logger.info("Failed to read execution log, returning unknown status.");
                        final ExecutionLog executionLog = new ExecutionLog(null, null);
                        executionLog.setExecutionStatus(ExecutionStatus.UNKNOWN);
                        return executionLog;
                    }
                }
            }
        });

        if (result == null) {
            // retry
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // do nothing
            }
            return readExecutionLogFile(file, tenant, retries - 1);
        }

        return result;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        _applicationContext = applicationContext;
        try {
            SchedulerContext schedulerContext = _scheduler.getContext();
            schedulerContext.put(AbstractQuartzJob.APPLICATION_CONTEXT, _applicationContext);
        } catch (SchedulerException e) {
            logger.error(
                    "Failed to get scheduler context and set application context on it. Expect issues when invoking jobs, or set property '"
                            + AbstractQuartzJob.APPLICATION_CONTEXT + " on the scheduler's context manually'.", e);
        }
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
