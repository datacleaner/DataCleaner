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
package org.eobjects.datacleaner.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DCExitActionListener implements ExitActionListener {

	private static final Logger logger = LoggerFactory.getLogger(DCExitActionListener.class);

	@Override
	public void exit(final int statusCode) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					// sleep for 5 seconds, to give the non-daemon threads a
					// chance to shut down the applications properly
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}

				if (logger.isWarnEnabled()) {
					logger.warn("Some threads are still running:");

					ThreadGroup topLevelThreadGroup = null;
					{
						ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
						while (threadGroup != null) {
							topLevelThreadGroup = threadGroup;
							threadGroup = threadGroup.getParent();
						}
					}
					printThreadInformation(topLevelThreadGroup, "");

					logger.warn("Invoking system.exit({})", statusCode);
				}

				System.exit(statusCode);
			}
		};
		thread.setDaemon(true);

		logger.info("Scheduling shutdown thread");
		thread.start();
	}

	public static void printThreadInformation(ThreadGroup threadGroup, String prefix) {
		logger.warn("Thread group: " + threadGroup);

		ThreadGroup[] groups = new ThreadGroup[threadGroup.activeGroupCount() + 2];
		int groupCount = threadGroup.enumerate(groups);
		for (int i = 0; i < groupCount; i++) {
			ThreadGroup group = groups[i];
			printThreadInformation(group, prefix + "  ");
		}

		Thread[] threads = new Thread[threadGroup.activeCount() + 4];
		int threadCount = threadGroup.enumerate(threads);

		for (int i = 0; i < threadCount; i++) {
			Thread thread = threads[i];
			if (thread != null) {
				boolean alive = thread.isAlive();
				boolean daemon = thread.isDaemon();
				logger.warn(prefix + "thread #" + (i + 1) + " (" + (alive ? "alive" : "dead") + (daemon ? ",daemon" : "")
						+ ")" + ": " + thread);
				if (alive && !daemon) {
					StackTraceElement[] stackTrace = thread.getStackTrace();
					for (int j = stackTrace.length - 1; j >= 0; j--) {
						logger.warn(prefix + " | stack " + (j + 1) + ": " + stackTrace[j]);
					}
				}
			}
		}
	}
}
