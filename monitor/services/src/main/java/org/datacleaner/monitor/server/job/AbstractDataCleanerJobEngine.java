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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.metamodel.util.Resource;
import org.apache.metamodel.util.ResourceException;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.FileDatastore;
import org.datacleaner.connection.ResourceDatastore;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.HasAnalyzerResultComponentDescriptor;
import org.datacleaner.descriptors.MetricDescriptor;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.InputColumnSinkJob;
import org.datacleaner.job.NoSuchDatastoreException;
import org.datacleaner.job.runner.AnalysisListener;
import org.datacleaner.job.runner.CompositeAnalysisListener;
import org.datacleaner.monitor.configuration.PlaceholderDatastore;
import org.datacleaner.monitor.configuration.ResultContext;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.job.ExecutionLogger;
import org.datacleaner.monitor.job.MetricJobContext;
import org.datacleaner.monitor.job.MetricJobEngine;
import org.datacleaner.monitor.job.MetricValues;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.monitor.scheduling.quartz.MonitorAnalysisListener;
import org.datacleaner.monitor.server.DefaultMetricValues;
import org.datacleaner.monitor.server.MetricValueUtils;
import org.datacleaner.monitor.shared.model.MetricIdentifier;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.result.AnalysisResult;
import org.datacleaner.util.FileFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public abstract class AbstractDataCleanerJobEngine extends AbstractJobEngine<DataCleanerJobContext> implements
MetricJobEngine<DataCleanerJobContext>{

    
    private static final Logger logger = LoggerFactory.getLogger(AbstractDataCleanerJobEngine.class);
    
    protected final DescriptorProvider _descriptorProvider;
    protected final ApplicationContext _applicationContext;
    
    public AbstractDataCleanerJobEngine(DescriptorProvider descriptorProvider,
            ApplicationContext applicationContext) {
        super(FileFilters.ANALYSIS_XML.getExtension());
        _descriptorProvider = descriptorProvider;
        _applicationContext = applicationContext;
    }

    @Override
    public abstract String getJobType(); 

    @Override
    public abstract void executeJob(TenantContext tenantContext, ExecutionLog execution, ExecutionLogger executionLogger,
            Map<String, String> variables) throws Exception;  

    @Override
    public abstract boolean cancelJob(TenantContext tenantContext, ExecutionLog executionLog); 


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
            componentDescriptor = (HasAnalyzerResultComponentDescriptor<?>) componentJob.getDescriptor();
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
    @Override
    protected abstract DataCleanerJobContext getJobContext(TenantContext tenantContext, RepositoryFile file); 

    /**
     * Validates a job before loading it with a concrete datastore.
     * 
     * @param context
     * @param job
     * 
     * @throws FileNotFoundException
     */
    protected void preLoadJob(TenantContext context, DataCleanerJobContext job) throws FileNotFoundException,
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
    protected void preExecuteJob(TenantContext context, DataCleanerJobContext job, AnalysisJob analysisJob)
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
    
    protected AnalysisListener createAnalysisListener(ExecutionLog execution, ExecutionLogger executionLogger) {
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
}
