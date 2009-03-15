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

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.gui.dialogs.NewTaskDialog;
import dk.eobjects.datacleaner.gui.model.DatabaseDriver;
import dk.eobjects.datacleaner.gui.setup.GuiConfiguration;
import dk.eobjects.datacleaner.gui.setup.GuiSettings;
import dk.eobjects.datacleaner.gui.website.UserRegistrationDialog;
import dk.eobjects.datacleaner.gui.windows.MainWindow;

/**
 * This is the class with the main method for the DataCleaner Graphical
 * User-Interface (GUI)
 */
public class DataCleanerGui {

	public static final String VERSION = "1.5";
	public static final int EXIT_CODE_NORMAL_EXIT = 0;
	public static final int EXIT_CODE_COULD_NOT_OPEN_CONFIGURATION_FILE = 15;
	private static Log _log = LogFactory.getLog(DataCleanerGui.class);
	private static MainWindow _mainWindow;

	public static void main(String[] args) throws Exception {
		Thread.setDefaultUncaughtExceptionHandler(new GuiExceptionHandler());

		_log.info("DataCleaner-gui starting up.");

		GuiConfiguration.initialize();
		GuiSettings.initialize(true);

		loadDrivers();

		_mainWindow = new MainWindow();

		String username = GuiSettings.getSettings().getUsername();
		if (username == null) {
			new UserRegistrationDialog().setVisible(true);
		} else {
			new NewTaskDialog().setVisible(true);
		}
		GuiHelper.silentNotification("GUI: " + VERSION);
	}

	private static void loadDrivers() {
		List<DatabaseDriver> databaseDrivers = new LinkedList<DatabaseDriver>();
		databaseDrivers.addAll(GuiConfiguration.getDatabaseDrivers());
		databaseDrivers.addAll(GuiSettings.getSettings().getDatabaseDrivers());

		for (DatabaseDriver databaseDriver : databaseDrivers) {
			try {
				databaseDriver.loadDriver();
			} catch (Exception e) {
				_log.error("Could not load database driver: " + databaseDriver,
						e);
			}
		}
	}

	public static MainWindow getMainWindow() {
		return _mainWindow;
	}

	public static void setMainWindow(MainWindow window) {
		_mainWindow = window;
	}
}