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
package dk.eobjects.datacleaner.gui.tasks;

import org.jdesktop.swingworker.SwingWorker;

/**
 * Abstract implementation for running tasks in the background. Task runners
 * that need to run in the background should extend this class.
 */
public abstract class AbstractTaskRunner extends SwingWorker<Void, Void>
		implements ITaskRunner {

	@Override
	protected Void doInBackground() throws Exception {
		runTask();
		return null;
	}
}