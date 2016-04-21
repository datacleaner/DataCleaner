package org.datacleaner.monitor.server.job;

import java.io.File;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.metamodel.util.HdfsResource;
import org.datacleaner.api.InputColumn;
import org.datacleaner.cluster.ClusterManager;
import org.datacleaner.cluster.DistributedAnalysisRunner;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.JaxbJobWriter;
import org.datacleaner.job.runner.AnalysisListener;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.monitor.cluster.ClusterManagerFactory;
import org.datacleaner.monitor.configuration.ResultContext;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.job.ExecutionLogger;
import org.datacleaner.monitor.job.MetricJobContext;
import org.datacleaner.monitor.job.MetricJobEngine;
import org.datacleaner.monitor.job.MetricValues;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.monitor.shared.model.MetricIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.spark.SparkRunner;
import org.datacleaner.spark.utils.HadoopJobExecutionUtils;
import org.datacleaner.spark.utils.HadoopUtils;
import org.datacleaner.spark.utils.ResultFilePathUtils;
import org.datacleaner.util.FileFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class HadoopDataCleanerJobEngine extends DataCleanerJobEngine {

    private static final Logger logger = LoggerFactory.getLogger(HadoopDataCleanerJobEngine.class);

    private final DescriptorProvider _descriptorProvider;
    private final Map<String, AnalysisResultFuture> _runningJobs;
    private final ApplicationContext _applicationContext;

    @Autowired
    public HadoopDataCleanerJobEngine(ClusterManagerFactory clusterManagerFactory, DescriptorProvider descriptorProvider,
            ApplicationContext applicationContext) {
        super(clusterManagerFactory, descriptorProvider, applicationContext);
        _descriptorProvider = descriptorProvider;
        _applicationContext = applicationContext;
        _runningJobs = new ConcurrentHashMap<String, AnalysisResultFuture>();
    }

    @Override
    public String getJobType() {
            return "HadoopDataCleanerAnalysisJob";
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

        final AnalysisRunner runner = new AnalysisRunnerImpl(configuration, analysisListener);
        final RepositoryFile jobFile = job.getJobFile();
        final String jobName = HadoopJobExecutionUtils.getUrlReadyJobName(getJobName());

        final String hadoopJobFileName = SparkRunner.DATACLEANER_TEMP_DIR + "/" + jobName + ".analysis.xml";
        final String hadoopJobResultFileName = SparkRunner.DEFAULT_RESULT_PATH + jobName + FileFilters.ANALYSIS_RESULT_SER.getExtension();
        final HdfsResource analysisJobResource = new HdfsResource(HadoopUtils.getFileSystem().getUri().resolve(
                hadoopJobFileName).toString());

        final OutputStream jobWriter = analysisJobResource.write();
        new JaxbJobWriter(configuration).write(analysisJob, jobWriter);
        jobWriter.close();

        final File configurationFile = HadoopJobExecutionUtils.createMinimalConfigurationFile(configuration, analysisJob);
        final SparkRunner sparkRunner = new SparkRunner(configurationFile.getAbsolutePath(), analysisJobResource
                .getFilepath(), hadoopJobResultFileName);
        
        sparkRunner.runJob(new SparkRunner.ProgressListener() {
            
            @Override
            public void onJobSubmitted() {
                
            }
            
            @Override
            public void onJobFilesReady() {
                
            }
        })
    }

    private String getJobName() {
     
        return null;
    }
    
}
