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
package dk.eobjects.datacleaner.gui.widgets;

import java.io.PrintWriter;
import java.io.StringWriter;

import dk.eobjects.datacleaner.execution.IProgressObserver;
import dk.eobjects.datacleaner.gui.windows.LogResultWindow;

public class LogInternalFrameProgressObserver implements IProgressObserver {

	private LogResultWindow _logInternalFrame;

	public LogInternalFrameProgressObserver(LogResultWindow logInternalFrame) {
		_logInternalFrame = logInternalFrame;
	}

	public void init(Object[] executingObjects) {
	}

	public void notifyExecutionBegin(Object executingObject) {
		_logInternalFrame.addLogMessage("Execution begin: " + executingObject);
	}

	public void notifyExecutionSuccess(Object executingObject) {
		_logInternalFrame.addLogMessage("Execution end: " + executingObject);
	}

	public void notifyExecutionFailed(Object executingObject,
			Throwable throwable) {
		_logInternalFrame.addLogMessage("Execution failed:");
		StringWriter stringWriter = new StringWriter();
		throwable.printStackTrace(new PrintWriter(stringWriter));
		_logInternalFrame.addLogMessage(stringWriter.toString());
	}
}