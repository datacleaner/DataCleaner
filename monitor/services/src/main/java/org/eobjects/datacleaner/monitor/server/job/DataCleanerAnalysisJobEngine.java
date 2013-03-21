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
import java.util.Map;

import org.eobjects.analyzer.cluster.ClusterManager;
import org.eobjects.analyzer.cluster.DistributedAnalysisRunner;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.FileDatastore;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.NoSuchDatastoreException;
import org.eobjects.analyzer.job.runner.AnalysisListener;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.datacleaner.monitor.cluster.ClusterManagerFactory;
import org.eobjects.datacleaner.monitor.configuration.PlaceholderDatastore;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.job.ExecutionLogger;
import org.eobjects.datacleaner.monitor.job.JobEngine;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.scheduling.quartz.MonitorAnalysisListener;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The {@link JobEngine} implementation for DataCleaner .analysis.xml jobs.
 */
@Component
public class DataCleanerAnalysisJobEngine extends AbstractJobEngine<DataCleanerAnalysisJobContext> {

    private static final Logger logger = LoggerFactory.getLogger(DataCleanerAnalysisJobEngine.class);

    private final ClusterManagerFactory _clusterManagerFactory;

    @Autowired
    public DataCleanerAnalysisJobEngine(ClusterManagerFactory clusterManagerFactory) {
        super(FileFilters.ANALYSIS_XML.getExtension());
        _clusterManagerFactory = clusterManagerFactory;
    }

    @Override
    public String getJobType() {
        return "DataCleanerAnalysisJob";
    }

    @Override
    protected DataCleanerAnalysisJobContext getJobContext(TenantContext tenantContext, RepositoryFile file) {
        final DataCleanerAnalysisJobContext job = new DataCleanerAnalysisJobContextImpl(tenantContext, file);
        return job;
    }

    @Override
    public void executeJob(TenantContext context, ExecutionLog execution, ExecutionLogger executionLogger,
            Map<String, String> variables) throws Exception {
        final RepositoryFolder resultFolder = context.getResultFolder();

        final AnalysisListener analysisListener = new MonitorAnalysisListener(execution, resultFolder, executionLogger);

        final String jobName = execution.getJob().getName();

        final DataCleanerAnalysisJobContext job = (DataCleanerAnalysisJobContext) context.getJob(jobName);

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
            runner = new DistributedAnalysisRunner(configuration, clusterManager, analysisListener);
        }

        // fire and forget (the listener will do the rest)
        runner.run(analysisJob);
    }

    /**
     * Validates a job before loading it with a concrete datastore.
     * 
     * @param context
     * @param job
     * 
     * @throws FileNotFoundException
     */
    private void preLoadJob(TenantContext context, DataCleanerAnalysisJobContext job) throws FileNotFoundException {
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

    /**
     * Validates a job before executing it.
     * 
     * @param context
     * @param job
     * @param analysisJob
     * 
     * @throws NoSuchDatastoreException
     */
    private void preExecuteJob(TenantContext context, DataCleanerAnalysisJobContext job, AnalysisJob analysisJob)
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
}
