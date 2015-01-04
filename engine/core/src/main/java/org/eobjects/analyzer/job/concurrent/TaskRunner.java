/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.job.concurrent;

import org.eobjects.analyzer.job.tasks.Task;

/**
 * Interface for the execution engine in AnalyzerBeans. The {@link TaskRunner}
 * is responsible for executing tasks, typically of rather small sizes. A task
 * runner is an abstraction over such execution details as thread pools, timer
 * services and clustering environments.
 */
public interface TaskRunner {

    /**
     * Submits a {@link Task} and a {@link TaskListener} to the
     * {@link TaskRunner}, that will (eventually) execute it.
     * 
     * @param task
     * @param listener
     */
    public void run(Task task, TaskListener listener);

    /**
     * Submits a {@link TaskRunnable} to the {@link TaskRunner}, that will
     * (eventually) execute it.
     * 
     * @param taskRunnable
     */
    public void run(TaskRunnable taskRunnable);

    /**
     * Shuts down the {@link TaskRunner}, cleaning up allocated threads and
     * making it unusable for future use.
     */
    public void shutdown();

    /**
     * Offers to 'assist' the {@link TaskRunner} in executing tasks. This will
     * effectively 'steal work' from the task runner and do that work in the
     * current thread. Calling this method may be advantageous in cases where
     * the thread is anyways waiting for tasks in the {@link TaskRunner} to
     * complete.
     */
    public void assistExecution();
}
