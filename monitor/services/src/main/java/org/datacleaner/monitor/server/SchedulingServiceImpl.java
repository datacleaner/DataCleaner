/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.monitor.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.FileResource;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.ExcelDatastore;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.JaxbJobReader;
import org.datacleaner.job.JaxbJobWriter;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.job.ExecutionLogger;
import org.datacleaner.monitor.job.JobContext;
import org.datacleaner.monitor.job.JobEngine;
import org.datacleaner.monitor.scheduling.SchedulingService;
import org.datacleaner.monitor.scheduling.model.ExecutionIdentifier;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.monitor.scheduling.model.ExecutionStatus;
import org.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.datacleaner.monitor.scheduling.model.TriggerType;
import org.datacleaner.monitor.scheduling.quartz.AbstractQuartzJob;
import org.datacleaner.monitor.scheduling.quartz.ExecuteJob;
import org.datacleaner.monitor.scheduling.quartz.ExecuteJobListener;
import org.datacleaner.monitor.server.hotfolder.DefaultWaitForCompleteFileStrategy;
import org.datacleaner.monitor.server.hotfolder.HotFolderPreferences;
import org.datacleaner.monitor.server.hotfolder.IncompleteFileException;
import org.datacleaner.monitor.server.hotfolder.WaitForCompleteFileStrategy;
import org.datacleaner.monitor.server.hotfolder.WrongInputException;
import org.datacleaner.monitor.server.jaxb.JaxbException;
import org.datacleaner.monitor.server.jaxb.JaxbExecutionLogReader;
import org.datacleaner.monitor.server.jaxb.JaxbScheduleReader;
import org.datacleaner.monitor.server.jaxb.JaxbScheduleWriter;
import org.datacleaner.monitor.server.jaxb.SaxExecutionIdentifierReader;
import org.datacleaner.monitor.server.job.ExecutionLoggerImpl;
import org.datacleaner.monitor.shared.model.CronExpressionException;
import org.datacleaner.monitor.shared.model.DCSecurityException;
import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.util.FileFilters;
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

import com.google.common.collect.Maps;

/**
 * Main implementation of the {@link SchedulingService} interface.
 */
@Component("schedulingService")
public class SchedulingServiceImpl implements SchedulingService, ApplicationContextAware {
    @Autowired
    HotFolderPreferences _hotFolderPreferences;

    private final class HotFolderAlterationListener extends FileAlterationListenerAdaptor {
        private static final String PROPERTIES_FILE_EXTENSION = ".properties";
        private static final String TRIGGER_FILE_EXTENSION = ".trigger";
        private static final String VARIABLE_FILE_NAME = "hotfolder.input.filename";
        private static final String VARIABLE_EXTENSION = "hotfolder.input.extension";
        private static final String VARIABLE_DATE = "datacleaner.run.date";
        private static final String VARIABLE_TIME = "datacleaner.run.time";
        private static final String DATE_FORMAT = "yyyyMMdd";
        private static final String TIME_FORMAT = "HHmmss";

        private final JobIdentifier job;
        private final TenantIdentifier tenant;
        private final String overridePropertiesFilePath;

        private HotFolderAlterationListener(final JobIdentifier job, final ScheduleDefinition schedule) {
            this.job = job;
            this.tenant = schedule.getTenant();

            final String hotFolderPath = schedule.getHotFolder();
            final File hotFolder = new File(hotFolderPath);
            final String overridePropertiesFileName = job.getName() + PROPERTIES_FILE_EXTENSION;
            if (hotFolder.isDirectory()) {
                overridePropertiesFilePath = new File(hotFolder, overridePropertiesFileName).getAbsolutePath();
            } else if (hotFolderPath.endsWith(PROPERTIES_FILE_EXTENSION)) {
                overridePropertiesFilePath = hotFolderPath;
            } else {
                overridePropertiesFilePath = FilenameUtils.getFullPath(hotFolderPath) + overridePropertiesFileName;
            }
        }

        @Override
        public void onFileCreate(final File file) {
            logger.info("file {} created in hot folder, triggering execution of job {}.", file.getName(),
                    job.getName());
            executeJobWithFileInNewThread(file);
        }

        @Override
        public void onFileChange(final File file) {
            logger.info("file {} changed in hot folder, triggering execution of job {}.", file.getName(),
                    job.getName());
            executeJobWithFileInNewThread(file);
        }

