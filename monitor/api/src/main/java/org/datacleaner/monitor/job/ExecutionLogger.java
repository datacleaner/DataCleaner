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
package org.datacleaner.monitor.job;

/**
 * Represents a callback for progress of job execution, and log writer for the
 * user-visible log on the history and audit pages of DataCleaner.
 */
public interface ExecutionLogger {

    public void setStatusRunning();

    public void setStatusFailed(Object component, Object data, Throwable error);

    /**
     * Notifies the logger that the job has finished successfully, optionally
     * with a persistent result as payload.
     * 
     * @param result
     */
    public void setStatusSuccess(Object result);

    /**
     * Explicitly flushes the log to the file/user view. When log statements are
     * not explicitly flushed, the writer may decide to postpone flushing.
     * 
     * The methods {@link #setStatusRunning()},
     * {@link #setStatusSuccess(Object)} and {@link #setStatusFailed(Throwable)}
     * will automatically flush the logs, so no explicit flushes are needed for
     * these operations.
     */
    public void flushLog();

    public void log(String message);

    public void log(String message, Throwable throwable);
}
