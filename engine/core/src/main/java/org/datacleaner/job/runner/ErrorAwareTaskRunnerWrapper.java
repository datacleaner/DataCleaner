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
package org.datacleaner.job.runner;

import java.util.List;

import org.datacleaner.job.concurrent.PreviousErrorsExistException;
import org.datacleaner.job.concurrent.TaskListener;
import org.datacleaner.job.concurrent.TaskRunnable;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.job.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for the TaskRunner that only submits jobs when no errors have been
 * reported yet. This makes sure that a job will finish early if an error is
 * reported.
 */
final class ErrorAwareTaskRunnerWrapper implements TaskRunner, ErrorAware {
    
    private static final Logger logger = LoggerFactory.getLogger(ErrorAwareTaskRunnerWrapper.class);

	// a single shared exception is used if previous exceptions have been
	// reported. This is to make sure that the error message
	// ("A previous exception has occurred") will only be saved once.
	private static final PreviousErrorsExistException PREVIOUS_ERROR_EXCEPTION = new PreviousErrorsExistException(
			"A previous exception has occurred");

	private final TaskRunner _taskRunner;
	private final ErrorAware _errorAware;

	public ErrorAwareTaskRunnerWrapper(ErrorAware errorAware, TaskRunner taskRunner) {
		_taskRunner = taskRunner;
		_errorAware = errorAware;
	}

	@Override
	public void run(Task task, TaskListener taskListener) {
		if (isErrornous()) {
			taskListener.onError(task, PREVIOUS_ERROR_EXCEPTION);
		} else if (isCancelled()) {
		    logger.info("Ignoring task because job has been cancelled: {}", task);
		    taskListener.onError(task, PREVIOUS_ERROR_EXCEPTION);
		} else {
			_taskRunner.run(task, taskListener);
		}
	}

	@Override
	public void run(TaskRunnable taskRunnable) {
		run(taskRunnable.getTask(), taskRunnable.getListener());
	}

	@Override
	public void shutdown() {
		_taskRunner.shutdown();
	}

	@Override
	public boolean isErrornous() {
		return _errorAware.isErrornous();
	}

	@Override
	public List<Throwable> getErrors() {
		return _errorAware.getErrors();
	}

	@Override
	public boolean isCancelled() {
		return _errorAware.isCancelled();
	}

	@Override
	public void assistExecution() {
		if (!isErrornous() && !isCancelled()) {
			_taskRunner.assistExecution();
		}
	}
}