        private void executeJobWithFileInNewThread(final File inputFile) {
            new Thread(() -> {
                try {
                    checkInputFile(inputFile);
                    fillJobVariables(inputFile);
                    executeJobWithFile(inputFile);
                } catch (final WrongInputException e) {
                    logger.error("File '{}' does not contain required columns. " + e.getMessage(), inputFile.getName());
                }
            }).start();
        }

        private void checkInputFile(final File inputFile) throws WrongInputException {
            final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
            final Datastore oldDataStore = tenantContext.getConfiguration().getDatastoreCatalog()
                    .getDatastore(getDataStoreName());
            final Datastore newDataStore = getNewDataStore(inputFile, oldDataStore);
            final String[] oldColumnNames = oldDataStore.openConnection().getSchemaNavigator().getDefaultSchema()
                    .getTable(0).getColumnNames();
            final String[] newColumnNames = newDataStore.openConnection().getSchemaNavigator().getDefaultSchema()
                    .getTable(0).getColumnNames();

            if (oldColumnNames.length != newColumnNames.length) {
                throw new WrongInputException("The file does not have correct number of input columns ("
                        + oldColumnNames.length + " required, " + newColumnNames.length + " present). ");
            }

            for (int i = 0; i < oldColumnNames.length; i++) {
                if (!oldColumnNames[i].equals(newColumnNames[i])) {
                    throw new WrongInputException("The file does not have a correct input column at position " + i
                            + " ('" + oldColumnNames[i] + "' required, '" + newColumnNames[i] + "' present). ");
                }
            }
        }

        private Datastore getNewDataStore(final File inputFile, final Datastore oldDataStore)
                throws WrongInputException {
            final Datastore newDataStore;

            if (oldDataStore instanceof CsvDatastore) {
                newDataStore = new CsvDatastore(inputFile.getName(), inputFile.getAbsolutePath());
            } else if (oldDataStore instanceof ExcelDatastore) {
                newDataStore = new ExcelDatastore(inputFile.getName(), new FileResource(inputFile),
                        inputFile.getAbsolutePath());
            } else {
                throw new WrongInputException("Unsupported data store type (" + inputFile.getAbsolutePath() + "). ");
            }

            return newDataStore;
        }

        private void fillJobVariables(final File file) {
            try {
                final File jobFile = getJobFile();
                final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
                final JaxbJobReader reader = new JaxbJobReader(tenantContext.getConfiguration());
                final AnalysisJob analysisJob = reader.read(new FileInputStream(jobFile));
                final Map<String, String> variables = analysisJob.getMetadata().getVariables();
                final String fileName = file.getName();
                final String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

                variables.put(VARIABLE_FILE_NAME, fileName);
                variables.put(VARIABLE_EXTENSION, extension);
                variables.put(VARIABLE_DATE, getDateTimePart(DATE_FORMAT));
                variables.put(VARIABLE_TIME, getDateTimePart(TIME_FORMAT));

                final OutputStream outputStream = new FileOutputStream(jobFile);
                new JaxbJobWriter(tenantContext.getConfiguration()).write(analysisJob, outputStream);
            } catch (final FileNotFoundException | RuntimeException e) {
                logger.error(e.getMessage());
            }
        }

        private String getDateTimePart(final String format) {
            final DateFormat dateFormat = new SimpleDateFormat(format);

            return dateFormat.format(new Date());
        }

        private File getJobFile() {
            final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
            final JobContext jobContext = tenantContext.getJob(job);
            final String jobFileName = jobContext.getJobFile().getName();
            final RepositoryFile jobRepositoryFile = tenantContext.getTenantRootFolder().getFolder("jobs")
                    .getFile(jobFileName);

            return new File(jobRepositoryFile.toResource().getQualifiedPath());
        }

