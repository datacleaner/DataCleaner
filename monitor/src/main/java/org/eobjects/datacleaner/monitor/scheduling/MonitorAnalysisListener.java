/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.monitor.scheduling;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionStatus;
import org.eobjects.datacleaner.monitor.server.JaxbExecutionLogWriter;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.Action;

/**
 * AnalysisListener for DataCleaner dq monitor. Picks up metrics and logging
 * statements about the job execution.
 */
public class MonitorAnalysisListener implements AnalysisListener {

    private final ExecutionLog _execution;
    private final StringBuilder _log;
    private final RepositoryFolder _resultFolder;
    private final Map<ComponentJob, AnalyzerResult> _results;
    private final String _resultFilename;
    private final RepositoryFile _logFile;
    private final JaxbExecutionLogWriter _executionLogWriter;

    public MonitorAnalysisListener(ExecutionLog execution, RepositoryFolder resultFolder, String resultName) {
        _execution = execution;
        _resultFolder = resultFolder;
        _executionLogWriter = new JaxbExecutionLogWriter();
        _resultFilename = resultName + FileFilters.ANALYSIS_RESULT_SER.getExtension();
        _results = new ConcurrentHashMap<ComponentJob, AnalyzerResult>();
        _log = new StringBuilder();

        final String logFilename = resultName + FileFilters.ANALYSIS_EXECUTION_LOG_XML.getExtension();
        _logFile = resultFolder.createFile(logFilename, new Action<OutputStream>() {
            @Override
            public void run(OutputStream out) throws Exception {
                _executionLogWriter.write(_execution, out);
            }
        });
    }

    @Override
    public void jobBegin(AnalysisJob job, AnalysisJobMetrics metrics) {
        _execution.setExecutionStatus(ExecutionStatus.RUNNING);
        _execution.setJobBeginDate(new Date());

        log("Job execution BEGIN", false);
    }

    @Override
    public void jobSuccess(AnalysisJob job, AnalysisJobMetrics metrics) {

        writeResult();

        log("Job execution SUCCESS");
        _execution.setJobEndDate(new Date());
        _execution.setExecutionStatus(ExecutionStatus.SUCCESS);
        
        flushLog();
    }

    private void writeResult() {
        _resultFolder.createFile(_resultFilename, new Action<OutputStream>() {
            @Override
            public void run(OutputStream out) throws Exception {
                final AnalysisResult result = new SimpleAnalysisResult(_results);
                final ObjectOutputStream oos = new ObjectOutputStream(out);
                oos.writeObject(result);
            }
        });
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
        _execution.setJobEndDate(new Date());
        _execution.setExecutionStatus(ExecutionStatus.FAILURE);

        final StringWriter stringWriter = new StringWriter();
        stringWriter.write("Job execution FAILURE");

        if (componentJob != null) {
            stringWriter.write('\n');
            stringWriter.write(" - Failure component: " + componentJob);
        }

        if (row != null) {
            stringWriter.write('\n');
            stringWriter.write(" - Failure row: " + row);
        }

        if (throwable != null) {
            stringWriter.write('\n');
            stringWriter.write(" - Exception stacktrace of failure condition:");
            stringWriter.write('\n');
            final PrintWriter printWriter = new PrintWriter(stringWriter);
            throwable.printStackTrace(printWriter);
            printWriter.flush();
        }

        stringWriter.write("\nCheck the server logs for more details, warnings and debug information.");

        log(stringWriter.toString());
        flushLog();
    }

    private void flushLog() {
        _execution.setLogOutput(_log.toString());

        _logFile.writeFile(new Action<OutputStream>() {
            @Override
            public void run(OutputStream out) throws Exception {
                // synchronize while writing
                synchronized (_log) {
                    _executionLogWriter.write(_execution, out);
                }
            }
        });
    }

    /**
     * Logs a message to the {@link HistoricExecution}'s log output
     * 
     * @param message
     */
    private void log(String message) {
        log(message, true);
    }

    /**
     * Logs a message to the {@link HistoricExecution}'s log output
     * 
     * @param message
     * @param newline
     *            whether or not to insert a newline in the beginning of the
     *            message
     */
    private void log(final String message, final boolean newline) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String dateString = dateFormat.format(new Date());

        synchronized (_log) {
            if (newline) {
                _log.append('\n');
            }
            _log.append(dateString);
            _log.append(" - ");
            _log.append(message);
        }
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

        log(sb.toString());
        flushLog();
    }

    @Override
    public void rowProcessingProgress(AnalysisJob job, RowProcessingMetrics metrics, int currentRow) {
        if (currentRow > 0 && currentRow % 1000 == 0) {
            final Table table = metrics.getTable();
            log("Row processing of table " + table + " progress: " + currentRow + " rows processed");
            flushLog();
        }
    }

    @Override
    public void rowProcessingSuccess(AnalysisJob job, RowProcessingMetrics metrics) {
        final Table table = metrics.getTable();
        log("Row processing of table " + table + " SUCCESS");
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
        log("Result gathered from analyzer: " + analyzerJob);
        flushLog();
    }

    @Override
    public void explorerSuccess(AnalysisJob job, ExplorerJob explorerJob, AnalyzerResult result) {
        _results.put(explorerJob, result);
        log("Result gathered from explorer: " + explorerJob);
        flushLog();
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
