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
package org.datacleaner.cluster.virtual;

import org.datacleaner.cluster.ClusterManager;
import org.datacleaner.cluster.DistributedJobContext;
import org.datacleaner.cluster.FixedDivisionsCountJobDivisionManager;
import org.datacleaner.cluster.JobDivisionManager;
import org.datacleaner.cluster.SlaveAnalysisRunner;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;

/**
 * A cluster manager which spawns virtual nodes, i.e. nodes that are not
 * actually on remote servers, but execute in the same JVM as the master node.
 */
public class VirtualClusterManager implements ClusterManager {

    private final DataCleanerConfiguration _configuration;
    private final int _nodeCount;

    public VirtualClusterManager(DataCleanerConfiguration configuration, int nodeCount) {
        _configuration = configuration;
        _nodeCount = nodeCount;
    }

    @Override
    public AnalysisResultFuture dispatchJob(AnalysisJob job, DistributedJobContext context) {
        AnalysisRunner runner = new SlaveAnalysisRunner(_configuration);
        return runner.run(job);
    }

    @Override
    public JobDivisionManager getJobDivisionManager() {
        return new FixedDivisionsCountJobDivisionManager(_nodeCount);
    }

}