        private void executeJobWithFile(final File file) {
            if (!file.exists()) {
                logger.warn("Data file '{}' triggering execution of the job '{}' does not exist.",
                        file.getAbsolutePath(), job.getName());
                return;
            }

            final String dataStoreName = getDataStoreName();
            final String triggerFileName = job.getName() + TRIGGER_FILE_EXTENSION;

            if (file.getName().equals(triggerFileName)) {
                triggerJobExecution();
            } else if (dataStoreName != null) {
                try {
                    /*
                     * File event can be triggered before the complete file content is written (copying in progress).
                     * This tries to check that possibility and wait for the complete file to be ready.
                     */
                    getWaitStrategy().waitForComplete(file);
                } catch (final IncompleteFileException e) {
                    logger.error("Hot folder job execution failed because the file '{}' is incomplete of unavailable. ",
                            file.getAbsolutePath());
                    return;
                }

                final Map<String, String> propertiesMap = new HashMap<>();
                propertiesMap.put("datastoreCatalog." + getDataStoreName() + ".filename", file.getAbsolutePath());
                triggerExecution(tenant, job, propertiesMap, TriggerType.HOTFOLDER);
            } else {
                logger.error("Hot folder job execution was cancelled because datastore name is missing. ");
            }
        }

        private WaitForCompleteFileStrategy getWaitStrategy() {
            WaitForCompleteFileStrategy waitStrategy;
            final String waitStrategyClassPath = _hotFolderPreferences.getWaitStrategyClass();

            try {
                waitStrategy = (WaitForCompleteFileStrategy) Class.forName(waitStrategyClassPath).newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                logger.warn("'{}' could not be used for hot-folder wait strategy, using default instead. {}",
                        waitStrategyClassPath, e);
                waitStrategy = new DefaultWaitForCompleteFileStrategy();
            }

            return waitStrategy;
        }

        private String getDataStoreName() {
            try {
                final File jobFile = getJobFile();
                final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
                final JaxbJobReader reader = new JaxbJobReader(tenantContext.getConfiguration());

                return reader.readMetadata(jobFile).getDatastoreName();
            } catch (RuntimeException e) {
                logger.warn("DataStore name could not be resolved. {}", e);
                return null;
            }
        }

        private void triggerJobExecution() {
            final FileResource overrideProperties = new FileResource(overridePropertiesFilePath);
            Map<String, String> propertiesMap = null;

            if (overrideProperties.isExists()) {
                try {
                    final Properties properties = new Properties();
                    properties.load(overrideProperties.read());

                    propertiesMap = Maps.fromProperties(properties);
                } catch (final IOException e) {
                    logger.warn("Exception occurred when loading properties file {} for hot folder trigger of job {}.",
                            overridePropertiesFilePath, job.getName());
                }
            }

            triggerExecution(tenant, job, propertiesMap, TriggerType.HOTFOLDER);
        }
    }

    public static final String EXTENSION_SCHEDULE_XML = ".schedule.xml";
    private static final Logger logger = LoggerFactory.getLogger(SchedulingServiceImpl.class);
    private final Repository _repository;
    private final TenantContextFactory _tenantContextFactory;
    private final Scheduler _scheduler;
    private final SchedulingServiceConfiguration _schedulingServiceConfiguration;
    private final FileAlterationMonitor _hotFolderMonitor = new FileAlterationMonitor();
    private ApplicationContext _applicationContext;
    private Map<String, FileAlterationObserver> _registeredHotFolders = new HashMap<>();

    /**
     * @param repository
     * @param tenantContextFactory
     */
    public SchedulingServiceImpl(final Repository repository, final TenantContextFactory tenantContextFactory) {
        this(repository, tenantContextFactory, createDefaultScheduler(), new SchedulingServiceConfiguration());
    }

    /**
     * Default constructor.
     *
     * @param repository
     * @param tenantContextFactory
     * @param scheduler
     * @param schedulingServiceConfiguration
     */
    @Autowired
    public SchedulingServiceImpl(final Repository repository, final TenantContextFactory tenantContextFactory,
            final Scheduler scheduler, final SchedulingServiceConfiguration schedulingServiceConfiguration) {
        if (repository == null) {
            throw new IllegalArgumentException("Repository cannot be null");
        }
        if (tenantContextFactory == null) {
            throw new IllegalArgumentException("TenantContextFactory cannot be null");
        }
        if (scheduler == null) {
            throw new IllegalArgumentException("Quartz scheduler cannot be null");
        }
        if (schedulingServiceConfiguration == null) {
            throw new IllegalArgumentException("SchedulingServiceConfiguration cannot be null");
        }
        _repository = repository;
        _tenantContextFactory = tenantContextFactory;
        _scheduler = scheduler;
        _schedulingServiceConfiguration = schedulingServiceConfiguration;
    }

