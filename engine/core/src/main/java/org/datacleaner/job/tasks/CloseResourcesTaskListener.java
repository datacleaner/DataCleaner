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
package org.datacleaner.job.tasks;

import java.io.Closeable;

import org.datacleaner.job.concurrent.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task listener that calls closing methods for any closeables.
 * 
 * 
 */
public class CloseResourcesTaskListener implements TaskListener {

	private static final Logger logger = LoggerFactory.getLogger(CloseResourcesTaskListener.class);
	
	private final Closeable[] _closeables;

	public CloseResourcesTaskListener(Closeable... closeables) {
		_closeables = closeables;
	}

	@Override
	public void onBegin(Task task) {
	}

	private void cleanup() {
		for (int i = 0; i < _closeables.length; i++) {
			try {
				_closeables[i].close();
			} catch (Exception e) {
				logger.error("Could not close resource: " + _closeables[i], e);
			}
		}
	}

	@Override
	public void onComplete(Task task) {
		cleanup();
	}

	@Override
	public void onError(Task task, Throwable throwable) {
		cleanup();
	}

}
