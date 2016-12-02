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
    public void jobBegin(final AnalysisJob job, final AnalysisJobMetrics metrics) {
        logger.debug("jobBegin({})", job);
    }

    @Override
    public void jobSuccess(final AnalysisJob job, final AnalysisJobMetrics metrics) {
        logger.debug("jobSuccess({})", job);
    }

    @Override
    public void rowProcessingBegin(final AnalysisJob job, final RowProcessingMetrics metrics) {
        logger.debug("rowProcessingBegin({}, {})", new Object[] { job, metrics.getTable() });
    }

    @Override
    public void rowProcessingProgress(final AnalysisJob job, final RowProcessingMetrics metrics, final InputRow row,
            final int currentRow) {
        logger.debug("rowProcessingProgress({}, {}, {}, {})",
                new Object[] { job, metrics.getTable(), row, currentRow });
    }

    @Override
    public void rowProcessingSuccess(final AnalysisJob job, final RowProcessingMetrics metrics) {
        logger.debug("rowProcessingSuccess({}, {})", new Object[] { job, metrics.getTable() });
    }

    @Override
    public void componentBegin(final AnalysisJob job, final ComponentJob componentJob, final ComponentMetrics metrics) {
        logger.debug("componentBegin({}, {})", new Object[] { job, componentJob });
    }

    @Override
    public void componentSuccess(final AnalysisJob job, final ComponentJob componentJob, final AnalyzerResult result) {
        logger.debug("componentSuccess({}, {})", new Object[] { job, componentJob, result });
    }

    @Override
    public void errorInComponent(final AnalysisJob job, final ComponentJob componentJob, final InputRow row,
            final Throwable throwable) {
        logger.debug("errorInComponent(" + job + "," + componentJob + "," + row + ")", throwable);
    }

    @Override
    public void errorUknown(final AnalysisJob job, final Throwable throwable) {
        logger.debug("errorUknown(" + job + ")", throwable);
    }
}
