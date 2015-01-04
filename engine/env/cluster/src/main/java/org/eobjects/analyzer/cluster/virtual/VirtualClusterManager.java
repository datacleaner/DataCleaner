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
package org.eobjects.analyzer.cluster.virtual;

import org.eobjects.analyzer.cluster.ClusterManager;
import org.eobjects.analyzer.cluster.DistributedJobContext;
import org.eobjects.analyzer.cluster.FixedDivisionsCountJobDivisionManager;
import org.eobjects.analyzer.cluster.JobDivisionManager;
import org.eobjects.analyzer.cluster.SlaveAnalysisRunner;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunner;

/**
 * A cluster manager which spawns virtual nodes, i.e. nodes that are not
 * actually on remote servers, but execute in the same JVM as the master node.
 */
public class VirtualClusterManager implements ClusterManager {

    private final AnalyzerBeansConfiguration _configuration;
    private final int _nodeCount;

    public VirtualClusterManager(AnalyzerBeansConfiguration configuration, int nodeCount) {
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
