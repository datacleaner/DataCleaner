/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.monitor.scheduling.quartz;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.job.ExplorerJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.job.runner.AnalysisJobMetrics;
import org.eobjects.analyzer.job.runner.AnalysisListener;
import org.eobjects.analyzer.job.runner.AnalyzerMetrics;
import org.eobjects.analyzer.job.runner.ExplorerMetrics;
import org.eobjects.analyzer.job.runner.RowProcessingMetrics;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.SimpleAnalysisResult;
import org.eobjects.datacleaner.monitor.job.ExecutionLogger;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.Action;

/**
 * AnalysisListener for DataCleaner monitor. Picks up metrics and logging
 * statements about the job execution.
 */
public class MonitorAnalysisListener implements AnalysisListener {

    private final Map<ComponentJob, AnalyzerResult> _results;
    private final ExecutionLogger _executionLogger;
    private final String _resultFilename;
    private RepositoryFolder _resultFolder;

    public MonitorAnalysisListener(ExecutionLog execution, RepositoryFolder resultFolder,
            ExecutionLogger executionLogger) {
        _executionLogger = executionLogger;
        _resultFolder = resultFolder;

        _results = new ConcurrentHashMap<ComponentJob, AnalyzerResult>();
        final String resultId = execution.getResultId();
        _resultFilename = resultId + FileFilters.ANALYSIS_RESULT_SER.getExtension();
    }

    @Override
    public void jobBegin(AnalysisJob job, AnalysisJobMetrics metrics) {
        _executionLogger.setStatusRunning();
    }

    @Override
    public void jobSuccess(AnalysisJob job, AnalysisJobMetrics metrics) {

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
    private void jobFailed(ComponentJob componentJob, InputRow row, Throwable throwable) {
        _executionLogger.setStatusFailed(componentJob, row, throwable);
    }

    @Override
    public void rowProcessingBegin(AnalysisJob job, RowProcessingMetrics metrics) {
        final Table table = metrics.getTable();

        final StringBuilder sb = new StringBuilder();
        sb.append("Row processing of table " + table + " BEGIN");

        final Query query = metrics.getQuery();
        if (query != null) {
            sb.append('\n');
            sb.append(" - Query: ");
            sb.append(query.toSql());
        }

        final int expectedRows = metrics.getExpectedRows();
        if (expectedRows != -1) {
            sb.append('\n');
            sb.append(" - Expected row count: ");
            sb.append(expectedRows);
        }

        _executionLogger.log(sb.toString());
        _executionLogger.flushLog();
    }

    @Override
    public void rowProcessingProgress(AnalysisJob job, RowProcessingMetrics metrics, int currentRow) {
        if (currentRow > 0 && currentRow % 1000 == 0) {
            final Table table = metrics.getTable();
            _executionLogger.log("Row processing of table " + table + " progress: " + currentRow + " rows processed");
            _executionLogger.flushLog();
        }
    }

    @Override
    public void rowProcessingSuccess(AnalysisJob job, RowProcessingMetrics metrics) {
        final Table table = metrics.getTable();
        _executionLogger.log("Row processing of table " + table + " SUCCESS");
    }

    @Override
    public void analyzerBegin(AnalysisJob job, AnalyzerJob analyzerJob, AnalyzerMetrics metrics) {
    }

    @Override
    public void explorerBegin(AnalysisJob job, ExplorerJob explorerJob, ExplorerMetrics metrics) {
    }

    @Override
    public void analyzerSuccess(AnalysisJob job, AnalyzerJob analyzerJob, AnalyzerResult result) {
        _results.put(analyzerJob, result);
        _executionLogger.log("Result gathered from analyzer: " + analyzerJob);
        _executionLogger.flushLog();
    }

    @Override
    public void explorerSuccess(AnalysisJob job, ExplorerJob explorerJob, AnalyzerResult result) {
        _results.put(explorerJob, result);
        _executionLogger.log("Result gathered from explorer: " + explorerJob);
        _executionLogger.flushLog();
    }

    @Override
    public void errorInFilter(AnalysisJob job, FilterJob filterJob, InputRow row, Throwable throwable) {
        jobFailed(filterJob, row, throwable);
    }

    @Override
    public void errorInTransformer(AnalysisJob job, TransformerJob transformerJob, InputRow row, Throwable throwable) {
        jobFailed(transformerJob, row, throwable);
    }

    @Override
    public void errorInAnalyzer(AnalysisJob job, AnalyzerJob analyzerJob, InputRow row, Throwable throwable) {
        jobFailed(analyzerJob, row, throwable);
    }

    @Override
    public void errorInExplorer(AnalysisJob job, ExplorerJob explorerJob, Throwable throwable) {
        jobFailed(explorerJob, null, throwable);
    }

    @Override
    public void errorUknown(AnalysisJob job, Throwable throwable) {
        jobFailed(null, null, throwable);
    }
}
