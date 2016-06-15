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
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingWorker;

import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.HdfsResource;
import org.apache.metamodel.util.Resource;
import org.apache.metamodel.util.ResourceException;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.cluster.ClusterManager;
import org.datacleaner.cluster.DistributedAnalysisRunner;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.FileDatastore;
import org.datacleaner.connection.ResourceDatastore;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.HasAnalyzerResultComponentDescriptor;
import org.datacleaner.descriptors.MetricDescriptor;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.InputColumnSinkJob;
import org.datacleaner.job.JaxbJobWriter;
import org.datacleaner.job.NoSuchDatastoreException;
import org.datacleaner.job.runner.AnalysisListener;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.job.runner.CompositeAnalysisListener;
import org.datacleaner.monitor.cluster.ClusterManagerFactory;
import org.datacleaner.monitor.configuration.PlaceholderDatastore;
import org.datacleaner.monitor.configuration.ResultContext;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.job.ExecutionLogger;
import org.datacleaner.monitor.job.JobEngine;
import org.datacleaner.monitor.job.MetricJobContext;
import org.datacleaner.monitor.job.MetricJobEngine;
import org.datacleaner.monitor.job.MetricValues;
import org.datacleaner.monitor.scheduling.model.ExecutionIdentifier;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.monitor.scheduling.model.ExecutionStatus;
import org.datacleaner.monitor.scheduling.quartz.MonitorAnalysisListener;
import org.datacleaner.monitor.server.DefaultMetricValues;
import org.datacleaner.monitor.server.MetricValueUtils;
import org.datacleaner.monitor.shared.model.MetricIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.result.AnalysisResult;
import org.datacleaner.spark.SparkRunner;
import org.datacleaner.spark.utils.HadoopJobExecutionUtils;
import org.datacleaner.spark.utils.HadoopUtils;
import org.datacleaner.util.FileFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * The {@link JobEngine} implementation for DataCleaner .analysis.xml jobs.
 */
