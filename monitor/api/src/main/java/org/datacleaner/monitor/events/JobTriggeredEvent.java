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

import org.datacleaner.job.AnalysisJob;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.springframework.context.ApplicationEvent;

/**
 * Event raised when an {@link AnalysisJob} is triggered for execution
 */
public class JobTriggeredEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final ExecutionLog _executionLog;

    public JobTriggeredEvent(Object source, ExecutionLog executionLog) {
        super(source);
        _executionLog = executionLog;
    }

    public ExecutionLog getExecutionLog() {
        return _executionLog;
    }
}
