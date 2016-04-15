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
package org.datacleaner.job.concurrent;

import java.util.concurrent.TimeUnit;

import org.datacleaner.job.tasks.Task;

/**
 * Extended TaskRunner for scheduled tasks.
 */
public interface ScheduledTaskRunner extends TaskRunner {

    /**
     * Submits a {@link Task} and a {@link TaskListener} to the
     * {@link TaskRunner}, that will schedule it.
     *
     * @param task
     * @param listener
     * @param initialDelay
     * @param delay
     * @param unit
     */
    void runScheduled(Task task, TaskListener listener, long initialDelay, long delay, TimeUnit unit);

    /**
     * Submits a {@link TaskRunnable} to the {@link ScheduledTaskRunner}, that will
     * schedule it.
     *
     * @param taskRunnable
     * @param initialDelay
     * @param delay
     * @param unit
     */
    void runScheduled(TaskRunnable taskRunnable, long initialDelay, long delay, TimeUnit unit);
}
