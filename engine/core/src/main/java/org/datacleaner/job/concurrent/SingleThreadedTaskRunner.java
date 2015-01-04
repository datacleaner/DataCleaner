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

import org.datacleaner.job.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SingleThreadedTaskRunner implements TaskRunner {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void run(final Task task, final TaskListener listener) {
		logger.debug("run({},{})", task, listener);
		TaskRunnable taskRunnable = new TaskRunnable(task, listener);
		taskRunnable.run();
	}

	@Override
	public void run(TaskRunnable taskRunnable) {
		logger.debug("run({})", taskRunnable);
		taskRunnable.run();
	}

	@Override
	public void shutdown() {
		logger.info("shutdown() called, nothing to do");
	}
	
	@Override
	public void assistExecution() {
		// do nothing, not relevant as all tasks are already being taken care of
	}
}
