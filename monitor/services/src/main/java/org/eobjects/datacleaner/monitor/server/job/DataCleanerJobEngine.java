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
package org.eobjects.datacleaner.monitor.server.job;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.cluster.ClusterManager;
import org.eobjects.analyzer.cluster.DistributedAnalysisRunner;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.FileDatastore;
import org.eobjects.analyzer.connection.ResourceDatastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.descriptors.HasAnalyzerResultBeanDescriptor;
import org.eobjects.analyzer.descriptors.MetricDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.job.InputColumnSinkJob;
import org.eobjects.analyzer.job.NoSuchDatastoreException;
import org.eobjects.analyzer.job.runner.AnalysisListener;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.datacleaner.monitor.cluster.ClusterManagerFactory;
import org.eobjects.datacleaner.monitor.configuration.PlaceholderDatastore;
import org.eobjects.datacleaner.monitor.configuration.ResultContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.job.ExecutionLogger;
import org.eobjects.datacleaner.monitor.job.JobEngine;
import org.eobjects.datacleaner.monitor.job.MetricJobContext;
import org.eobjects.datacleaner.monitor.job.MetricJobEngine;
import org.eobjects.datacleaner.monitor.job.MetricValues;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.scheduling.quartz.MonitorAnalysisListener;
import org.eobjects.datacleaner.monitor.server.DefaultMetricValues;
import org.eobjects.datacleaner.monitor.server.MetricValueUtils;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.metamodel.util.Resource;
import org.eobjects.metamodel.util.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public DataCleanerJobEngine(ClusterManagerFactory clusterManagerFactory, DescriptorProvider descriptorProvider) {
        super(FileFilters.ANALYSIS_XML.getExtension());
        _clusterManagerFactory = clusterManagerFactory;
        _descriptorProvider = descriptorProvider;
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
        final DataCleanerJobContext dataCleanerJobContext = (DataCleanerJobContext) job;
        final AnalysisJob analysisJob = dataCleanerJobContext.getAnalysisJob();
        final AnalysisResult analysisResult = result.getAnalysisResult();
        return new DefaultMetricValues(this, job, metricIdentifiers, analysisResult, analysisJob);
    }

    @Override
    public void executeJob(TenantContext context, ExecutionLog execution, ExecutionLogger executionLogger,
            Map<String, String> variables) throws Exception {
        final RepositoryFolder resultFolder = context.getResultFolder();

        final AnalysisListener analysisListener = new MonitorAnalysisListener(execution, resultFolder, executionLogger);

        final DataCleanerJobContext job = (DataCleanerJobContext) context.getJob(execution.getJob());
        if (job == null) {
            throw new IllegalStateException("No such job: " + execution.getJob());
        }

        preLoadJob(context, job);

        final AnalyzerBeansConfiguration configuration = context.getConfiguration();

        final AnalysisJob analysisJob = job.getAnalysisJob(variables);

        preExecuteJob(context, job, analysisJob);

        final ClusterManager clusterManager;
        if (_clusterManagerFactory != null && execution.getSchedule().isDistributedExecution()) {
            final TenantIdentifier tenant = new TenantIdentifier(context.getTenantId());
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

        // await the completion of the job (to block concurrent execution by
        // Quartz).
        resultFuture.await();
    }

    /**
     * Validates a job before loading it with a concrete datastore.
     * 
     * @param context
     * @param job
     * 
     * @throws FileNotFoundException
     */
    private void preLoadJob(TenantContext context, DataCleanerJobContext job) throws FileNotFoundException,
            ResourceException {
        final String sourceDatastoreName = job.getSourceDatastoreName();
        final Datastore datastore = context.getConfiguration().getDatastoreCatalog().getDatastore(sourceDatastoreName);

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
        HasAnalyzerResultBeanDescriptor<?> componentDescriptor = _descriptorProvider
                .getAnalyzerBeanDescriptorByDisplayName(analyzerDescriptorName);

        if (componentDescriptor == null) {
            // in some cases we have results of components that are not
            // discovered by the descriptor provider. Although this is not
            // ideal, we will apply a work-around.
            logger.debug("Analyzer descriptor not found: {}. Continuing using the result file.", analyzerDescriptorName);
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
            componentDescriptor = (HasAnalyzerResultBeanDescriptor<?>) componentJob.getDescriptor();
            metricDescriptor = componentDescriptor.getResultMetric(metricDescriptorName);

            if (!metricDescriptor.isParameterizedByString()) {
                return null;
            }
            logger.debug("Component descriptor inferred as: {}", componentDescriptor);
        }

        final AnalyzerResult analyzerResult = metricValueUtils
                .getResult(analysisResult, componentJob, metricIdentifier);
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
}