    /**
     * Creates a default single-node scheduler
     *
     * @return
     */
    public static Scheduler createDefaultScheduler() {
        try {
            final StdSchedulerFactory factory = new StdSchedulerFactory();
            return factory.getScheduler();
        } catch (final Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalStateException("Failed to create scheduler", e);
        }
    }

    protected static CronExpression toCronExpressionForOneTimeSchedule(String scheduleExpression) throws CronExpressionException {
        scheduleExpression = scheduleExpression.trim();
        final CronExpression cronExpression;
        try {
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            simpleDateFormat.setLenient(false);
            final Date oneTimeSchedule = simpleDateFormat.parse(scheduleExpression);
            final Calendar dateInfoExtractor = Calendar.getInstance();
            dateInfoExtractor.setTime(oneTimeSchedule);
            final int month = dateInfoExtractor.get(Calendar.MONTH) + 1;
            final StringBuilder cronStringBuilder = new StringBuilder();
            final String cronBuilder =
                    cronStringBuilder.append(" ").append(dateInfoExtractor.get(Calendar.SECOND)).append(" ")
                            .append(dateInfoExtractor.get(Calendar.MINUTE)).append(" ")
                            .append(dateInfoExtractor.get(Calendar.HOUR_OF_DAY)).append(" ")
                            .append(dateInfoExtractor.get(Calendar.DAY_OF_MONTH)).append(" ").append(month)
                            .append(" ? ").append(dateInfoExtractor.get(Calendar.YEAR)).toString();
            cronExpression = new CronExpression(cronBuilder);
        } catch (final ParseException e) {
            throw new CronExpressionException(
                    "The cron expression is not valid for one time schedule: " + scheduleExpression);
        }
        
        if (logger.isInfoEnabled()) {
            logger.info("Cron expression summary ({}): {}", scheduleExpression,
                    cronExpression.getExpressionSummary().replaceAll("\n", ", "));
        }

        return cronExpression;
    }

    protected static CronExpression toCronExpression(String scheduleExpression) throws CronExpressionException {
        scheduleExpression = scheduleExpression.trim();

        final CronExpression cronExpression;

        try {
            // CRON expression: Sec Min Hr DoM M DoW (Y)

            if ("@yearly".equals(scheduleExpression) || "@annually".equals(scheduleExpression)) {
                cronExpression = new CronExpression("0 0 0 1 1 ? *");
            } else if ("@monthly".equals(scheduleExpression)) {
                cronExpression = new CronExpression("0 0 0 1 * ?");
            } else if ("@weekly".equals(scheduleExpression)) {
                cronExpression = new CronExpression("0 0 0 ? * 1");
            } else if ("@daily".equals(scheduleExpression)) {
                cronExpression = new CronExpression("0 0 0 * * ?");
            } else if ("@hourly".equals(scheduleExpression)) {
                cronExpression = new CronExpression("0 0 * * * ?");
            } else if ("@minutely".equals(scheduleExpression) || "@every_minute".equals(scheduleExpression)) {
                cronExpression = new CronExpression("0 * * * * ?");
            } else {
                cronExpression = new CronExpression(scheduleExpression);
            }
        } catch (final ParseException e) {
            throw new CronExpressionException("The cron expression is not valid: " + scheduleExpression);
        }

        if (logger.isInfoEnabled()) {
            logger.info("Cron expression summary ({}): {}", scheduleExpression,
                    cronExpression.getExpressionSummary().replaceAll("\n", ", "));
        }

        return cronExpression;
    }

    public Scheduler getScheduler() {
        return _scheduler;
    }

    public SchedulingServiceConfiguration getSchedulingServiceConfiguration() {
        return _schedulingServiceConfiguration;
    }

    @PostConstruct()
    public void initialize() throws CronExpressionException {
        // initialize tenants by scanning tenant folders
        if (_schedulingServiceConfiguration.isTenantInitialization()) {
            final List<RepositoryFolder> tenantFolders = _repository.getFolders();
            for (final RepositoryFolder tenantFolder : tenantFolders) {
                final TenantIdentifier tenant = new TenantIdentifier(tenantFolder.getName());
                final String tenantId = tenant.getId();

                final List<ScheduleDefinition> schedules = getSchedules(tenant, true);
                logger.info("Initializing {} schedules for tenant {}", schedules.size(), tenantId);

                for (final ScheduleDefinition schedule : schedules) {
                    initializeSchedule(schedule);
                }
            }
        }

        // start scheduler
        try {
            if (!_scheduler.isStarted()) {
                _scheduler.start();
            }
        } catch (final SchedulerException e) {
            throw new IllegalStateException("Failed to start scheduler", e);
        }

        try {
            _hotFolderMonitor.start();
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to start file alteration monitor", e);
        }

        if (_schedulingServiceConfiguration.isTenantInitialization()) {
            logTriggers();
        }

        logger.info("Schedule initialization done!");
    }

