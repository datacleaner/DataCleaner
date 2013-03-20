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
    private final Object _component;
    private final Object _data;
    private final Throwable _throwable;

    /**
     * Constructs a new {@link JobFailedEvent}.
     * 
     * @param source
     * @param executionLog
     *            the execution log of the triggered job
     * @param component
     *            the component that made job fail, if available, or else null.
     * @param data
     *            the data/record that made the job fail, if available, or else
     *            null.
     * @param throwable
     *            the exception thrown that made the job fail, if available, or
     *            null.
     */
    public JobFailedEvent(Object source, ExecutionLog executionLog, Object component, Object data, Throwable throwable) {
        super(source);
        _executionLog = executionLog;
        _component = component;
        _data = data;
        _throwable = throwable;
    }

    /**
     * Gets the execution log of the triggered job
     * 
     * @return the execution log of the triggered job
     */
    public ExecutionLog getExecutionLog() {
        return _executionLog;
    }

    /**
     * @deprecated use {@link #getComponent()} instead.
     */
    @Deprecated
    public ComponentJob getComponentJob() {
        return (ComponentJob) _component;
    }

    /**
     * Gets the component that made job fail, if available, or else null.
     * 
     * @return the component that made job fail, if available, or else null.
     */
    public Object getComponent() {
        return _component;
    }

    /**
     * @deprecated use {@link #getData()} instead
     */
    public InputRow getRow() {
        return (InputRow) _data;
    }

    /**
     * Gets the data/record that made the job fail, if available, or else null.
     * 
     * @return the data/record that made the job fail, if available, or else
     *         null.
     */
    public Object getData() {
        return _data;
    }

    /**
     * Gets the exception thrown that made the job fail, if available, or null.
     * 
     * @return the exception thrown that made the job fail, if available, or
     *         null.
     */
    public Throwable getThrowable() {
        return _throwable;
    }
}
