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
package org.eobjects.datacleaner.monitor.scheduling.widgets;

import org.eobjects.datacleaner.monitor.scheduling.SchedulingServiceAsync;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;

/**
 * Helper class for polling updates to {@link ExecutionLog}s.
 */
public class ExecutionLogPoller {

    /**
     * Callback interface of the poller
     */
    public static interface Callback {
        public void updateExecutionLog(ExecutionLog executionLog);
    }

    private final SchedulingServiceAsync _service;
    private final TenantIdentifier _tenant;
    private final Callback _callback;

    public ExecutionLogPoller(SchedulingServiceAsync service, TenantIdentifier tenant, Callback callback) {
        _service = service;
        _tenant = tenant;
        _callback = callback;
    }

    public void start(final ExecutionLog executionLog) {
        if (executionLog != null && executionLog.isFinished()) {
            // already finished
            _callback.updateExecutionLog(executionLog);
            return;
        }
        schedulePoll(executionLog);
    }

    private void schedulePoll(final ExecutionLog executionLog) {
        if (executionLog == null || executionLog.isFinished()) {
            return;
        }

        new Timer() {
            @Override
            public void run() {
                _service.getExecution(_tenant, executionLog, new DCAsyncCallback<ExecutionLog>() {
                    @Override
                    public void onSuccess(ExecutionLog result) {
                        _callback.updateExecutionLog(result);
                        schedulePoll(result);
                    }

                    public void onFailure(Throwable e) {
                        GWT.log("Failed to get execution log, silently ignoring...", e);
                        schedulePoll(executionLog); // retry with previous log
                    };
                });
            }
        }.schedule(1000);
    }
}