    private void logTriggers() {
        final List<RepositoryFolder> tenantFolders = _repository.getFolders();
        for (final RepositoryFolder tenantFolder : tenantFolders) {
            final String tenantId = tenantFolder.getName();
            try {
                final Set<TriggerKey> triggerKeys =
                        _scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(tenantId));
                if (triggerKeys == null || triggerKeys.isEmpty()) {
                    logger.info("No triggers initialized for tenant: {}", tenantId);
                } else {
                    for (final TriggerKey triggerKey : triggerKeys) {
                        logger.info("Trigger of tenant {}: {}", tenantId, triggerKey);
                    }
                }
            } catch (final SchedulerException e) {
                logger.warn("Failed to get triggers of tenant: " + tenantId, e);
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            _scheduler.shutdown();
        } catch (final SchedulerException e) {
            logger.error("Failed to shutdown scheduler: " + e.getMessage(), e);
        }

        try {
            _hotFolderMonitor.stop();
        } catch (final Exception e) {
            logger.error("Failed to shutdown file alteration monitor: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ScheduleDefinition> getSchedules(final TenantIdentifier tenant, final boolean loadProperties) {
        final TenantContext context = _tenantContextFactory.getContext(tenant);

        final List<JobIdentifier> jobs = context.getJobs();
        final List<ScheduleDefinition> schedules = new ArrayList<>(jobs.size());
        for (final JobIdentifier job : jobs) {
            try {
                schedules.add(getSchedule(tenant, job));
            } catch (final Exception e) {
                logger.error("Failed to initialize schedule for tenant '" + tenant.getId() + "' job '" + job.getName()
                        + "'.", e);
            }
        }
        return schedules;
    }

    @Override
    public ScheduleDefinition getSchedule(final TenantIdentifier tenant, final JobIdentifier jobIdentifier) {
        return getSchedule(tenant, jobIdentifier, null);
    }

    private ScheduleDefinition getSchedule(final TenantIdentifier tenant, final JobIdentifier jobIdentifier,
            final Map<String, String> overrideProperties) {
        final TenantContext context = _tenantContextFactory.getContext(tenant);

        final String jobName = jobIdentifier.getName();

        final JobContext jobContext = context.getJob(jobIdentifier);
        if (jobContext == null) {
            throw new IllegalArgumentException("No such job: " + jobName);
        }

        final Map<String, String> immutableJobMetadataProperties = jobContext.getMetadataProperties();
        // Convert Immutable map to mutable map as it will be transferred to a
        // GWT object on the front end .
        final Map<String, String> jobMetadataProperties = new HashMap<>(immutableJobMetadataProperties);

        final String groupName = jobContext.getGroupName();

        final RepositoryFolder jobsFolder = context.getJobFolder();

        final RepositoryFile scheduleFile = jobsFolder.getFile(jobName + EXTENSION_SCHEDULE_XML);
        final JaxbScheduleReader reader = new JaxbScheduleReader();

        final ScheduleDefinition schedule;

        if (scheduleFile == null) {
            schedule = new ScheduleDefinition(tenant, jobIdentifier, groupName);
        } else {
            schedule = scheduleFile.readFile(inputStream -> {
                return reader.read(inputStream, jobIdentifier, tenant, groupName);
            });
        }

        schedule.setJobMetadataProperties(jobMetadataProperties);
        schedule.setOverrideProperties(overrideProperties);

        return schedule;
    }

    private ScheduleDefinition getScheduleWithoutProperties(final TenantIdentifier tenant,
            final JobIdentifier jobIdentifier) {
        final JobContext jobContext = getJobContext(tenant, jobIdentifier);
        final String groupName = jobContext.getGroupName();

        return new ScheduleDefinition(tenant, jobIdentifier, groupName);
    }

    private JobContext getJobContext(final TenantIdentifier tenant, final JobIdentifier jobIdentifier) {
        final String jobName = jobIdentifier.getName();
        final TenantContext context = _tenantContextFactory.getContext(tenant);
        final JobContext jobContext = context.getJob(jobIdentifier);

        if (jobContext == null) {
            throw new IllegalArgumentException("No such job: " + jobName);
        }

        return jobContext;
    }

    @Override
    public ScheduleDefinition updateSchedule(final TenantIdentifier tenant,
            final ScheduleDefinition scheduleDefinition) throws DCUserInputException {

        initializeSchedule(scheduleDefinition);

        final String jobName = scheduleDefinition.getJob().getName();

        final TenantContext context = _tenantContextFactory.getContext(tenant);

        final RepositoryFolder jobsFolder = context.getJobFolder();
        final String filename = jobName + EXTENSION_SCHEDULE_XML;
        final RepositoryFile file = jobsFolder.getFile(filename);

        final Action<OutputStream> writeAction = out -> {
            final JaxbScheduleWriter writer = new JaxbScheduleWriter();
            writer.write(scheduleDefinition, out);
        };

        if (file == null) {
            jobsFolder.createFile(filename, writeAction);
        } else {
            file.writeFile(writeAction);
        }

        return scheduleDefinition;
    }

    private void initializeSchedule(final ScheduleDefinition schedule) throws CronExpressionException {
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
                final JobDetail jobDetail =
                        JobBuilder.newJob(ExecuteJob.class).withIdentity(jobName, tenantId).storeDurably().build();

                final JobDataMap jobDataMap = jobDetail.getJobDataMap();
                jobDataMap.put(ExecuteJob.DETAIL_SCHEDULE_DEFINITION, schedule);

                if (triggerType == TriggerType.PERIODIC) {
                    // time based trigger
                    final String scheduleExpression = schedule.getCronExpression();
                    final CronExpression cronExpression = toCronExpression(scheduleExpression);
                    final CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule(cronExpression);
                    final CronTrigger trigger =
                            TriggerBuilder.newTrigger().withIdentity(jobName, tenantId).forJob(jobDetail)
                                    .withSchedule(cronSchedule).startNow().build();

                    logger.info("Adding trigger to scheduler: {} | {}", jobName, cronExpression);
                    _scheduler.scheduleJob(jobDetail, trigger);

                } else if (triggerType == TriggerType.ONETIME) {
                    final String scheduleDate = schedule.getDateForOneTimeSchedule();
                    final CronExpression cronExpression = toCronExpressionForOneTimeSchedule(scheduleDate);
                    final Date nextValidTimeAfter = cronExpression.getNextValidTimeAfter(new Date());
                    if (nextValidTimeAfter != null) {
                        final CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule(cronExpression);
                        final CronTrigger trigger =
                                TriggerBuilder.newTrigger().withIdentity(jobName, tenantId).forJob(jobDetail)
                                        .withSchedule(cronSchedule).startNow().build();
                        logger.info("Adding trigger to scheduler for One time schedule: {} | {}", jobName,
                                cronExpression);
                        _scheduler.scheduleJob(jobDetail, trigger);
                    }
                } else if (triggerType == TriggerType.HOTFOLDER) {
                    final String hotFolder = schedule.getHotFolder();

                    if (hotFolder != null) {
                        final FileAlterationObserver observer = createObserver(hotFolder);

                        observer.addListener(new HotFolderAlterationListener(job, schedule));

                        _hotFolderMonitor.addObserver(observer);

                        _registeredHotFolders.put(jobListenerName, observer);

                        logger.info("Adding hot folder {} as trigger for job {}", hotFolder, jobListenerName);
                    }
                } else {
                    // event based trigger (via a job listener)
                    _scheduler.addJob(jobDetail, true);
                    final ExecuteJobListener listener = new ExecuteJobListener(jobListenerName, schedule);
                    _scheduler.getListenerManager().addJobListener(listener);
                    logger.info("Adding listener to scheduler: {}", jobListenerName);
                }
            }
        } catch (final Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            
            throw new IllegalStateException("Failed to schedule job: " + job, e);
        }
    }

