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
package dk.eobjects.datacleaner.gui;

import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class catches all the uncaught exceptions in non-main threads. We use
 * this as a last means to log and report errors in DataCleaner.
 */
public class GuiExceptionHandler implements UncaughtExceptionHandler {

	private final static Log _log = LogFactory
			.getLog(GuiExceptionHandler.class);

	public void uncaughtException(Thread thread, Throwable throwable) {
		_log.fatal("Uncaught exception in thread: " + thread, throwable);
		GuiHelper.showErrorMessage("Unexpected exception",
				"An unexcepted exception occurred: " + throwable.getMessage(),
				throwable);
	}
}
