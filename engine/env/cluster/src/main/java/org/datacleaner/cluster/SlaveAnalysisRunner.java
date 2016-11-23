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
package org.datacleaner.cluster;

import org.datacleaner.api.Close;
import org.datacleaner.api.Initialize;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.runner.AnalysisListener;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link AnalysisRunner} which is appropriate for use on slave nodes, since
 * it will honor non-distributed tasks like {@link Initialize} and {@link Close}
 * methods that are marked with distributed=false.
 */
public class SlaveAnalysisRunner extends AnalysisRunnerImpl {

    private static final Logger logger = LoggerFactory.getLogger(SlaveAnalysisRunner.class);

    public SlaveAnalysisRunner(final DataCleanerConfiguration configuration) {
        super(configuration);
    }

    public SlaveAnalysisRunner(final DataCleanerConfiguration configuration,
            final AnalysisListener... sharedAnalysisListeners) {
        super(configuration, sharedAnalysisListeners);
    }

    @Override
    public AnalysisResultFuture run(final AnalysisJob job) {
        logger.info("Running slave job: {}", job);
        return super.run(job);
    }

    @Override
    protected boolean isNonDistributedTasksIncluded() {
        return false;
    }
}