    private FileAlterationObserver createObserver(final String fileName) {
        final File file = new File(fileName);

        if (file.isDirectory()) {
            return new FileAlterationObserver(file);
        } else {
            return new FileAlterationObserver(FilenameUtils.getFullPathNoEndSeparator(fileName),
                    FileFilterUtils.nameFileFilter(FilenameUtils.getName(fileName)));
        }
    }

    @Override
    public void removeSchedule(final TenantIdentifier tenant, final JobIdentifier job) throws DCSecurityException {
        logger.info("Removing schedule for job: " + job);
        final String jobName = job.getName();
        final String tenantId = tenant.getId();
        final String jobListenerName = tenantId + "." + jobName;
        try {
            _scheduler.deleteJob(new JobKey(jobName, tenantId));
            _scheduler.getListenerManager().removeJobListener(jobListenerName);
        } catch (final Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalStateException("Failed to remove job schedule: " + job, e);
        }
        removeHotFolder(jobListenerName);
    }

    @Override
    public boolean cancelExecution(final TenantIdentifier tenant, final ExecutionLog execution)
            throws DCSecurityException {
        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        final JobContext job = tenantContext.getJob(execution.getJob());
        final JobEngine<?> jobEngine = job.getJobEngine();
        return jobEngine.cancelJob(tenantContext, execution);
    }

