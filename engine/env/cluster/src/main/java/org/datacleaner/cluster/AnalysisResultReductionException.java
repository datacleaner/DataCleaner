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

import java.util.Collection;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.AnalyzerResultReducer;
import org.datacleaner.job.AnalyzerJob;

/**
 * Exception raised when performing the reduction by a
 * {@link AnalyzerResultReducer} failed. This condition indicates that results
 * where succesfully produced by all slave nodes in a cluster, but the results
 * could not be combined/reduced without errors.
 */
public class AnalysisResultReductionException extends IllegalStateException {

    private final AnalyzerJob _analyzerJob;
    private final Collection<AnalyzerResult> _slaveResults;

    private static final long serialVersionUID = 1L;

    public AnalysisResultReductionException(AnalyzerJob analyzerJob, Collection<AnalyzerResult> slaveResults,
            Exception cause) {
        super(cause);
        _analyzerJob = analyzerJob;
        _slaveResults = slaveResults;
    }
    
    @Override
    public String getMessage() {
        return "Failed to reduce results for " + _analyzerJob + ": " + getCause().getMessage();
    }

    /**
     * Gets the {@link AnalyzerJob} for which the results pertained to.
     * 
     * @return
     */
    public AnalyzerJob getAnalyzerJob() {
        return _analyzerJob;
    }

    /**
     * Gets the individual results from the slaves in the cluster
     * 
     * @return
     */
    public Collection<AnalyzerResult> getSlaveResults() {
        return _slaveResults;
    }
}
