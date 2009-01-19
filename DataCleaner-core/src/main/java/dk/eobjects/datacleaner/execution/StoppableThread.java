/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.execution;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract thread providing the infrastructure to stop a thread safely at
 * acceptable places in execution (as opposed to using the stop() method which
 * kills the thread abruptly).
 * 
 * Subclasses of this class should query for execution status using the the
 * keepRunning() method at acceptable stop-condition places.
 */
public abstract class StoppableThread extends Thread {

	protected Log _log = LogFactory.getLog(getClass());

	public StoppableThread(ThreadGroup group, String name) {
		super(group, name);
	}

	public StoppableThread(String name) {
		super(name);
	}

	// used to stop thread execution
	private boolean _keepRunning = true;

	public void safeStop(boolean synchronize) {
		_log.info("Safe-stopping thread: " + getName());
		// Request that execution should stop
		_keepRunning = false;
		if (synchronize) {
			while (this.isAlive()) {
				// Wait for the last unstoppable chunk have been executed
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
			}
			_log.info("Safe-stopped thread: " + getName());
		}
	}

	/**
	 * Used by subclasses to ask whether or not to keep running
	 * 
	 * @return true if execution should continue, false if the thread should
	 *         stop execution
	 */
	protected boolean keepRunning() {
		return _keepRunning;
	}
}
