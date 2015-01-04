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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.concurrent.TaskListener;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.job.tasks.Task;

public final class RowConsumerTaskListener implements TaskListener {

	private final AtomicInteger _counter = new AtomicInteger();
	private final AtomicBoolean _errorsReported = new AtomicBoolean(false);
	private final AnalysisListener _analysisListener;
	private final AnalysisJob _analysisJob;
	private final TaskRunner _taskRunner;

	public RowConsumerTaskListener(AnalysisJob analysisJob, AnalysisListener analysisListener, TaskRunner taskRunner) {
		_analysisListener = analysisListener;
		_analysisJob = analysisJob;
		_taskRunner = taskRunner;
	}

	@Override
	public void onBegin(Task task) {
	}

	@Override
	public void onComplete(Task task) {
		incrementCounter();
	}

	@Override
	public void onError(Task task, Throwable throwable) {
		boolean alreadyRegisteredError = _errorsReported.getAndSet(true);
		if (!alreadyRegisteredError) {
			_analysisListener.errorUknown(_analysisJob, throwable);
		}

		incrementCounter();
	}

	private void incrementCounter() {
		_counter.incrementAndGet();
	}

	public boolean isErrornous() {
		return _errorsReported.get();
	}

	public void awaitTasks(final int numTasks) {
		while (numTasks > _counter.get() && !isErrornous()) {
			_taskRunner.assistExecution();
		}
	}
}
