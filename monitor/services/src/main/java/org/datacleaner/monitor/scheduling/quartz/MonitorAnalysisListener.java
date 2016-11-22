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
package org.datacleaner.monitor.scheduling.quartz;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Table;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.ComponentMessage;
import org.datacleaner.api.ExecutionLogMessage;
import org.datacleaner.api.InputRow;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.FilterJob;
import org.datacleaner.job.TransformerJob;
import org.datacleaner.job.runner.AnalysisJobMetrics;
import org.datacleaner.job.runner.AnalysisListener;
import org.datacleaner.job.runner.AnalysisListenerAdaptor;
import org.datacleaner.job.runner.ComponentMetrics;
import org.datacleaner.job.runner.RowProcessingMetrics;
import org.datacleaner.monitor.job.ExecutionLogger;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.result.AnalysisResult;
import org.datacleaner.result.SimpleAnalysisResult;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.ProgressCounter;
import org.datacleaner.util.SystemProperties;

/**
 * AnalysisListener for DataCleaner monitor. Picks up metrics and logging
 * statements about the job execution.
 */
public class MonitorAnalysisListener extends AnalysisListenerAdaptor implements AnalysisListener {

    private final Map<ComponentJob, AnalyzerResult> _results;
    private final Map<Table, ProgressCounter> _progressCounters;
    private final ExecutionLogger _executionLogger;

    public MonitorAnalysisListener(final ExecutionLog execution, final ExecutionLogger executionLogger) {
        _executionLogger = executionLogger;
        _results = new ConcurrentHashMap<ComponentJob, AnalyzerResult>();
        _progressCounters = new ConcurrentHashMap<Table, ProgressCounter>();
    }

    @Override
    public void jobBegin(final AnalysisJob job, final AnalysisJobMetrics metrics) {
        _executionLogger.setStatusRunning();
    }

    @Override
    public void jobSuccess(final AnalysisJob job, final AnalysisJobMetrics metrics) {

        final AnalysisResult result = new SimpleAnalysisResult(_results);

        _executionLogger.setStatusSuccess(result);
    }

    /**
     * Dispatch method for all failure conditions. All parameters are optional,
     * because their availability depend on the failure condition.
     *
     * @param componentJob
     * @param row
     * @param throwable
     */
    private void jobFailed(final ComponentJob componentJob, final InputRow row, final Throwable throwable) {
        _executionLogger.setStatusFailed(componentJob, row, throwable);
    }

    @Override
    public void rowProcessingBegin(final AnalysisJob job, final RowProcessingMetrics metrics) {
        final Table table = metrics.getTable();

        _progressCounters.put(table, new ProgressCounter());

        final StringBuilder sb = new StringBuilder();
        sb.append("Starting processing of " + table.getName());

        final Query query = metrics.getQuery();
        if (query != null) {
            sb.append('\n');
            sb.append(" - Query: ");
            sb.append(query.toSql());
        }

        final String enableRowCount = System.getProperty(SystemProperties.MONITOR_LOG_ROWCOUNT);
        if (enableRowCount == null || !enableRowCount.equalsIgnoreCase("false")) {
            final int expectedRows = metrics.getExpectedRows();
            if (expectedRows != -1) {
                sb.append('\n');
                sb.append(" - Expected row count: ");
                sb.append(expectedRows);
            }
        }

        _executionLogger.log(sb.toString());
        _executionLogger.flushLog();
    }

    @Override
    public void rowProcessingProgress(final AnalysisJob job, final RowProcessingMetrics metrics, final int currentRow) {
        if (currentRow <= 0) {
            return;
        }

        final ProgressCounter progressCounter = _progressCounters.get(metrics.getTable());
        if (progressCounter == null) {
            return;
        }

        if (progressCounter.setIfSignificantToUser(currentRow)) {
            final Table table = metrics.getTable();
            _executionLogger.log("Progress of " + table.getName() + ": " + currentRow + " rows processed");
            _executionLogger.flushLog();
        }
    }

    @Override
    public void onComponentMessage(final AnalysisJob job, final ComponentJob componentJob, final ComponentMessage message) {
        if (message instanceof ExecutionLogMessage) {
            final String messageString = ((ExecutionLogMessage) message).getMessage();
            final String componentLabel = LabelUtils.getLabel(componentJob);
            _executionLogger.log(messageString + " (" + componentLabel + ")");
            _executionLogger.flushLog();
        }
    }

    @Override
    public void rowProcessingSuccess(final AnalysisJob job, final RowProcessingMetrics metrics) {
        final Table table = metrics.getTable();
        _executionLogger.log("Processing of " + table.getName() + " finished. Generating results ...");
    }

    @Override
    public void componentBegin(final AnalysisJob job, final ComponentJob componentJob, final ComponentMetrics metrics) {
    }

    @Override
    public void componentSuccess(final AnalysisJob job, final ComponentJob componentJob, final AnalyzerResult result) {
        if (result != null) {
            _results.put(componentJob, result);
            _executionLogger.log("Result gathered from analyzer: " + componentJob);
            _executionLogger.flushLog();
        }
    }

    @Override
    public void errorInFilter(final AnalysisJob job, final FilterJob filterJob, final InputRow row, final Throwable throwable) {
        jobFailed(filterJob, row, throwable);
    }

    @Override
    public void errorInTransformer(final AnalysisJob job, final TransformerJob transformerJob, final InputRow row, final Throwable throwable) {
        jobFailed(transformerJob, row, throwable);
    }

    @Override
    public void errorInAnalyzer(final AnalysisJob job, final AnalyzerJob analyzerJob, final InputRow row, final Throwable throwable) {
        jobFailed(analyzerJob, row, throwable);
    }

    @Override
    public void errorUknown(final AnalysisJob job, final Throwable throwable) {
        jobFailed(null, null, throwable);
    }
}
