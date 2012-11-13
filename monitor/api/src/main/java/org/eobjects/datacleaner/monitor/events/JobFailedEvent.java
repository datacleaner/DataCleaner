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
package org.eobjects.datacleaner.monitor.events;

import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.springframework.context.ApplicationEvent;

/**
 * Event raised when executing a {@link AnalysisJob} failed.
 */
public class JobFailedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final ExecutionLog _executionLog;
    private final ComponentJob _componentJob;
    private final InputRow _row;
    private final Throwable _throwable;

    /**
     * Constructs a new {@link JobFailedEvent}.
     * 
     * @param source
     * @param executionLog
     *            the execution log of the triggered job
     * @param componentJob
     *            the component that made job fail, if available, or else null.
     * @param row
     *            the record that made the job fail, if available, or else null.
     * @param throwable
     *            the exception thrown that made the job fail, if available, or
     *            null.
     */
    public JobFailedEvent(Object source, ExecutionLog executionLog, ComponentJob componentJob, InputRow row,
            Throwable throwable) {
        super(source);
        _executionLog = executionLog;
        _componentJob = componentJob;
        _row = row;
        _throwable = throwable;
    }

    public ExecutionLog getExecutionLog() {
        return _executionLog;
    }

    public ComponentJob getComponentJob() {
        return _componentJob;
    }

    public InputRow getRow() {
        return _row;
    }

    public Throwable getThrowable() {
        return _throwable;
    }
}
