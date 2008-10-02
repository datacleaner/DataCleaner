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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.datacleaner.execution.IProgressObserver;
import dk.eobjects.datacleaner.execution.IRunner;
import dk.eobjects.datacleaner.gui.windows.LogResultWindow;
import dk.eobjects.metamodel.schema.Table;

/**
 * Runs a runner task in the background thread.
 */
public abstract class RunnerWrapper extends AbstractTaskRunner implements
		PropertyChangeListener, IProgressObserver {

	@SuppressWarnings("unchecked")
	protected IRunner _runner;
	private DataContextSelection _schemaSelection;
	private LogResultWindow _logInternalFrame;
	private int _tableCounter;
	private int _totalTables;
	private String _tableName;

	@SuppressWarnings("unchecked")
	public RunnerWrapper(DataContextSelection schemaSelection, IRunner runner,
			LogResultWindow logInternalFrame) {
		_schemaSelection = schemaSelection;
		_runner = runner;
		_logInternalFrame = logInternalFrame;
		addPropertyChangeListener(this);
	}

	public void runTask() {
		_runner.addProgressObserver(this);
		_runner.execute(_schemaSelection.getDataContext());
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			_logInternalFrame.getProgressBar().setValue(progress);
			_logInternalFrame.setStatusBarMessage("Analyzing: " + _tableName);
		}
	}

	@Override
	protected void done() {
		_logInternalFrame.setStatusBarMessage("Done.");
	}
	
	public void init(Object[] executingObjects) {
		_totalTables = executingObjects.length;
		_tableCounter = 0;
	}

	public void notifyExecutionBegin(Object executingObject) {
		_tableCounter++;
		if (executingObject instanceof Table) {
			_tableName = ((Table) executingObject).getName();
		}
	}

	public void notifyExecutionFailed(Object executingObject,
			Throwable throwable) {
	}

	public void notifyExecutionSuccess(Object executingObject) {
		int progress = Math.round(100 * _tableCounter / _totalTables);
		setProgress(progress);
	}
}