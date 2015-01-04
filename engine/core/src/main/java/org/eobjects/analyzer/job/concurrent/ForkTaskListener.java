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
package org.eobjects.analyzer.job.concurrent;

import java.util.Collection;

import org.eobjects.analyzer.job.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task listener that will fork into a new set of tasks, once it's predecessors
 * have ben completed.
 * 
 * 
 */
public final class ForkTaskListener implements TaskListener {

	private static final Logger logger = LoggerFactory.getLogger(ForkTaskListener.class);

	private final TaskRunner _taskRunner;
	private final Collection<TaskRunnable> _tasks;
	private final String _whatAreYouWaitingFor;

	/**
	 * Creates a new {@link ForkTaskListener}.
	 * 
	 * @param whatAreYouWaitingFor
	 *            a description of what the task is waiting for (used for
	 *            debugging and messaging)
	 * @param taskRunner
	 * @param tasksToSchedule
	 * @param executeOnErrors
	 *            defines whether the tasks should be executed/forked even if
	 *            previous errors have been encountered. Default value should be
	 *            false, but in some cases (like tasks that clean up resources)
	 *            this can be set to true.
	 */
	public ForkTaskListener(String whatAreYouWaitingFor, TaskRunner taskRunner, Collection<TaskRunnable> tasksToSchedule) {
		_whatAreYouWaitingFor = whatAreYouWaitingFor;
		_taskRunner = taskRunner;
		_tasks = tasksToSchedule;
	}

	@Override
	public void onComplete(Task task) {
		logger.info("onComplete({})", _whatAreYouWaitingFor);
		int index = 1;
		for (TaskRunnable tr : _tasks) {
			logger.info("Scheduling task {} out of {}: {}", new Object[] { index, _tasks.size(), tr });
			_taskRunner.run(tr);
			index++;
		}
	}

	public void onBegin(Task task) {
		// do nothing
	};

	@Override
	public void onError(Task task, Throwable throwable) {
		for (TaskRunnable tr : _tasks) {
			TaskListener listener = tr.getListener();
			if (listener == null) {
				logger.warn("TaskListener for {} was null", tr);
			} else {
				listener.onError(task, throwable);
			}
		}
	}
}
