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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.datacleaner.monitor.configuration.ConfigurationCache;
import org.eobjects.datacleaner.monitor.jaxb.Alert;
import org.eobjects.datacleaner.monitor.jaxb.Schedule;
import org.eobjects.datacleaner.monitor.scheduling.AbstractQuartzJob;
import org.eobjects.datacleaner.monitor.scheduling.ExecuteJob;
import org.eobjects.datacleaner.monitor.scheduling.ExecuteJobListener;
import org.eobjects.datacleaner.monitor.scheduling.SchedulingService;
import org.eobjects.datacleaner.monitor.scheduling.model.AlertDefinition;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.scheduling.model.TriggerType;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.timeline.TimelineService;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.metamodel.util.Action;
import org.eobjects.metamodel.util.FileHelper;
import org.quartz.CronExpression;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.CronTriggerBean;

/**
 * Main implementation of the {@link SchedulingService} interface.
 */
public class SchedulingServiceImpl implements SchedulingService, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(SchedulingServiceImpl.class);

    public static final String EXTENSION_SCHEDULE_XML = ".schedule.xml";

    private final TimelineService _timelineService;
    private final Repository _repository;
    private final ConfigurationCache _configurationCache;
    private final Scheduler _scheduler;

    private ApplicationContext _applicationContext;

    public SchedulingServiceImpl(TimelineService timelineService, Repository repository,
            ConfigurationCache configurationCache) {
        _timelineService = timelineService;
        _repository = repository;
        _configurationCache = configurationCache;
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

    public void initialize() {
        final List<RepositoryFolder> tenantFolders = _repository.getFolders();
        for (RepositoryFolder tenantFolder : tenantFolders) {
            final TenantIdentifier tenant = new TenantIdentifier(tenantFolder.getName());
            final List<ScheduleDefinition> schedules = getSchedules(tenant);

            logger.info("Initializing {} schedules for tenant {}", schedules.size(), tenant.getId());

            for (ScheduleDefinition schedule : schedules) {
                if (schedule.isActive()) {
                    initializeSchedule(schedule);
                }
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
        final List<JobIdentifier> jobs = _timelineService.getJobs(tenant);
        final List<ScheduleDefinition> schedules = new ArrayList<ScheduleDefinition>(jobs.size());
        for (JobIdentifier jobIdentifier : jobs) {
            schedules.add(getSchedule(tenant, jobIdentifier));
        }
        return schedules;
    }

    private ScheduleDefinition getSchedule(TenantIdentifier tenant, JobIdentifier jobIdentifier) {
        final RepositoryFolder tenantFolder = _repository.getFolder(tenant.getId());
        final RepositoryFolder jobsFolder = tenantFolder.getFolder(TimelineServiceImpl.PATH_JOBS);
        final RepositoryFile scheduleFile = jobsFolder.getFile(jobIdentifier.getName() + EXTENSION_SCHEDULE_XML);
        final JaxbScheduleReader reader = new JaxbScheduleReader();

        final String scheduleAfterJob;
        final String scheduleExpression;
        final boolean active;

        final List<Alert> alerts;

        if (scheduleFile == null) {
            scheduleExpression = null;
            scheduleAfterJob = null;
            active = false;
            alerts = Collections.emptyList();
        } else {
            InputStream inputStream = scheduleFile.readFile();
            final Schedule schedule;
            try {
                schedule = reader.unmarshallSchedule(inputStream);
            } finally {
                FileHelper.safeClose(inputStream);
            }

            alerts = schedule.getAlerts().getAlert();

            scheduleExpression = schedule.getScheduleExpression();
            active = schedule.isActive();
            scheduleAfterJob = schedule.getScheduleAfterJob();
        }

        final ScheduleDefinition scheduleDefinition;

        if (scheduleAfterJob == null) {
            scheduleDefinition = new ScheduleDefinition(tenant, jobIdentifier, scheduleExpression, active);
        } else {
            final JobIdentifier job = new JobIdentifier(scheduleAfterJob);

            scheduleDefinition = new ScheduleDefinition(tenant, jobIdentifier, job, active);
        }

        for (Alert alert : alerts) {
            final AlertDefinition alertDefinition = reader.createAlert(alert);
            scheduleDefinition.getAlerts().add(alertDefinition);
        }

        return scheduleDefinition;
    }

    @Override
    public ScheduleDefinition updateSchedule(final TenantIdentifier tenant, final ScheduleDefinition scheduleDefinition) {

        initializeSchedule(scheduleDefinition);

        final String jobName = scheduleDefinition.getJob().getName();

        final RepositoryFolder tenantFolder = _repository.getFolder(tenant.getId());
        final RepositoryFolder jobsFolder = tenantFolder.getFolder(TimelineServiceImpl.PATH_JOBS);
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

            if (schedule.isActive()) {
                final JobDetail jobDetail = new JobDetail(jobName, tenantId, ExecuteJob.class);
                JobDataMap jobDataMap = jobDetail.getJobDataMap();
                jobDataMap.put(AbstractQuartzJob.APPLICATION_CONTEXT, _applicationContext);
                jobDataMap.put(ExecuteJob.DETAIL_SCHEDULE_DEFINITION, schedule);

                final JobIdentifier scheduleAfterJob = schedule.getScheduleAfterJob();
                if (scheduleAfterJob == null) {
                    // time based trigger

                    final String scheduleExpression = schedule.getScheduleExpression();
                    final CronExpression cronExpression = toCronExpression(scheduleExpression);

                    final CronTriggerBean trigger = new CronTriggerBean();
                    trigger.setName(jobName);
                    trigger.setStartTime(new Date());
                    trigger.setCronExpression(cronExpression);
                    trigger.setJobDetail(jobDetail);

                    trigger.afterPropertiesSet();

                    logger.info("Adding trigger to scheduler: {} | {}", jobName, cronExpression);
                    _scheduler.scheduleJob(jobDetail, trigger);
                } else {
                    // event based trigger (via a job listener)

                    _scheduler.addJob(jobDetail, true);

                    final ExecuteJobListener listener = new ExecuteJobListener(jobListenerName, schedule);
                    _scheduler.addJobListener(listener);
                    logger.info("Adding listener to scheduler: {}", jobListenerName);
                }
            } else {
                logger.info("Not scheduling job: {} (inactive)", jobName);
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
        final AnalyzerBeansConfiguration configuration = _configurationCache.getAnalyzerBeansConfiguration(tenant);

        final ScheduleDefinition schedule = new ScheduleDefinition(tenant, job, (String) null, false);
        final ExecutionLog execution = new ExecutionLog(schedule, TriggerType.MANUAL);

        ExecuteJob.executeJob(tenant.getId(), _repository, configuration, execution);

        return execution;
    }

    @Override
    public ExecutionLog getLatestExecution(TenantIdentifier tenant, JobIdentifier job) {
        // TODO: Not implemented
        return null;
    }

    @Override
    public List<ExecutionLog> getAllExecutions(TenantIdentifier tenant, JobIdentifier job) {
        // TODO: Not implemented
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        _applicationContext = applicationContext;
    }

}
