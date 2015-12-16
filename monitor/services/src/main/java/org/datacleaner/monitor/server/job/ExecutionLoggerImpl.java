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
package org.datacleaner.monitor.server.job;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.SerializationUtils;
import org.datacleaner.util.NoopAction;
import org.datacleaner.util.StringUtils;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.concurrent.PreviousErrorsExistException;
import org.datacleaner.monitor.events.JobExecutedEvent;
import org.datacleaner.monitor.events.JobFailedEvent;
import org.datacleaner.monitor.job.ExecutionLogger;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.monitor.scheduling.model.ExecutionStatus;
import org.datacleaner.monitor.server.jaxb.JaxbExecutionLogWriter;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryFileResource;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.result.AnalysisResult;
import org.datacleaner.result.save.AnalysisResultSaveHandler;
import org.datacleaner.util.FileFilters;
import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.Resource;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Default implementation of the {@link ExecutionLogger} interface. Writes log
 * to {@link FileFilters#ANALYSIS_EXECUTION_LOG_XML} files in the result folder
 * of the tenant.
 */
public class ExecutionLoggerImpl implements ExecutionLogger {

    private static final Logger logger = LoggerFactory.getLogger(ExecutionLoggerImpl.class);

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormat.forPattern("HH:mm:ss");

    private final ApplicationEventPublisher _eventPublisher;
    private final ExecutionLog _execution;
    private final StringBuilder _log;
    private final JaxbExecutionLogWriter _executionLogWriter;
    private final RepositoryFile _logFile;
    private final RepositoryFolder _resultFolder;
    private final AtomicBoolean _erronuous;

    public ExecutionLoggerImpl(ExecutionLog execution, RepositoryFolder resultFolder,
            ApplicationEventPublisher eventPublisher) {
        _execution = execution;
        _resultFolder = resultFolder;
        _eventPublisher = eventPublisher;
        _erronuous = new AtomicBoolean(false);
        _executionLogWriter = new JaxbExecutionLogWriter();

        _log = new StringBuilder();

        final String resultId = execution.getResultId();
        final String logFilename = resultId + FileFilters.ANALYSIS_EXECUTION_LOG_XML.getExtension();

        final RepositoryFile existingLogFile = resultFolder.getFile(logFilename);
        if (existingLogFile == null) {
            _logFile = resultFolder.createFile(logFilename, new Action<OutputStream>() {
                @Override
                public void run(OutputStream out) throws Exception {
                    _executionLogWriter.write(_execution, out);
                }
            });
        } else {
            _logFile = existingLogFile;
        }
    }

    @Override
    public void setStatusRunning() {
        _execution.setExecutionStatus(ExecutionStatus.RUNNING);
        if (_execution.getJobBeginDate() == null) {
            _execution.setJobBeginDate(new Date());
        }

        log("Job execution BEGIN");
    }

    @Override
    public void setStatusFailed(Object component, Object data, Throwable throwable) {
        final boolean erronuousBefore = _erronuous.getAndSet(true);
        if (erronuousBefore) {
            if (throwable instanceof PreviousErrorsExistException) {
                // don't report PreviousErrorsExistExceptions
                logger.error(
                        "More than one error was reported, but only the first will be put into the user-log. This error was also reported: "
                                + throwable.getMessage(), throwable);
            } else {
                log("\n - Additional exception stacktrace:", throwable);
                flushLog();
            }
        } else {
            _execution.setJobEndDate(new Date());
            _execution.setExecutionStatus(ExecutionStatus.FAILURE);

            final StringWriter stringWriter = new StringWriter();
            stringWriter.write("Job execution FAILURE");

            if (throwable != null && !StringUtils.isNullOrEmpty(throwable.getMessage())) {
                stringWriter.write("\n - ");
                stringWriter.write(throwable.getMessage());
                stringWriter.write(" (");
                stringWriter.write(throwable.getClass().getSimpleName());
                stringWriter.write(")");
            }

            if (component != null) {
                stringWriter.write('\n');
                stringWriter.write(" - Failure component: " + component);
            }

            if (data != null) {
                stringWriter.write('\n');
                stringWriter.write(" - Failure input data: " + data);
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

            if (_eventPublisher != null) {
                _eventPublisher.publishEvent(new JobFailedEvent(this, _execution, component, data, throwable));
            }
        }
    }

    @Override
    public void setStatusSuccess(Object result) {
        if (result == null) {
            _execution.setResultPersisted(false);
        } else if (result instanceof Serializable) {
            try {
                log("Saving job result.");
                serializeResult((Serializable) result);
                _execution.setResultPersisted(true);
            } catch (Exception e) {
                log("Failed to save job result! Execution of the job was succesfull, but the result was not persisted.");
                _execution.setResultPersisted(false);
                setStatusFailed(null, result, e);
                return;
            }
        } else {
            log("Job returned in non persistent result: " + result);
            _execution.setResultPersisted(true);
        }

        log("Job execution SUCCESS");
        _execution.setJobEndDate(new Date());
        _execution.setExecutionStatus(ExecutionStatus.SUCCESS);

        flushLog();

        if (_eventPublisher != null) {
            _eventPublisher.publishEvent(new JobExecutedEvent(this, _execution, result));
        }
    }

    private void serializeResult(final Serializable result) {
        final String resultFilename = _execution.getResultId() + FileFilters.ANALYSIS_RESULT_SER.getExtension();

        if (result instanceof AnalysisResult) {
            final RepositoryFile file = _resultFolder.createFile(resultFilename, new NoopAction<OutputStream>());
            final Resource resource = new RepositoryFileResource(file);
            final AnalysisResultSaveHandler analysisResultSaveHandler = new AnalysisResultSaveHandler((AnalysisResult) result, resource);
            try {
                analysisResultSaveHandler.saveOrThrow();
            } catch (SerializationException e) {
                // attempt to save what we can - and then rethrow
                final AnalysisResult safeAnalysisResult = analysisResultSaveHandler.createSafeAnalysisResult();
                if (safeAnalysisResult == null) {
                    logger.error("Serialization of result failed without any safe result elements to persist");
                } else {
                    final Map<ComponentJob, AnalyzerResult> unsafeResultElements = analysisResultSaveHandler.getUnsafeResultElements();
                    logger.error("Serialization of result failed with the following unsafe elements: {}", unsafeResultElements);
                    logger.warn("Partial AnalysisResult will be persisted to filename '{}'", resultFilename);
                    
                    analysisResultSaveHandler.saveWithoutUnsafeResultElements();
                }
                
                // rethrow the exception regardless
                throw e;
            }
        } else {
            _resultFolder.createFile(resultFilename, new Action<OutputStream>() {
                @Override
                public void run(OutputStream out) throws Exception {
                    SerializationUtils.serialize(result, out);
                }
            });
        }
    }

    @Override
    public void log(String message) {
        final String dateString = new LocalTime().toString(DATE_TIME_FORMAT);

        synchronized (_log) {
            if (_log.length() > 0) {
                _log.append('\n');
            }
            _log.append(dateString);
            _log.append(" - ");
            _log.append(message);
        }
    }

    @Override
    public void log(String message, Throwable throwable) {
        final StringWriter stringWriter = new StringWriter();
        if (message != null) {
            stringWriter.write(message);
            stringWriter.write('\n');
        }

        final PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        printWriter.flush();

        log(stringWriter.toString());
    }

    @Override
    public void flushLog() {
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

}
