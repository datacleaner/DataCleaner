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
import org.datacleaner.api.ComponentContext;
import org.datacleaner.api.ComponentMessage;
import org.datacleaner.api.InputRow;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;

/**
 * Listener interface for analysis execution. Typically the user interface and
 * maybe also system services would implement this interface to be able to react
 * to progress notifications or errors occurring in the execution of the
 * analysis.
 *
 *
 */
public interface AnalysisListener {

    void jobBegin(AnalysisJob job, AnalysisJobMetrics metrics);

    void jobSuccess(AnalysisJob job, AnalysisJobMetrics metrics);

    /**
     * Notifies the listener that a row processing operation is about to begin.
     *
     * @param job
     *            the job that is being run
     * @param metrics
     *            metrics for the row processing operation
     */
    void rowProcessingBegin(AnalysisJob job, RowProcessingMetrics metrics);

    /**
     * Notifies the listener about progress in the row processing operation.
     *
     * @param job
     *            the job that is being run
     * @param metrics
     *            metrics for the row processing operation
     * @param row
     *            the {@link InputRow} that just finished processing
     * @param rowNumber
     *            the number of the row that just finished processing. This will
     *            start at 1 and continue typically to
     *            {@link RowProcessingMetrics#getExpectedRows()}.
     */
    void rowProcessingProgress(AnalysisJob job, RowProcessingMetrics metrics, InputRow row, int rowNumber);

    /**
     * Notifies the listener that a component published a message (using
     * {@link ComponentContext#publishMessage(ComponentMessage)}).
     *
     * @param job
     *            the job that is being run
     * @param componentJob
     *            the component that sent the message
     * @param message
     *            the message itself
     */
    void onComponentMessage(AnalysisJob job, ComponentJob componentJob, ComponentMessage message);

    /**
     * Notifies the listener that row processing has finished successfully.
     *
     * @param job
     * @param metrics
     */
    void rowProcessingSuccess(AnalysisJob job, RowProcessingMetrics metrics);

    /**
     * Notifies the listener that a component has begun processing
     *
     * @param job
     * @param componentJob
     * @param metrics
     */
    void componentBegin(AnalysisJob job, ComponentJob componentJob, ComponentMetrics metrics);

    /**
     * Notifies the listener that a component has finished processing
     *
     * @param job
     * @param componentJob
     * @param result
     *            the result of the component, if any
     */
    void componentSuccess(AnalysisJob job, ComponentJob componentJob, AnalyzerResult result);

    void errorInComponent(AnalysisJob job, ComponentJob componentJob, InputRow row, Throwable throwable);

    void errorUnknown(AnalysisJob job, Throwable throwable);
}
