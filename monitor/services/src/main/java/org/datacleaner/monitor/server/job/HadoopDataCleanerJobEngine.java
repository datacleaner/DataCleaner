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
package org.datacleaner.monitor.server.job;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.HdfsResource;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.JaxbJobWriter;
import org.datacleaner.job.runner.AnalysisJobMetrics;
import org.datacleaner.job.runner.AnalysisListener;
import org.datacleaner.monitor.cluster.ClusterManagerFactory;
import org.datacleaner.monitor.configuration.ResultContext;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.job.ExecutionLogger;
import org.datacleaner.monitor.job.MetricJobContext;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.MetricIdentifier;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.spark.SparkRunner;
import org.datacleaner.spark.utils.HadoopJobExecutionUtils;
import org.datacleaner.spark.utils.HadoopUtils;
import org.datacleaner.util.FileFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


public class HadoopDataCleanerJobEngine extends AbstractDataCleanerJobEngine {

    private static final Logger logger = LoggerFactory.getLogger(HadoopDataCleanerJobEngine.class);

    private final List<String> _runningJobs;

    @Autowired
    public HadoopDataCleanerJobEngine(
            DescriptorProvider descriptorProvider, ApplicationContext applicationContext) {
        super( descriptorProvider, applicationContext);
        _runningJobs = Collections.synchronizedList(new ArrayList<String>());
    }

    @Override
    public String getJobType() {
        return JobIdentifier.JOB_TYPE_ANALYSIS_JOB_HADOOP;
    }

    @Override
    protected DataCleanerJobContext getJobContext(TenantContext tenantContext, RepositoryFile file) {
        return new HadoopDataCleanerJobContextImpl(this, tenantContext, file);
    }
    
    @Override
    public void executeJob(TenantContext tenantContext, ExecutionLog execution, ExecutionLogger executionLogger,
            Map<String, String> variables) throws Exception {

        final AnalysisListener analysisListener = createAnalysisListener(execution, executionLogger);

        final DataCleanerJobContext job = (DataCleanerJobContext) tenantContext.getJob(execution.getJob());
        if (job == null) {
            throw new IllegalStateException("No such job: " + execution.getJob());
        }
        super.preLoadJob(tenantContext, job);

        final DataCleanerConfiguration configuration = tenantContext.getConfiguration();

        final AnalysisJob analysisJob = job.getAnalysisJob(variables);
        super.preExecuteJob(tenantContext, job, analysisJob);

        final String jobName = HadoopJobExecutionUtils.getUrlReadyJobName(execution.getResultId());
        final String hadoopJobFileName = SparkRunner.DATACLEANER_TEMP_DIR + "/" + jobName + ".analysis.xml";
        final String hadoopJobResultFileName = SparkRunner.DEFAULT_RESULT_PATH + jobName
                + FileFilters.ANALYSIS_RESULT_SER.getExtension();
        final HdfsResource analysisJobResource = new HdfsResource(HadoopUtils.getFileSystem().getUri().resolve(
                hadoopJobFileName).toString());

        final OutputStream jobWriter = analysisJobResource.write();
        new JaxbJobWriter(configuration).write(analysisJob, jobWriter);
        jobWriter.close();

        final File configurationFile = HadoopJobExecutionUtils.createMinimalConfigurationFile(configuration,
                analysisJob);
        final SparkRunner sparkRunner = new SparkRunner(configurationFile.getAbsolutePath(), analysisJobResource
                .getFilepath(), hadoopJobResultFileName);

        AnalysisJobMetrics analysisJobMetrics = null;

        sparkRunner.runJob(new SparkRunner.ProgressListener() {

            @Override
            public void onJobSubmitted() {
                putRunningJob(tenantContext, execution);
                analysisListener.jobBegin(analysisJob, null);
            }

            @Override
            public void onJobFilesReady() {
                removeRunningJob(tenantContext, execution);

                analysisListener.jobSuccess(analysisJob, analysisJobMetrics);

            }
        });
    }

    private void putRunningJob(TenantContext tenantContext, ExecutionLog execution) {
        if (tenantContext == null || execution == null) {
            return;
        }
        final String key = tenantContext.getTenantId() + "-" + execution.getResultId();
        _runningJobs.add(key);
    }

    private void removeRunningJob(TenantContext tenantContext, ExecutionLog execution) {
        if (tenantContext == null || execution == null) {
            return;
        }
        final String key = tenantContext.getTenantId() + "-" + execution.getResultId();
        _runningJobs.remove(key);
    }

    public boolean cancelJob(TenantContext tenantContext, ExecutionLog execution) {
        return false;
    }

    private void getResultFileFromCluster(ExecutionLogger executionLogger, String hadoopResultFileName,
            DataCleanerConfiguration configuration, String jobName) {
        HdfsResource resultsResource = null;
        try {
            resultsResource = new HdfsResource(HadoopUtils.getFileSystem().getUri().resolve(hadoopResultFileName)
                    .toString());
        } catch (IOException e) {
            executionLogger.log("Exception occurred when interacting with Hadoop Cluster.", e);
        }

        if (resultsResource != null && resultsResource.isExists()) {
            File resultsFolder = new File(configuration.getHomeFolder().toFile(), "results");
            final boolean created = resultsFolder.mkdir();
            if (created) {
                executionLogger.log("Results folder didn't exist, created");
            }

            File file = new File(resultsFolder, HadoopJobExecutionUtils.getUrlReadyJobName(jobName));

            FileHelper.copy(resultsResource, new FileResource(file));

        }

    }

  
}
