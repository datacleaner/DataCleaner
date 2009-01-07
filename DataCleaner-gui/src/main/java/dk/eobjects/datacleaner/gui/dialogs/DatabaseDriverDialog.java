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
package dk.eobjects.datacleaner.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.swingx.action.OpenBrowserAction;

import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.model.DatabaseDriver;
import dk.eobjects.datacleaner.gui.model.ExtensionFilter;
import dk.eobjects.datacleaner.gui.setup.GuiSettings;
import dk.eobjects.datacleaner.gui.widgets.AutoCompleteComboBox;
import dk.eobjects.datacleaner.gui.windows.DownloadDialog;
import dk.eobjects.datacleaner.util.ReflectionHelper;

public class DatabaseDriverDialog extends BanneredDialog {

	private static final String POSTGRESQL_DRIVER = "org.postgresql.Driver";
	private static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
	private static final String JTDS_DRIVER = "net.sourceforge.jtds.jdbc.Driver";
	private static final String SQLITE_DRIVER = "org.sqlite.JDBC";
	private static final long serialVersionUID = -7450893693170647726L;
	private static final String[] COMMON_DRIVER_NAMES = { POSTGRESQL_DRIVER,
			"org.firebirdsql.jdbc.FBDriver",
			"org.apache.derby.jdbc.ClientDriver", "oracle.jdbc.OracleDriver",
			MYSQL_DRIVER, JTDS_DRIVER,
			"com.microsoft.sqlserver.jdbc.SQLServerDriver",
			"com.ingres.jdbc.IngresDriver", SQLITE_DRIVER };
	private static final Log _log = LogFactory
			.getLog(DatabaseDriverDialog.class);
	private JTextField _filenameField;
	private AutoCompleteComboBox _driverClassField;
	private File _file;

