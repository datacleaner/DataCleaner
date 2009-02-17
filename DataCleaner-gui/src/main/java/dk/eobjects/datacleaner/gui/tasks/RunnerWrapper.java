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

import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.datacleaner.execution.DataCleanerExecutor;

/**
 * Runs a runner task in the background thread.
 */
public class RunnerWrapper extends AbstractTaskRunner {

	@SuppressWarnings("unchecked")
	protected DataCleanerExecutor _runner;
	private DataContextSelection _dataContextSelection;

	@SuppressWarnings("unchecked")
	public RunnerWrapper(DataContextSelection schemaSelection,
			DataCleanerExecutor runner) {
		_dataContextSelection = schemaSelection;
		_runner = runner;
	}

	public void runTask() {
		_runner.execute(_dataContextSelection, false);
	}

	@Override
	protected void done() {
	}
}