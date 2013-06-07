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
package org.eobjects.datacleaner.monitor.scheduling.quartz;

import java.util.Date;
import java.util.Map;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.descriptors.ComponentDescriptor;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.lifecycle.LifeCycleHelper;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.events.JobExecutedEvent;
import org.eobjects.datacleaner.monitor.events.JobFailedEvent;
import org.eobjects.datacleaner.monitor.events.JobTriggeredEvent;
import org.eobjects.datacleaner.monitor.job.ExecutionLogger;
import org.eobjects.datacleaner.monitor.job.JobContext;
import org.eobjects.datacleaner.monitor.job.JobEngine;
import org.eobjects.datacleaner.monitor.job.JobEngineManager;
import org.eobjects.datacleaner.monitor.scheduling.api.VariableProvider;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.scheduling.model.TriggerType;
import org.eobjects.datacleaner.monitor.scheduling.model.VariableProviderDefinition;
import org.eobjects.datacleaner.monitor.server.job.ExecutionLoggerImpl;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Quartz job which encapsulates the process of executing a DataCleaner job and
 * writes the result to the repository.
 * 
 * The {@link ExecuteJob} class is annotated with
 * {@link DisallowConcurrentExecution}. This ensures that Quartz will not run a
 * job instance concurrently. For more details, please check out the section
 * "job instances" and "job state and concurrency", <a href=
 * "http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/tutorial-lesson-03"
 * >in the quartz documentation</a>.
 */
@DisallowConcurrentExecution
public class ExecuteJob extends AbstractQuartzJob {

    public static final String DETAIL_SCHEDULE_DEFINITION = "DataCleaner.schedule.definition";
    public static final String DETAIL_EXECUTION_LOG = "DataCleaner.schedule.execution.log";

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (logger.isInfoEnabled()) {
            logger.info("Executing quartz job with key: {} - {}", jobExecutionContext.getJobDetail().getKey(), jobExecutionContext.getJobInstance());
        }
        
        final ApplicationContext applicationContext;
        final ExecutionLog execution;
        final ScheduleDefinition schedule;
        final TenantIdentifier tenant;
        final TenantContext context;
        final JobEngineManager jobEngineManager;

        try {
            logger.debug("executeInternal({})", jobExecutionContext);

            applicationContext = getApplicationContext(jobExecutionContext);
            final JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
            if (jobDataMap.containsKey(DETAIL_EXECUTION_LOG)) {
                // the execution log has been provided already
                execution = (ExecutionLog) jobDataMap.get(DETAIL_EXECUTION_LOG);
                schedule = execution.getSchedule();
            } else {
                // we create a new execution log
                schedule = (ScheduleDefinition) jobDataMap.get(DETAIL_SCHEDULE_DEFINITION);
                if (schedule == null) {
                    throw new IllegalArgumentException("No schedule definition defined");
                }
                final TriggerType triggerType = schedule.getTriggerType();
                execution = new ExecutionLog(schedule, triggerType);
            }

            jobEngineManager = applicationContext.getBean(JobEngineManager.class);
            final TenantContextFactory contextFactory = applicationContext.getBean(TenantContextFactory.class);
            tenant = schedule.getTenant();
            logger.info("Tenant {} executing job {}", tenant.getId(), schedule.getJob());

            context = contextFactory.getContext(tenant);
        } catch (RuntimeException e) {
            logger.error("Unexpected error occurred in executeInternal!", e);
            throw e;
        }

        executeJob(context, execution, applicationContext, jobEngineManager);
        
        if (logger.isInfoEnabled()) {
            logger.info("Finished quartz job with key: {} - {}", jobExecutionContext.getJobDetail().getKey(), jobExecutionContext.getJobInstance());
        }
    }

    /**
     * Executes a DataCleaner job in the repository and stores the result.
     * 
     * @param context
     *            the tenant's {@link TenantContext}
     * @param execution
     *            the execution log object
     * @param eventPublisher
     *            publisher of application events, specifically for
     *            {@link JobTriggeredEvent}, {@link JobExecutedEvent} and
     *            {@link JobFailedEvent}.
     * @param jobEngineManager
     *            A {@link JobEngineManager} for determining the job engine to
     *            use
     * 
     * @return The expected result name, which can be used to get updates about
     *         execution status etc. at a later state.
     */
    protected String executeJob(final TenantContext context, final ExecutionLog execution,
            final ApplicationEventPublisher eventPublisher, final JobEngineManager jobEngineManager) {
        if (execution.getJobBeginDate() == null) {
            // although the job begin date will in vanilla scenarios be set by
            // the MonitorAnalysisListener, we also set it here, just in case of
            // unknown exception scenarios.
            execution.setJobBeginDate(new Date());
        }

        if (eventPublisher != null) {
            eventPublisher.publishEvent(new JobTriggeredEvent(this, execution));
        }

        final RepositoryFolder resultFolder = context.getResultFolder();
        final ExecutionLogger executionLogger = new ExecutionLoggerImpl(execution, resultFolder, eventPublisher);

        try {
            final JobContext job = context.getJob(execution.getJob().getName());

            final JobEngine<? extends JobContext> jobEngine = jobEngineManager.getJobEngine(job);
            if (jobEngine == null) {
                throw new UnsupportedOperationException("No Job engine available for job: " + job);
            }

            final AnalyzerBeansConfiguration configuration = context.getConfiguration();

            final VariableProviderDefinition variableProviderDef = execution.getSchedule().getVariableProvider();
            final Map<String, String> variables = overrideVariables(variableProviderDef, job, execution, configuration);

            jobEngine.executeJob(context, execution, executionLogger, variables);

        } catch (Throwable error) {
            // only initialization issues are catched here, eg. failing to load
            // job or configuration. Other issues will be reported to the
            // listener by the runner.
            executionLogger.setStatusFailed(null, null, error);
        }
        return execution.getResultId();
    }

    private Map<String, String> overrideVariables(VariableProviderDefinition variableProviderDef, JobContext job,
            ExecutionLog execution, AnalyzerBeansConfiguration configuration) throws ClassNotFoundException {
        if (variableProviderDef == null) {
            return null;
        }

        final String className = variableProviderDef.getClassName();
        if (className == null) {
            return null;
        }

        final InjectionManager injectionManager = configuration.getInjectionManager(null);
        final LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(injectionManager, null, true);

        @SuppressWarnings("unchecked")
        final Class<? extends VariableProvider> cls = (Class<? extends VariableProvider>) Class.forName(className);
        final ComponentDescriptor<? extends VariableProvider> descriptor = Descriptors.ofComponent(cls);
        final VariableProvider variableProvider = ReflectionUtils.newInstance(cls);
        lifeCycleHelper.assignProvidedProperties(descriptor, variableProvider);
        lifeCycleHelper.initialize(descriptor, variableProvider);
        try {
            final Map<String, String> variableOverrides = variableProvider.provideValues(job, execution);
            return variableOverrides;
        } finally {
            lifeCycleHelper.close(descriptor, variableProvider);
        }
    }
}