	private static ActionListener getFileActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				ExtensionFilter filter = new ExtensionFilter(
						"Java class archive (.jar)", "jar");
				fileChooser.setFileFilter(filter);
				GuiHelper.centerOnScreen(fileChooser);
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					DatabaseDriverDialog dialog = new DatabaseDriverDialog(file);
					dialog.setVisible(true);
				}
			}
		};
	}

	public static List<JMenuItem> getMenuItems() {
		List<JMenuItem> result = new ArrayList<JMenuItem>();
		JMenuItem fromFileItem = new JMenuItem("From JAR file", GuiHelper
				.getImageIcon("images/toolbar_jar_file.png"));
		fromFileItem.setMnemonic('d');
		fromFileItem.addActionListener(getFileActionListener());

		result.add(fromFileItem);
		result
				.add(downloadAndInstallItem(
						"MySQL",
						"http://mirrors.ibiblio.org/pub/mirrors/maven2/mysql/mysql-connector-java/5.1.6/mysql-connector-java-5.1.6.jar",
						MYSQL_DRIVER));
		result
				.add(downloadAndInstallItem(
						"PostgreSQL",
						"http://mirrors.ibiblio.org/pub/mirrors/maven2/postgresql/postgresql/8.3-603.jdbc4/postgresql-8.3-603.jdbc4.jar",
						POSTGRESQL_DRIVER));

		result
				.add(downloadAndInstallItem(
						"SQL Server",
						"http://mirrors.ibiblio.org/pub/mirrors/maven2/net/sourceforge/jtds/jtds/1.2.2/jtds-1.2.2.jar",
						JTDS_DRIVER));

		result
				.add(downloadAndInstallItem(
						"SQLite",
						"http://mirrors.ibiblio.org/pub/mirrors/maven2/org/xerial/sqlite-jdbc/3.6.7/sqlite-jdbc-3.6.7.jar",
						SQLITE_DRIVER));

		result
				.add(downloadItem("Oracle",
						"http://www.oracle.com/technology/software/tech/java/sqlj_jdbc"));
		result.add(downloadItem("Firebird",
				"http://www.firebirdsql.org/index.php?op=files&id=jaybird"));
		result
				.add(downloadItem("Ingres",
						"http://esd.ingres.com/product/Community_Projects/Drivers/java/JDBC"));
		return result;
	}

	public static JMenu getMenu() {
		JMenu menu = new JMenu("Register database driver");
		menu.setIcon(GuiHelper.getImageIcon("images/toolbar_database.png"));

		List<JMenuItem> menuItems = getMenuItems();
		for (JMenuItem menuItem : menuItems) {
			menu.add(menuItem);
		}

		return menu;
	}

	private static JMenuItem downloadItem(final String databaseName,
			final String url) {
		JMenuItem menuItem = new JMenuItem("Visit driver website: "
				+ databaseName, GuiHelper
				.getImageIcon("images/toolbar_visit_website.png"));
		try {
			menuItem.addActionListener(new OpenBrowserAction(url));
		} catch (MalformedURLException e) {
			_log.error(e);
		}
		return menuItem;
	}

	private static JMenuItem downloadAndInstallItem(final String databaseName,
			final String downloadUrl, final String driverClass) {
		JMenuItem menuItem = new JMenuItem("Download and install driver: "
				+ databaseName, GuiHelper
				.getImageIcon("images/toolbar_download.png"));
		menuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				final File file = new File(databaseName.toLowerCase().replace(
						' ', '_') + "-driver.jar");
				final DownloadDialog dialog = new DownloadDialog(downloadUrl,
						file);
				dialog.setCompleteAction(new ActionListener() {

					public void actionPerformed(ActionEvent event) {
						DatabaseDriver driver = new DatabaseDriver(file,
								driverClass);
						try {
							driver.loadDriver();
							GuiSettings settings = GuiSettings.getSettings();
							settings.getDatabaseDrivers().add(driver);
							GuiSettings.saveSettings(settings);
						} catch (Exception e) {
							GuiHelper
									.showErrorMessage(
											"Could not load driver",
											"An error occurred while loading the database driver",
											e);
						}
					}

				});
				dialog.setVisible(true);
				new Thread() {
					@Override
					public void run() {
						dialog.download();
					}
				}.start();
			}

		});

		return menuItem;
	}

	public DatabaseDriverDialog(File file) {
		super();

		JTextArea aboutDatabaseDrivers = GuiHelper.createLabelTextArea()
				.toComponent();
		aboutDatabaseDrivers
				.setText("Database drivers are java classes that enable programs to connect to a database. To add a database driver you have to obtain the driver file as well as the class name from you database vendor.");
		add(aboutDatabaseDrivers, BorderLayout.SOUTH);

		_file = file;
		_filenameField.setText(file.getAbsolutePath());
	}

	@Override
	protected Component getContent() {
		JPanel panel = GuiHelper.createPanel().toComponent();

		JLabel header = new JLabel("Database driver");
		header.setFont(GuiHelper.FONT_HEADER);
		GuiHelper.addToGridBag(header, panel, 0, 0, 2, 1);

		_filenameField = new JTextField(30);
		_filenameField.setEditable(false);

		GuiHelper.addToGridBag(new JLabel("Filename:"), panel, 0, 1);
		GuiHelper.addToGridBag(_filenameField, panel, 1, 1);

		GuiHelper.addToGridBag(new JLabel("Driver class:"), panel, 0, 2);

		String[] items = COMMON_DRIVER_NAMES;
		_driverClassField = new AutoCompleteComboBox(items);
		_driverClassField.setName("driverClassField");
		GuiHelper.addToGridBag(_driverClassField, panel, 1, 2);

		JButton saveButton = new JButton("Test and save database driver",
				GuiHelper.getImageIcon("images/toolbar_database.png"));
		saveButton.setName("saveButton");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String driverClass = _driverClassField.getSelectedItem()
						.toString().trim();
				DatabaseDriver databaseDriver = new DatabaseDriver(_file,
						driverClass);
				GuiSettings settings = GuiSettings.getSettings();
				Object[] driverClasses = ReflectionHelper.getProperties(
						settings.getDatabaseDrivers().toArray(), "driverClass");
				boolean unique = true;
				for (int i = 0; i < driverClasses.length && unique; i++) {
					if (driverClass.equals(driverClasses[i])) {
						unique = false;
						GuiHelper
								.showErrorMessage(
										"Driver class already registered",
										"The driver class '"
												+ driverClass
												+ "' is already registered in your database driver catalog.",
										null);
					}
				}

				if (unique) {
					try {
						databaseDriver.loadDriver();
						settings.getDatabaseDrivers().add(databaseDriver);
						GuiSettings.saveSettings(settings);
						dispose();
					} catch (Exception e) {
						GuiHelper
								.showErrorMessage(
										"Could not load driver",
										"An error occurred while loading the database driver",
										e);
					}
				}

			}
		});
		GuiHelper.addToGridBag(saveButton, panel, 1, 3);

		return panel;
	}

	@Override
	protected String getDialogTitle() {
		return "Database driver";
	}

}