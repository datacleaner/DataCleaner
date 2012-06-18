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
package org.eobjects.datacleaner.monitor.scheduling;

import java.util.Date;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.runner.AnalysisListener;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.datacleaner.monitor.configuration.ConfigurationCache;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.scheduling.model.TriggerType;
import org.eobjects.datacleaner.monitor.server.MonitorJobReader;
import org.eobjects.datacleaner.monitor.server.TimelineServiceImpl;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

/**
 * Quartz job which encapsulates the process of executing a DataCleaner job and
 * writes the result to the repository.
 */
public class ExecuteJob extends AbstractQuartzJob {

    public static final String DETAIL_SCHEDULE_DEFINITION = "DataCleaner.schedule.definition";

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        logger.debug("executeInternal({})", context);

        final ApplicationContext applicationContext = getApplicationContext(context);

        final Repository repository = applicationContext.getBean(Repository.class);
        final ConfigurationCache configurationCache = applicationContext.getBean(ConfigurationCache.class);

        final JobDetail jobDetail = context.getJobDetail();
        final ScheduleDefinition schedule = (ScheduleDefinition) jobDetail.getJobDataMap().get(
                DETAIL_SCHEDULE_DEFINITION);
        if (schedule == null) {
            throw new IllegalArgumentException("No schedule definition defined");
        }

        final String tenantId = schedule.getTenant().getId();
        logger.info("Tenant {} executing job {}", tenantId, schedule.getJob());

        final AnalyzerBeansConfiguration configuration = configurationCache.getAnalyzerBeansConfiguration(tenantId);
        final ExecutionLog execution = new ExecutionLog(schedule, TriggerType.SCHEDULED);

        executeJob(tenantId, repository, configuration, execution);
    }

    /**
     * Executes a DataCleaner job in the repository and stores the result
     * 
     * @param tenantId
     *            the tenant id
     * @param repository
     *            the repository
     * @param configuration
     *            the configuration to use
     * @param execution
     *            the execution log object
     * 
     * @return The expected result name, which can be used to get updates about
     *         execution status etc. at a later state.
     */
    public static String executeJob(String tenantId, Repository repository, AnalyzerBeansConfiguration configuration,
            ExecutionLog execution) {

        final String jobName = execution.getJob().getName();

        final RepositoryFolder tenantFolder = repository.getFolder(tenantId);
        final RepositoryFolder jobsFolder = tenantFolder.getFolder(TimelineServiceImpl.PATH_JOBS);
        final String jobFileName = jobName + FileFilters.ANALYSIS_XML.getExtension();
        final RepositoryFile jobFile = jobsFolder.getFile(jobFileName);
        if (jobFile == null) {
            throw new IllegalArgumentException("No such job file: " + jobFileName);
        }

        final MonitorJobReader jobReader = new MonitorJobReader(configuration, jobFile);
        final AnalysisJob job = jobReader.readJob();

        final RepositoryFolder resultFolder = tenantFolder.getFolder(TimelineServiceImpl.PATH_RESULTS);
        final Date timestamp = new Date();
        final String resultName = jobName + "-" + timestamp.getTime();

        final AnalysisListener analysisListener = new MonitorAnalysisListener(execution, resultFolder, resultName);
        final AnalysisRunnerImpl runner = new AnalysisRunnerImpl(configuration, analysisListener);

        // fire and forget
        runner.run(job);

        // TODO: Check alerts

        return resultName;
    }
}