@Component
public class DataCleanerJobEngine extends AbstractJobEngine<DataCleanerJobContext> implements
        MetricJobEngine<DataCleanerJobContext> {

    private static final Logger logger = LoggerFactory.getLogger(DataCleanerJobEngine.class);

    private final ClusterManagerFactory _clusterManagerFactory;
    private final DescriptorProvider _descriptorProvider;
    private final Map<String, AnalysisResultFuture> _runningJobs;
    private final ApplicationContext _applicationContext;

    @Autowired
    public DataCleanerJobEngine(ClusterManagerFactory clusterManagerFactory, DescriptorProvider descriptorProvider,
            ApplicationContext applicationContext) {
        super(FileFilters.ANALYSIS_XML.getExtension());
        _clusterManagerFactory = clusterManagerFactory;
        _descriptorProvider = descriptorProvider;
        _applicationContext = applicationContext;
        _runningJobs = new ConcurrentHashMap<String, AnalysisResultFuture>();
    }

    /**
     * 
     * @param clusterManagerFactory
     * @param descriptorProvider
     * 
     * @deprecated use
     *             {@link #DataCleanerJobEngine(ClusterManagerFactory, DescriptorProvider, ApplicationContext)}
     *             instead
     */
    @Deprecated
    public DataCleanerJobEngine(ClusterManagerFactory clusterManagerFactory, DescriptorProvider descriptorProvider) {
        this(clusterManagerFactory, descriptorProvider, null);
    }

    @Override
    public String getJobType() {
        return "DataCleanerAnalysisJob";
    }

    @Override
    protected DataCleanerJobContext getJobContext(TenantContext tenantContext, RepositoryFile file) {
        final DataCleanerJobContext job = new DataCleanerJobContextImpl(this, tenantContext, file);
        return job;
    }

    @Override
    public MetricValues getMetricValues(MetricJobContext job, ResultContext result,
            List<MetricIdentifier> metricIdentifiers) {
        final AnalysisJob analysisJob;
        if (job == null) {
            analysisJob = null;
        } else {
            final DataCleanerJobContext dataCleanerJobContext = (DataCleanerJobContext) job;
            analysisJob = dataCleanerJobContext.getAnalysisJob();
        }
        final AnalysisResult analysisResult = result.getAnalysisResult();
        return new DefaultMetricValues(this, job, metricIdentifiers, analysisResult, analysisJob);
    }

    @Override
    public void executeJob(TenantContext tenantContext, ExecutionLog execution, ExecutionLogger executionLogger,
            Map<String, String> variables) throws Exception {

        final AnalysisListener analysisListener = createAnalysisListener(execution, executionLogger);

        final DataCleanerJobContext job = (DataCleanerJobContext) tenantContext.getJob(execution.getJob());
        if (job == null) {
            throw new IllegalStateException("No such job: " + execution.getJob());
        }

        final Map<String, String> overrideProperties = execution.getSchedule().getOverrideProperties();
        final DataCleanerConfiguration configuration = tenantContext.getConfiguration(overrideProperties);

        preLoadJob(configuration, job);

        final AnalysisJob analysisJob = job.getAnalysisJob(variables, overrideProperties);

        if (execution.getSchedule().isRunOnHadoop()) {
            runJobOnHadoop(configuration, execution, analysisJob, tenantContext, analysisListener, executionLogger,
                    execution);
        } else {
            runJobNormalMode(tenantContext, execution, executionLogger, analysisListener, job, configuration,
                    analysisJob);
        }
    }

    /**
     * Executes the job on the server in normal mode
     *
     */
    private void runJobNormalMode(TenantContext tenantContext, ExecutionLog execution, ExecutionLogger executionLogger,
            final AnalysisListener analysisListener, final DataCleanerJobContext job,
            final DataCleanerConfiguration configuration, final AnalysisJob analysisJob) {
        preExecuteJob(tenantContext, job, analysisJob);

        final ClusterManager clusterManager;
        if (_clusterManagerFactory != null && execution.getSchedule().isDistributedExecution()) {
            final TenantIdentifier tenant = new TenantIdentifier(tenantContext.getTenantId());
            clusterManager = _clusterManagerFactory.getClusterManager(tenant);
        } else {
            clusterManager = null;
        }

        final AnalysisRunner runner;
        if (clusterManager == null) {
            runner = new AnalysisRunnerImpl(configuration, analysisListener);
        } else {
            executionLogger.log("Partitioning and dispatching job to run in distributed mode.");
            runner = new DistributedAnalysisRunner(configuration, clusterManager, analysisListener);
        }

        // fire the job
        final AnalysisResultFuture resultFuture = runner.run(analysisJob);

        putRunningJob(tenantContext, execution, resultFuture);
        try {
            // await the completion of the job (to block concurrent execution by
            // Quartz).
            resultFuture.await();
        } finally {
            removeRunningJob(tenantContext, execution);
        }
    }

    /**
     * Sends the job on Hadoop for execution
     *
     */
    private void runJobOnHadoop(DataCleanerConfiguration configuration, ExecutionIdentifier executionIndentifier,
            AnalysisJob analysisJob, TenantContext tenantContext, AnalysisListener analysisListener,
            ExecutionLogger executionLogger, ExecutionLog execution) throws Exception {
        
        if (!HadoopJobExecutionUtils.isSparkHomeSet()) {
            final Exception exception = new Exception("Error while trying to run Hadoop Job. The environment variable SPARK_HOME is not set.");
            executionLogger.setStatusFailed(null, null, exception);
            return;
        }
      
        final Datastore datastore = analysisJob.getDatastore();
        if (!HadoopJobExecutionUtils.isValidSourceDatastore(datastore)) {
            final Exception exception = new Exception(
                    "Error while trying to run Hadoop Job. The datastore is not valid. Please check the configuration of the datastore. The encoding must be UTF-8 and multiline values must be false.");
            executionLogger.setStatusFailed(null, null, exception);
            return;
        }
        
        final String jobName = HadoopJobExecutionUtils.getUrlReadyJobName(executionIndentifier.getResultId());
        final String hadoopJobFileName = SparkRunner.DATACLEANER_TEMP_DIR + "/" + jobName + ".analysis.xml";
        final String hadoopJobResultFileName = SparkRunner.DEFAULT_RESULT_PATH + "/" + jobName + SparkRunner.RESULT_FILE_EXTENSION;
        final String uri = HadoopUtils.getFileSystem().getUri().resolve(hadoopJobFileName).toString();
        final HdfsResource analysisJobResource = new HdfsResource(uri);

        try {
            final OutputStream jobWriter = analysisJobResource.write();
            new JaxbJobWriter(configuration).write(analysisJob, jobWriter);
            jobWriter.close();

            final File configurationFile = HadoopJobExecutionUtils.createMinimalConfigurationFile(configuration,
                    analysisJob);
            final SparkRunner sparkRunner = new SparkRunner(configurationFile.getAbsolutePath(), analysisJobResource
                    .getFilepath(), hadoopJobResultFileName);

            SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
                @Override
                protected Integer doInBackground() throws Exception {

                    sparkRunner.runJob(new SparkRunner.ProgressListener() {

                        @Override
                        public void onJobFilesReady() {
                            executionLogger.log("Upload job definition to HDFS");
                            analysisListener.jobBegin(analysisJob, null);
                            executionLogger.flushLog();
                        }

                        @Override
                        public void onJobSubmitted() {
                            executionLogger.log("Submit job with Apache Spark");
                            executionLogger.log("Await job completition");
                            executionLogger.flushLog();
                        }

                    });

                    return 1;
                }

                @Override
                protected void done() {
                    executionLogger.log("Download job results");
                    // copy the file from Hadoop to the server
                    getResultFileFromCluster(tenantContext, executionLogger,hadoopJobResultFileName, jobName);
                    if (!execution.getExecutionStatus().equals(ExecutionStatus.FAILURE)) {
                        // the result is null so that there is no EMPTY result
                        // is persisted. The method will create an empty result file. We already copied one from Hadoop.
                        executionLogger.setStatusSuccess(null);
                        execution.setResultPersisted(true);
                        executionLogger.flushLog();
                    }
                }
            };

            worker.execute();
        } catch (Exception e) {
            // Exception occurred when interacting with Hadoop Cluster
            executionLogger.setStatusFailed(null, null, e);
            executionLogger.log("Job failed, please check Hadoop and/or DataCleaner monitor logs");
        }
        executionLogger.flushLog();
    }

    private AnalysisListener createAnalysisListener(ExecutionLog execution, ExecutionLogger executionLogger) {
        // we always want a MonitorAnalysisListener instance
        final AnalysisListener monitorAnalysisListener = new MonitorAnalysisListener(execution, executionLogger);

        // we might want to plug in additional AnalysisListeners
        final Map<String, AnalysisListener> analysisListeners = (_applicationContext == null ? null
                : _applicationContext.getBeansOfType(AnalysisListener.class));

        final AnalysisListener analysisListener;
        if (analysisListeners == null || analysisListeners.isEmpty()) {
            analysisListener = monitorAnalysisListener;
        } else {
            final AnalysisListener[] delegates = analysisListeners.values().toArray(
                    new AnalysisListener[analysisListeners.size()]);
            analysisListener = new CompositeAnalysisListener(monitorAnalysisListener, delegates);
        }
        return analysisListener;
    }

    @Override
    public boolean cancelJob(TenantContext tenantContext, ExecutionLog execution) {
        final AnalysisResultFuture resultFuture = getRunningJob(tenantContext, execution);
        if (resultFuture == null) {
            logger.info("cancelJob(...) invoked but job not found: {}, {}", tenantContext, execution);
            return false;
        }

        try {
            logger.info("Invoking cancel on job: {}, {}", tenantContext, execution);
            resultFuture.cancel();
            return true;
        } catch (Exception e) {
            logger.warn("Unexpected exception thrown while cancelling job: " + tenantContext + ", " + execution, e);
            return false;
        }
    }

    private void removeRunningJob(TenantContext tenantContext, ExecutionLog execution) {
        if (tenantContext == null || execution == null) {
            return;
        }
        final String key = tenantContext.getTenantId() + "-" + execution.getResultId();
        _runningJobs.remove(key);
    }

    private void putRunningJob(TenantContext tenantContext, ExecutionLog execution, AnalysisResultFuture resultFuture) {
        if (tenantContext == null || execution == null) {
            return;
        }
        final String key = tenantContext.getTenantId() + "-" + execution.getResultId();
        _runningJobs.put(key, resultFuture);
    }

    private AnalysisResultFuture getRunningJob(TenantContext tenantContext, ExecutionLog execution) {
        if (tenantContext == null || execution == null) {
            return null;
        }
        final String key = tenantContext.getTenantId() + "-" + execution.getResultId();
        return _runningJobs.get(key);
    }

    /**
     * Validates a job before loading it with a concrete datastore.
     * 
     * @param context
     * @param job
     * 
     * @throws FileNotFoundException
     */
    private void preLoadJob(DataCleanerConfiguration configuration, DataCleanerJobContext job) throws FileNotFoundException,
            ResourceException {
        final String sourceDatastoreName = job.getSourceDatastoreName();
        final Datastore datastore = configuration.getDatastoreCatalog().getDatastore(sourceDatastoreName);

        if (datastore instanceof ResourceDatastore) {
            Resource resource = ((ResourceDatastore) datastore).getResource();
            if (resource == null || !resource.isExists()) {
                logger.warn("Raising ResourceException from datastore: {}", datastore);
                throw new ResourceException(resource, "Resource does not exist: " + resource);
            }
        } else if (datastore instanceof FileDatastore) {
            final String filename = ((FileDatastore) datastore).getFilename();
            final File file = new File(filename);
            if (!file.exists()) {
                logger.warn("Raising FileNotFound exception from datastore: {}", datastore);
                throw new FileNotFoundException(filename);
            }
        }
    }

    /**
     * Validates a job before executing it.
     * 
     * @param context
     * @param job
     * @param analysisJob
     * 
     * @throws NoSuchDatastoreException
     */
    private void preExecuteJob(TenantContext context, DataCleanerJobContext job, AnalysisJob analysisJob)
            throws NoSuchDatastoreException {
        final Datastore datastore = analysisJob.getDatastore();

        if (datastore instanceof PlaceholderDatastore) {
            // the job was materialized using a placeholder datastore - ie.
            // the real datastore was not found!
            final String sourceDatastoreName = job.getSourceDatastoreName();
            logger.warn(
                    "Raising a NoSuchDatastoreException since a PlaceholderDatastore was found at execution time: {}",
                    sourceDatastoreName);
            throw new NoSuchDatastoreException(sourceDatastoreName);
        }
    }

    @Override
    public Collection<String> getMetricParameterSuggestions(MetricJobContext job, ResultContext result,
            MetricIdentifier metricIdentifier) {

        final String analyzerDescriptorName = metricIdentifier.getAnalyzerDescriptorName();
        final String metricDescriptorName = metricIdentifier.getMetricDescriptorName();

        final MetricValueUtils metricValueUtils = new MetricValueUtils();

        MetricDescriptor metricDescriptor = null;
        HasAnalyzerResultComponentDescriptor<?> componentDescriptor = _descriptorProvider
                .getAnalyzerDescriptorByDisplayName(analyzerDescriptorName);

        if (componentDescriptor == null) {
            // in some cases we have results of components that are not
            // discovered by the descriptor provider. Although this is not
            // ideal, we will apply a work-around.
            logger.debug("Analyzer descriptor not found: {}. Continuing using the result file.",
                    analyzerDescriptorName);
        } else {
            metricDescriptor = componentDescriptor.getResultMetric(metricDescriptorName);

            if (!metricDescriptor.isParameterizedByString()) {
                return null;
            }
        }

        final AnalysisResult analysisResult = result.getAnalysisResult();

        final AnalysisJob analysisJob = ((DataCleanerJobContext) job).getAnalysisJob();
        final ComponentJob componentJob = metricValueUtils.getComponentJob(metricIdentifier, analysisJob,
                analysisResult);

        if (componentDescriptor == null) {
            componentDescriptor = (HasAnalyzerResultComponentDescriptor<?>) componentJob.getDescriptor();
            metricDescriptor = componentDescriptor.getResultMetric(metricDescriptorName);

            if (!metricDescriptor.isParameterizedByString()) {
                return null;
            }
            logger.debug("Component descriptor inferred as: {}", componentDescriptor);
        }

        final AnalyzerResult analyzerResult = metricValueUtils.getResult(analysisResult, componentJob,
                metricIdentifier);
        final Collection<String> suggestions = metricDescriptor.getMetricParameterSuggestions(analyzerResult);

        // make sure we can send it across the GWT-RPC wire.
        if (suggestions instanceof ArrayList) {
            return suggestions;
        }
        return new ArrayList<String>(suggestions);
    }

    @Override
    public Collection<InputColumn<?>> getMetricParameterColumns(MetricJobContext job, ComponentJob component) {
        if (component instanceof InputColumnSinkJob) {
            final InputColumnSinkJob inputColumnSinkJob = (InputColumnSinkJob) component;
            final InputColumn<?>[] inputColumns = inputColumnSinkJob.getInput();
            return Arrays.asList(inputColumns);
        }
        return Collections.emptyList();
    }

    private void getResultFileFromCluster(TenantContext tenantContext, ExecutionLogger executionLogger, String hadoopResultFileName, String jobName) {
        HdfsResource resultsResource = null;
        try {
            resultsResource = new HdfsResource(HadoopUtils.getFileSystem().getUri().resolve(hadoopResultFileName)
                    .toString());
            if (resultsResource != null && resultsResource.isExists()) {
                final RepositoryFolder repositoryResultFolder = tenantContext.getResultFolder();

                final String fileName = HadoopJobExecutionUtils.getUrlReadyJobName(jobName)
                        + FileFilters.ANALYSIS_RESULT_SER.getExtension();
                final Resource resourceFile = repositoryResultFolder.createFile(fileName, null).toResource(); 
                
                logger.info("Writing the result to" + resourceFile.getQualifiedPath());
                FileHelper.copy(resultsResource, resourceFile);
            } else {
                final String message = "An error has occured while running the job. The result was not persisted on Hadoop. Please check Hadoop and/or DataCleaner logs";
                final Exception error = new Exception(message);
                executionLogger.setStatusFailed(null, null, error);
            }
        }
        catch (Exception e) {
            executionLogger.setStatusFailed(null, null, e);
        }
    }
}
