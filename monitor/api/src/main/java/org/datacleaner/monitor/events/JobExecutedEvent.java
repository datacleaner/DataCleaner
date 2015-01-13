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
package org.datacleaner.monitor.events;

import org.datacleaner.result.AnalysisResult;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.springframework.context.ApplicationEvent;

/**
 * Event raised when a job has been executed succesfully.
 */
public class JobExecutedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final ExecutionLog _executionLog;
    private final Object _result;

    public JobExecutedEvent(Object source, ExecutionLog executionLog, Object result) {
        super(source);
        _executionLog = executionLog;
        _result = result;
    }

    /**
     * Gets the result of the execution.
     * 
     * @return
     */
    public Object getResult() {
        return _result;
    }

    /**
     * @deprecated use {@link #getResult()} instead
     */
    @Deprecated
    public AnalysisResult getAnalysisResult() {
        return (AnalysisResult) _result;
    }

    public ExecutionLog getExecutionLog() {
        return _executionLog;
    }
}