    @Override
    public ExecutionLog triggerExecution(final TenantIdentifier tenant, final JobIdentifier job) {
        return triggerExecution(tenant, job, null);
    }

    @Override
    public ExecutionLog triggerExecution(final TenantIdentifier tenant, final JobIdentifier job,
            final Map<String, String> overrideProperties) {
        return triggerExecution(tenant, job, overrideProperties, TriggerType.MANUAL);
    }

    private ExecutionLog triggerExecution(final TenantIdentifier tenant, final JobIdentifier job,
            final Map<String, String> overrideProperties, final TriggerType manual) {
        final String jobNameToBeTriggered = job.getName();
        final ScheduleDefinition schedule = getSchedule(tenant, job, overrideProperties);
        final ExecutionLog execution = new ExecutionLog(schedule, manual);
        execution.setJobBeginDate(new Date());

        try {
            boolean addJob = true;
            final GroupMatcher<JobKey> matcher = GroupMatcher.jobGroupEquals(tenant.getId());
            final Set<JobKey> jobKeys = _scheduler.getJobKeys(matcher);
            for (final JobKey jobKey : jobKeys) {
                final String jobName = jobKey.getName();
                if (jobName.equals(jobNameToBeTriggered)) {
                    addJob = false;
                    break;
                }
            }
            if (addJob) {
                final JobDetail jobDetail =
                        JobBuilder.newJob(ExecuteJob.class).withIdentity(jobNameToBeTriggered, tenant.getId())
                                .storeDurably().build();
                _scheduler.addJob(jobDetail, true);
            }

            // set the "triggered by" attribute.
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                final String username = authentication.getName();
                execution.setTriggeredBy(username);
            }

            // save the initial result log file
            final RepositoryFolder resultFolder = _tenantContextFactory.getContext(tenant).getResultFolder();
            final ExecutionLogger executionLogger = new ExecutionLoggerImpl(execution, resultFolder, null);
            executionLogger.flushLog();

            final JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(ExecuteJob.DETAIL_EXECUTION_LOG, execution);

            _scheduler.triggerJob(new JobKey(jobNameToBeTriggered, tenant.getId()), jobDataMap);
        } catch (final SchedulerException e) {
            throw new IllegalStateException("Unexpected error invoking scheduler", e);
        }

