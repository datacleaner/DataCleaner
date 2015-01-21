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
package org.datacleaner.job.runner;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputRow;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.ComponentJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AnalysisListener used for DEBUG level logging. This listener is obviously
 * very verbose.
 * 
 * 
 */
public class DebugLoggingAnalysisListener extends AnalysisListenerAdaptor {

    private static final Logger logger = LoggerFactory.getLogger(DebugLoggingAnalysisListener.class);

    /**
     * @return whether or not the debug logging level is enabled. Can be used to
     *         find out of it is even feasable to add this listener or not.
     */
    public static boolean isEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void jobBegin(AnalysisJob job, AnalysisJobMetrics metrics) {
        logger.debug("jobBegin({})", job);
    }

    @Override
    public void jobSuccess(AnalysisJob job, AnalysisJobMetrics metrics) {
        logger.debug("jobSuccess({})", job);
    }

    @Override
    public void rowProcessingBegin(AnalysisJob job, RowProcessingMetrics metrics) {
        logger.debug("rowProcessingBegin({}, {})", new Object[] { job, metrics.getTable() });
    }

    @Override
    public void rowProcessingProgress(AnalysisJob job, RowProcessingMetrics metrics, InputRow row, int currentRow) {
        logger.debug("rowProcessingProgress({}, {}, {}, {})", new Object[] { job, metrics.getTable(), row, currentRow });
    }

    @Override
    public void rowProcessingSuccess(AnalysisJob job, RowProcessingMetrics metrics) {
        logger.debug("rowProcessingSuccess({}, {})", new Object[] { job, metrics.getTable() });
    }

    @Override
    public void analyzerBegin(AnalysisJob job, AnalyzerJob analyzerJob, AnalyzerMetrics metrics) {
        logger.debug("analyzerBegin({}, {})", new Object[] { job, analyzerJob });
    }

    @Override
    public void analyzerSuccess(AnalysisJob job, AnalyzerJob analyzerJob, AnalyzerResult result) {
        logger.debug("analyzerSuccess({}, {})", new Object[] { job, analyzerJob, result });
    }

    @Override
    public void errorInComponent(AnalysisJob job, ComponentJob componentJob, InputRow row, Throwable throwable) {
        logger.debug("errorInComponent(" + job + "," + componentJob + "," + row + ")", throwable);
    }

    @Override
    public void errorUknown(AnalysisJob job, Throwable throwable) {
        logger.debug("errorUknown(" + job + ")", throwable);
    }
}
