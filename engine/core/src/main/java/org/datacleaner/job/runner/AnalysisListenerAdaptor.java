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
import org.datacleaner.api.ComponentMessage;
import org.datacleaner.api.InputRow;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.FilterJob;
import org.datacleaner.job.TransformerJob;

/**
 * An {@link AnalysisListener} that does nothing. Useful base-class for
 * implementing only particular methods of the {@link AnalysisListener}
 * interface.
 */
public class AnalysisListenerAdaptor implements AnalysisListener {

    @Override
    public void jobBegin(AnalysisJob job, AnalysisJobMetrics metrics) {
    }

    @Override
    public void jobSuccess(AnalysisJob job, AnalysisJobMetrics metrics) {
    }

    @Override
    public void rowProcessingBegin(AnalysisJob job, RowProcessingMetrics metrics) {
    }

    @Override
    public void rowProcessingProgress(AnalysisJob job, RowProcessingMetrics metrics, InputRow row, int currentRow) {
        rowProcessingProgress(job, metrics, currentRow);
    }

    @Override
    public void onComponentMessage(AnalysisJob job, ComponentJob componentJob, ComponentMessage message) {
        // do nothing
    }

    /**
     * 
     * @param job
     * @param metrics
     * @param currentRow
     * 
     * @deprecated use
     *             {@link #rowProcessingProgress(AnalysisJob, RowProcessingMetrics, InputRow, int)}
     *             instead.
     */
    @Deprecated
    protected void rowProcessingProgress(AnalysisJob job, RowProcessingMetrics metrics, int currentRow) {
    }

    @Override
    public void rowProcessingSuccess(AnalysisJob job, RowProcessingMetrics metrics) {
    }

    @Override
    public void componentBegin(AnalysisJob job, ComponentJob componentJob, ComponentMetrics metrics) {
    }

    @Override
    public void componentSuccess(AnalysisJob job, ComponentJob componentJob, AnalyzerResult result) {
    }

    @Override
    public void errorInComponent(AnalysisJob job, ComponentJob componentJob, InputRow row, Throwable throwable) {
        if (componentJob instanceof AnalyzerJob) {
            errorInAnalyzer(job, (AnalyzerJob) componentJob, row, throwable);
        } else if (componentJob instanceof TransformerJob) {
            errorInTransformer(job, (TransformerJob) componentJob, row, throwable);
        } else if (componentJob instanceof FilterJob) {
            errorInFilter(job, (FilterJob) componentJob, row, throwable);
        }
    }

    /**
     * 
     * @param job
     * @param filterJob
     * @param row
     * @param throwable
     * 
     * @deprecated use
     *             {@link #errorInComponent(AnalysisJob, ComponentJob, InputRow, Throwable)}
     *             instead
     */
    @Deprecated
    protected void errorInFilter(AnalysisJob job, FilterJob filterJob, InputRow row, Throwable throwable) {
    }

    /**
     * 
     * @param job
     * @param transformerJob
     * @param row
     * @param throwable
     * 
     * @deprecated use
     *             {@link #errorInComponent(AnalysisJob, ComponentJob, InputRow, Throwable)}
     *             instead
     */
    @Deprecated
    protected void errorInTransformer(AnalysisJob job, TransformerJob transformerJob, InputRow row, Throwable throwable) {
    }

    /**
     * 
     * @param job
     * @param analyzerJob
     * @param row
     * @param throwable
     * 
     * @deprecated use
     *             {@link #errorInComponent(AnalysisJob, ComponentJob, InputRow, Throwable)}
     *             instead
     */
    @Deprecated
    protected void errorInAnalyzer(AnalysisJob job, AnalyzerJob analyzerJob, InputRow row, Throwable throwable) {
    }

    @Override
    public void errorUnknown(AnalysisJob job, Throwable throwable) {
        errorUknown(job, throwable);
    }

    /**
     * Represents the previous typo in the method name of the errorUnknown
     * method.
     * 
     * @param job
     * @param throwable
     * 
     * @deprecated use {@link #errorUknown(AnalysisJob, Throwable)} (this method
     *             retained for forward-going compatibility).
     */
    @Deprecated
    public void errorUknown(AnalysisJob job, Throwable throwable) {
    }
}
