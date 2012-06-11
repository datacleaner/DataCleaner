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

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Date;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.SimpleAnalysisResult;
import org.eobjects.datacleaner.monitor.configuration.ConfigurationCache;
import org.eobjects.datacleaner.monitor.server.MonitorJobReader;
import org.eobjects.datacleaner.monitor.server.TimelineServiceImpl;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.metamodel.util.Action;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

/**
 * Quartz job which encapsulates the process of executing a DataCleaner job and
 * writes the result to the repository.
 */
public class ExecuteJob extends AbstractQuartzJob {

    public static final String DETAIL_TENANT_ID = "DataCleaner.tenant.id";
    public static final String DETAIL_JOB_NAME = "DataCleaner.job.name";

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        logger.debug("executeInternal({})", context);
        
        final ApplicationContext applicationContext = getApplicationContext(context);

        final Repository repository = applicationContext.getBean(Repository.class);
        final ConfigurationCache configurationCache = applicationContext.getBean(ConfigurationCache.class);

        final JobDetail jobDetail = context.getJobDetail();
        final String tenantId = jobDetail.getJobDataMap().getString(DETAIL_TENANT_ID);
        if (tenantId == null) {
            throw new IllegalArgumentException("No tenant id defined");
        }

        final String jobName = jobDetail.getJobDataMap().getString(DETAIL_JOB_NAME);
        if (jobName == null) {
            throw new IllegalArgumentException("No job path defined");
        }

        final Date fireTime = context.getFireTime();
        
        logger.info("Tenant {} executing job {}", tenantId, jobName);

        executeJob(tenantId, jobName, fireTime, repository, configurationCache);
    }

    public static void executeJob(String tenantId, String jobName, Date fireTime, Repository repository,
            ConfigurationCache configurationCache) {
        final AnalyzerBeansConfiguration configuration = configurationCache.getAnalyzerBeansConfiguration(tenantId);

        executeJob(tenantId, jobName, fireTime, repository, configuration);
    }

    /**
     * Executes a DataCleaner job in the repository and stores the result
     * 
     * @param tenantId
     *            the ID of the tenant
     * @param jobName
     *            the name of the job (note, NOT the path, and NOT including the
     *            .analysis.xml extension)
     * @param fireTime
     *            the time that the job got fired
     * @param repository
     *            the repository
     * @param configuration
     *            the configuration to use
     */
    public static void executeJob(String tenantId, String jobName, Date fireTime, Repository repository,
            AnalyzerBeansConfiguration configuration) {
        final RepositoryFolder tenantFolder = repository.getFolder(tenantId);
        final RepositoryFolder jobsFolder = tenantFolder.getFolder(TimelineServiceImpl.PATH_JOBS);
        final String jobFileName = jobName + FileFilters.ANALYSIS_XML.getExtension();
        final RepositoryFile jobFile = jobsFolder.getFile(jobFileName);
        if (jobFile == null) {
            throw new IllegalArgumentException("No such job file: " + jobFileName);

        }

        final MonitorJobReader jobReader = new MonitorJobReader(configuration, jobFile);
        final AnalysisJob job = jobReader.readJob();

        final AnalysisRunnerImpl runner = new AnalysisRunnerImpl(configuration);
        final AnalysisResultFuture resultFuture = runner.run(job);

        final RepositoryFolder resultFolder = tenantFolder.getFolder(TimelineServiceImpl.PATH_RESULTS);
        final String resultFilename = jobName + "-" + fireTime.getTime()
                + FileFilters.ANALYSIS_RESULT_SER.getExtension();

        resultFolder.createFile(resultFilename, new Action<OutputStream>() {
            @Override
            public void run(OutputStream out) throws Exception {
                final AnalysisResult result = new SimpleAnalysisResult(resultFuture.getResultMap());
                final ObjectOutputStream oos = new ObjectOutputStream(out);
                oos.writeObject(result);
            }
        });
    }
}
