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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a Runnable wrapper for a task. A task might have also a listener
 * which will be invoked based on the outcome of the task.
 */
public final class TaskRunnable implements Runnable {

	private final static Logger logger = LoggerFactory.getLogger(TaskRunnable.class);
	private final Task _task;
	private final TaskListener _listener;

	public TaskRunnable(Task task, TaskListener listener) {
		if (task == null && listener == null) {
			throw new IllegalArgumentException("both task and listener cannot be null");
		}
		_task = task;
		_listener = listener;
	}

	@Override
	public final void run() {
		if (_listener == null) {

			// execute without listener
			try {
				_task.execute();
			} catch (Throwable t) {
				logger.warn("No TaskListener to inform of error!", t);
			}

		} else {

			// execute with listener
			_listener.onBegin(_task);
			try {
				if (_task != null) {
					_task.execute();
				}
				_listener.onComplete(_task);
			} catch (Throwable t) {
				_listener.onError(_task, t);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		if (_task != null) {
			sb.append("task=" + _task);
			if (_listener != null) {
				sb.append(',');
			}
		}
		if (_listener != null) {
			sb.append("listener=" + _listener);
		}
		sb.append(']');
		return sb.toString();
	}

	/**
	 * Gets the task to run, or null if none exists
	 * 
	 * @return the task to run, or null if none exists
	 */
	public final Task getTask() {
		return _task;
	}

	/**
	 * Gets the task listener for the task, or null if none exists.
	 * 
	 * @return the task listener for the task, or null if none exists.
	 */
	public final TaskListener getListener() {
		return _listener;
	}
}
