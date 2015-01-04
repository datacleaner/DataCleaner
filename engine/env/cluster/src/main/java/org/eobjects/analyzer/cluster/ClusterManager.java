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
package org.eobjects.analyzer.cluster;

import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;

/**
 * Defines an interface for a cluster and it's management.
 */
public interface ClusterManager {

    /**
     * Gets a {@link JobDivisionManager} object, used to advice the distribution
     * plan on how to divide work across the cluster.
     * 
     * @return
     */
    public JobDivisionManager getJobDivisionManager();

    /**
     * Dispatches a job for execution on a node. Typically this job will not be
     * the original job which the {@link DistributedAnalysisRunner} received,
     * but a "slave job" which represents a variant with processing thresholds
     * added to spread the load over multiple nodes.
     * 
     * @param job
     * @param context
     * @return
     * 
     * @throws Exception
     *             if dispatching the job failed, typically because of a
     *             transport error.
     */
    public AnalysisResultFuture dispatchJob(AnalysisJob job, DistributedJobContext context) throws Exception;

}