        return execution;
    }

    @Override
    public ExecutionLog getLatestExecution(final TenantIdentifier tenant, final JobIdentifier job) {
        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        final RepositoryFolder resultFolder = tenantContext.getResultFolder();

        final RepositoryFile latestFile =
                resultFolder.getLatestFile(job.getName(), FileFilters.ANALYSIS_EXECUTION_LOG_XML.getExtension());

        if (latestFile == null) {
            // no execution available
            return null;
        }

        return readExecutionLogFile(latestFile, job, tenant, 1);
    }

    @Override
    public List<ExecutionIdentifier> getAllExecutions(final TenantIdentifier tenant, final JobIdentifier job)
            throws IllegalStateException {
        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        final RepositoryFolder resultFolder = tenantContext.getResultFolder();
        final List<RepositoryFile> files =
                resultFolder.getFiles(job.getName(), FileFilters.ANALYSIS_EXECUTION_LOG_XML.getExtension());

        final List<ExecutionIdentifier> executionIdentifiers = CollectionUtils.map(files, file -> {
            try {
                return file.readFile(in -> {
                    return SaxExecutionIdentifierReader.read(in, file.getQualifiedPath());
                });
            } catch (final Exception e) {
                logger.warn("The file " + file.getQualifiedPath() + " could not be read or parsed correctly " + e);
                return new ExecutionIdentifier(
                        "Execution failed for " + FilenameUtils.getBaseName(file.getQualifiedPath()));
            }
        });

        Collections.sort(executionIdentifiers);

        return executionIdentifiers;
    }

    @Override
    public ExecutionLog getExecution(final TenantIdentifier tenant, final ExecutionIdentifier executionIdentifier)
            throws DCSecurityException {
        if (executionIdentifier == null) {
            return null;
        }
        final String resultId = executionIdentifier.getResultId();
        if (resultId == null) {
            return null;
        }

        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        final RepositoryFolder resultFolder = tenantContext.getResultFolder();

        final RepositoryFile file =
                resultFolder.getFile(resultId + FileFilters.ANALYSIS_EXECUTION_LOG_XML.getExtension());
        if (file == null) {
            logger.warn("No execution with result id: " + resultId);
            return null;
        }

        final JobIdentifier jobIdentifier = JobIdentifier.fromExecutionIdentifier(executionIdentifier);

        return readExecutionLogFile(file, jobIdentifier, tenant, 3);
    }

    private ExecutionLog readExecutionLogFile(final RepositoryFile file, final JobIdentifier jobIdentifier,
            final TenantIdentifier tenant, final int retries) {
        final JaxbExecutionLogReader reader = new JaxbExecutionLogReader();

        final ExecutionLog result = file.readFile(in -> {
            try {
                return reader.read(in, jobIdentifier, tenant);
            } catch (final JaxbException e) {
                if (retries > 0) {
                    logger.debug("Failed to read execution log in first pass. This could be because it "
                            + "is also being written at this time. Retrying.");
                    return null;
                } else {
                    logger.info("Failed to read execution log, returning unknown status.");
                    final ExecutionLog executionLog = new ExecutionLog(null, null);
                    executionLog.setExecutionStatus(ExecutionStatus.UNKNOWN);
                    executionLog.setJob(jobIdentifier);
                    return executionLog;
                }
            }
        });

        if (result == null) {
            // retry
            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                // do nothing
            }
            return readExecutionLogFile(file, jobIdentifier, tenant, retries - 1);
        }

        return result;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        _applicationContext = applicationContext;
        try {
            final SchedulerContext schedulerContext = _scheduler.getContext();
            schedulerContext.put(AbstractQuartzJob.APPLICATION_CONTEXT, _applicationContext);
        } catch (final SchedulerException e) {
            logger.error("Failed to get scheduler context and set application context on it. Expect issues when "
                    + "invoking jobs, or set property '" + AbstractQuartzJob.APPLICATION_CONTEXT
                    + " on the scheduler's context manually'.", e);
        }
    }

    @Override
    public List<JobIdentifier> getDependentJobCandidates(final TenantIdentifier tenant,
            final ScheduleDefinition schedule) throws DCSecurityException {
        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        final List<JobIdentifier> jobs = tenantContext.getJobs();
        final List<JobIdentifier> result = new ArrayList<>();
        for (final JobIdentifier job : jobs) {
            final String jobName = job.getName();
            if (!jobName.equals(schedule.getJob().getName())) {
                result.add(job);
            }
        }
        return result;
    }

    @Override
    public String getServerDate() {
        final Date serverDate = new Date();
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(serverDate);
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String serverDateFormat = dateFormat.format(serverDate);
        final String timeStampName = calendar.getTimeZone().getDisplayName();
        logger.info("Date and TimeStamp for one time schedule: {} | {}", serverDate, timeStampName);
        return serverDateFormat;
    }

    void removeHotFolder(final String jobIdentifier) {
        final FileAlterationObserver observer = _registeredHotFolders.get(jobIdentifier);
        _hotFolderMonitor.removeObserver(observer);
        try {
            observer.destroy();
        } catch (final Exception e) {
            logger.info("Removing hot folder trigger for job {}", jobIdentifier);
        }
    }
}
