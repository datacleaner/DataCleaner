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
package org.datacleaner.util.filemonitor;

import java.io.File;
import java.nio.file.WatchService;

/**
 * The simplest (but also worst) implementation of the file monitor interface.
 * 
 * @deprecated use {@link WatchService} instead
 */
@Deprecated
final class PollingBasedFileMonitor implements FileMonitor {

	// most file systems only support file changes on a second level, so don't
	// bother to poll more often than a second
	private static final int POLL_THRESHOLD_MILLIS = 1000;

	private final File _file;
	private volatile long _lastModified;
	private volatile long _lastPolled;

	public PollingBasedFileMonitor(File file) {
		_file = file;
		_lastModified = _file.lastModified();
		_lastPolled = System.currentTimeMillis();
	}

	/**
	 * Polls the file system for a file change (synchronized)
	 * 
	 * @param currentTimeMillis
	 * 
	 * @return true if the file has changed
	 */
	private synchronized boolean poll(long currentTimeMillis) {
		long timeDiffMillis = currentTimeMillis - _lastPolled;
		if (timeDiffMillis > POLL_THRESHOLD_MILLIS) {
			final long lastModifiedBefore = _lastModified;
			_lastModified = _file.lastModified();
			_lastPolled = System.currentTimeMillis();

			return _lastModified != lastModifiedBefore;
		}
		return false;
	}

	@Override
	public boolean hasChanged() {
		long currentTimeMillis = System.currentTimeMillis();
		long timeDiffMillis = currentTimeMillis - _lastPolled;
		if (timeDiffMillis > POLL_THRESHOLD_MILLIS) {
			return poll(currentTimeMillis);
		}
		return false;
	}

}
