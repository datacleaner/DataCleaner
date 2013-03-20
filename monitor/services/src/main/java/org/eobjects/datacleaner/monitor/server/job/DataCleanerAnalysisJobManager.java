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
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.cluster.ClusterManager;
import org.eobjects.analyzer.cluster.DistributedAnalysisRunner;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.FileDatastore;
import org.eobjects.analyzer.descriptors.ComponentDescriptor;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.NoSuchDatastoreException;
import org.eobjects.analyzer.job.runner.AnalysisListener;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.lifecycle.LifeCycleHelper;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.datacleaner.monitor.cluster.ClusterManagerFactory;
import org.eobjects.datacleaner.monitor.configuration.JobContext;
import org.eobjects.datacleaner.monitor.configuration.PlaceholderDatastore;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.job.ExecutionLogger;
import org.eobjects.datacleaner.monitor.job.JobManager;
import org.eobjects.datacleaner.monitor.scheduling.api.VariableProvider;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.scheduling.model.VariableProviderDefinition;
import org.eobjects.datacleaner.monitor.scheduling.quartz.MonitorAnalysisListener;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.Func;
import org.eobjects.metamodel.util.HasNameMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@link JobManager} implementation for DataCleaner .analysis.xml jobs.
 */
@Component
public class DataCleanerAnalysisJobManager implements JobManager {

    private static final Logger logger = LoggerFactory.getLogger(DataCleanerAnalysisJobManager.class);

    private static final String EXTENSION_JOB = FileFilters.ANALYSIS_XML.getExtension();

    private final ClusterManagerFactory _clusterManagerFactory;

    @Autowired
    public DataCleanerAnalysisJobManager(ClusterManagerFactory clusterManagerFactory) {
        _clusterManagerFactory = clusterManagerFactory;
    }

    @Override
    public String getJobTypeDisplayName() {
        return "DataCleaner analysis job";
    }

    @Override
    public List<String> getJobNames(TenantContext tenantContext) {
        final RepositoryFolder jobsFolder = tenantContext.getJobFolder();
        final List<RepositoryFile> files = jobsFolder.getFiles(null, EXTENSION_JOB);
        final List<String> filenames = CollectionUtils.map(files, new HasNameMapper());
        final List<String> jobNames = CollectionUtils.map(filenames, new Func<String, String>() {
            @Override
            public String eval(String filename) {
                String jobName = filename.substring(0, filename.length() - EXTENSION_JOB.length());
                return jobName;
            }
        });
        return jobNames;
    }

    @Override
    public void executeJob(TenantContext context, ExecutionLog execution, ExecutionLogger executionLogger) {
        final RepositoryFolder resultFolder = context.getResultFolder();

        final AnalysisListener analysisListener = new MonitorAnalysisListener(execution, resultFolder, executionLogger);

        try {
            final String jobName = execution.getJob().getName();

            final JobContext job = context.getJob(jobName);

            preLoadJob(context, job);

            final AnalyzerBeansConfiguration configuration = context.getConfiguration();

            final VariableProviderDefinition variableProviderDef = execution.getSchedule().getVariableProvider();
            final Map<String, String> variableOverrides = overrideVariables(variableProviderDef, job, execution,
                    configuration);

            final AnalysisJob analysisJob = job.getAnalysisJob(variableOverrides);

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
                runner = new DistributedAnalysisRunner(configuration, clusterManager, analysisListener);
            }

            // fire and forget (the listener will do the rest)
            runner.run(analysisJob);

        } catch (Throwable e) {

            // only initialization issues are catched here, eg. failing to load
            // job or configuration. Other issues will be reported to the
            // listener by the runner.
            analysisListener.errorUknown(null, e);
        }
    }

    private static Map<String, String> overrideVariables(VariableProviderDefinition variableProviderDef,
            JobContext job, ExecutionLog execution, AnalyzerBeansConfiguration configuration)
            throws ClassNotFoundException {
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

    /**
     * Validates a job before loading it with a concrete datastore.
     * 
     * @param context
     * @param job
     * @throws FileNotFoundException
     */
    private void preLoadJob(TenantContext context, JobContext job) throws FileNotFoundException {
        final String sourceDatastoreName = job.getSourceDatastoreName();
        final Datastore datastore = context.getConfiguration().getDatastoreCatalog().getDatastore(sourceDatastoreName);

        if (datastore instanceof FileDatastore) {
            final String filename = ((FileDatastore) datastore).getFilename();
            final File file = new File(filename);
            if (!file.exists()) {
                logger.warn("Raising FileNotFound exception from datastore: {}", datastore);
                throw new FileNotFoundException(filename);
            }
        }
    }

    private void preExecuteJob(TenantContext context, JobContext job, AnalysisJob analysisJob) throws Exception {
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
}
