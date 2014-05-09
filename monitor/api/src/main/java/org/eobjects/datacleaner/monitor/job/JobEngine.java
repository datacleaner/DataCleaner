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
package org.eobjects.datacleaner.monitor.job;

import java.util.List;
import java.util.Map;

import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;

/**
 * A component responsible for discovery and execution of jobs of a particular
 * type.
 * 
 * @param <T>
 *            the type of Jobs (JobContexts) that are produced by this engine.
 */
public interface JobEngine<T extends JobContext> {

    /**
     * Gets the type string, such as 'DataCleanerAnalysisJob', 'CustomJob' or
     * other names of the job type. These string should not contain white space
     * or special signs.
     * 
     * @return
     */
    public String getJobType();

    /**
     * Gets all jobs of a particular tenant.
     * 
     * @param tenantContext
     * @return
     */
    public List<JobIdentifier> getJobs(TenantContext tenantContext);

    /**
     * Gets/produces a job context for a specific job.
     * 
     * @param job
     * @return
     */
    public T getJobContext(TenantContext tenantContext, JobIdentifier job);

    /**
     * Executes a job
     * 
     * @param tenantContext
     * @param execution
     * @param executionLogger
     * @param variables
     *            special variables/parameters/properties for this job execution
     * 
     * @throws Exception
     *             if something goes wrong while executing the job, in which
     *             case the job status will be set to FAILURE and the exception
     *             logged.
     */
    public void executeJob(TenantContext tenantContext, ExecutionLog execution, ExecutionLogger executionLogger,
            Map<String, String> variables) throws Exception;

    /**
     * Requests a cancellation of a running job.
     * 
     * @param tenantContext
     * @param executionLog
     *            the executionlog of a job that is assumed to be running and
     *            should be stopped.
     * @return true if the job was cancelled, false if it was not or if the
     *         state of the job is unknown, or if the {@link JobEngine} is not
     *         capable of cancelling the job.
     */
    public boolean cancelJob(TenantContext tenantContext, ExecutionLog executionLog);

    /**
     * Determines if a particular job is available within this job engine's jobs
     * 
     * @param tenantContext
     * @param jobName
     * @return
     */
    public boolean containsJob(TenantContext tenantContext, String jobName);
}
