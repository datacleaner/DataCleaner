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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.datacleaner.cluster.ClusterManager;
import org.datacleaner.cluster.DistributedAnalysisRunner;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.runner.AnalysisListener;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.monitor.cluster.ClusterManagerFactory;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.job.ExecutionLogger;
import org.datacleaner.monitor.job.JobEngine;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.repository.RepositoryFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * The {@link JobEngine} implementation for DataCleaner .analysis.xml jobs.  
 */
@Component
public class DataCleanerJobEngine extends AbstractDataCleanerJobEngine {

    private static final Logger logger = LoggerFactory.getLogger(DataCleanerJobEngine.class);

    private final ClusterManagerFactory _clusterManagerFactory;
    private final Map<String, AnalysisResultFuture> _runningJobs;

    @Autowired
    public DataCleanerJobEngine(ClusterManagerFactory clusterManagerFactory, DescriptorProvider descriptorProvider,
            ApplicationContext applicationContext) {
        super(descriptorProvider, applicationContext); 
        _clusterManagerFactory = clusterManagerFactory;
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

    public String getJobType() {
        return JobIdentifier.JOB_TYPE_ANALYSIS_JOB;
    }

    @Override
    protected DataCleanerJobContext getJobContext(TenantContext tenantContext, RepositoryFile file) {
        final DataCleanerJobContext job = new DataCleanerJobContextImpl(this, tenantContext, file);
        return job;
    }

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

        preExecuteJob(tenantContext, job, analysisJob);

       
        runDataCleanerJobNormally(tenantContext, execution, executionLogger, analysisListener, configuration,
                analysisJob);
        
   
        
        
    }

    private void runDataCleanerJobNormally(TenantContext tenantContext, ExecutionLog execution,
            ExecutionLogger executionLogger, final AnalysisListener analysisListener,
            final DataCleanerConfiguration configuration, final AnalysisJob analysisJob) {
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



  

    
   
}
