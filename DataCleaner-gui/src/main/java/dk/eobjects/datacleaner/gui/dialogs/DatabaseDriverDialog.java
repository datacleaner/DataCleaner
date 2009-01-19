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

	private static final long serialVersionUID = -7450893693170647726L;

	public static final String DB2_DRIVER = "com.ibm.db2.jdbc.app.DB2Driver";
	public static final String INGRES_DRIVER = "com.ingres.jdbc.IngresDriver";
	public static final String FIREBIRD_DRIVER = "org.firebirdsql.jdbc.FBDriver";
	public static final String SAPDB_DRIVER = "com.sap.dbtech.jdbc.DriverSapDB";
	public static final String POSTGRESQL_DRIVER = "org.postgresql.Driver";
	public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
	public static final String JTDS_DRIVER = "net.sourceforge.jtds.jdbc.Driver";
	public static final String SQLITE_DRIVER = "org.sqlite.JDBC";
	public static final String DERBY_DRIVER = "org.apache.derby.jdbc.ClientDriver";
	public static final String ORACLE_DRIVER = "oracle.jdbc.OracleDriver";
	public static final String JDBC4OLAP_DRIVER = "org.jdbc4olap.jdbc.OlapDriver";
	public static final String HSQLDB_DRIVER = "org.hsqldb.jdbcDriver";
	public static final String SQLSERVER_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	public static final String ODBC_BRIDGE_DRIVER = "sun.jdbc.odbc.JdbcOdbcDriver";

	private static final String[] COMMON_DRIVER_NAMES = { POSTGRESQL_DRIVER,
			FIREBIRD_DRIVER, DERBY_DRIVER, ORACLE_DRIVER, MYSQL_DRIVER,
			JTDS_DRIVER, SQLSERVER_DRIVER, INGRES_DRIVER, SQLITE_DRIVER,
			DB2_DRIVER, SAPDB_DRIVER, JDBC4OLAP_DRIVER };
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

		JMenu automaticMenu = new JMenu("Automatic download and install");
		automaticMenu.setIcon(GuiHelper
				.getImageIcon("images/toolbar_download.png"));
		result.add(automaticMenu);

		JMenu websiteMenu = new JMenu("Visit driver website");
		websiteMenu.setIcon(GuiHelper
				.getImageIcon("images/toolbar_visit_website.png"));
		result.add(websiteMenu);

		JMenuItem fromFileItem = new JMenuItem("Local JAR file ...", GuiHelper
				.getImageIcon("images/toolbar_jar_file.png"));
		fromFileItem.setMnemonic('d');
		fromFileItem.addActionListener(getFileActionListener());
		result.add(fromFileItem);

		automaticMenu
				.add(downloadAndInstallItem(
						"MySQL",
						"http://mirrors.ibiblio.org/pub/mirrors/maven2/mysql/mysql-connector-java/5.1.6/mysql-connector-java-5.1.6.jar",
						MYSQL_DRIVER, "images/database_mysql.png"));
		automaticMenu
				.add(downloadAndInstallItem(
						"PostgreSQL",
						"http://mirrors.ibiblio.org/pub/mirrors/maven2/postgresql/postgresql/8.3-603.jdbc4/postgresql-8.3-603.jdbc4.jar",
						POSTGRESQL_DRIVER, "images/database_postgresql.png"));
		automaticMenu
				.add(downloadAndInstallItem(
						"SQL Server + Sybase",
						"jtds-driver.jar",
						"http://mirrors.ibiblio.org/pub/mirrors/maven2/net/sourceforge/jtds/jtds/1.2.2/jtds-1.2.2.jar",
						JTDS_DRIVER, "images/database_jtds.png"));
		automaticMenu
				.add(downloadAndInstallItem(
						"Derby",
						"http://mirrors.ibiblio.org/pub/mirrors/maven2/org/apache/derby/derbyclient/10.4.2.0/derbyclient-10.4.2.0.jar",
						DERBY_DRIVER, "images/database_derby.png"));
		automaticMenu
				.add(downloadAndInstallItem(
						"SQLite",
						"http://mirrors.ibiblio.org/pub/mirrors/maven2/org/xerial/sqlite-jdbc/3.6.7/sqlite-jdbc-3.6.7.jar",
						SQLITE_DRIVER, "images/database_sqlite.png"));
		websiteMenu
				.add(downloadItem(
						"Oracle",
						"http://www.oracle.com/technology/software/tech/java/sqlj_jdbc",
						"images/database_oracle.png"));
		websiteMenu.add(downloadItem("Firebird",
				"http://www.firebirdsql.org/index.php?op=files&id=jaybird",
				"images/database_firebird.png"));
		websiteMenu
				.add(downloadItem(
						"Ingres",
						"http://esd.ingres.com/product/Community_Projects/Drivers/java/JDBC",
						"images/database_ingres.png"));
		websiteMenu.add(downloadItem("IBM DB2",
				"http://www.ibm.com/software/data/db2/java/",
				"images/database_db2.png"));
		websiteMenu.add(downloadItem("Mondrian + SAP BW + Analysis Services",
				"http://www.jdbc4olap.org", "images/database_jdbc4olap.png"));
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

	private static JMenuItem downloadAndInstallItem(final String databaseName,
			final String downloadUrl, final String driverClass, String iconPath) {
		String filename = databaseName.toLowerCase().replace(' ', '_')
				+ "-driver.jar";
		return downloadAndInstallItem(databaseName, filename, downloadUrl,
				driverClass, iconPath);
	}

	private static JMenuItem downloadAndInstallItem(final String databaseName,
			final String filename, final String downloadUrl,
			final String driverClass, String iconPath) {
		JMenuItem menuItem = new JMenuItem(databaseName, GuiHelper
				.getImageIcon(iconPath));
		menuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				if (GuiSettings.getSettings().isDriverInstalled(driverClass)) {
					GuiHelper.showErrorMessage("Driver already installed",
							"An existing driver for " + databaseName
									+ " (class name '" + driverClass
									+ "') is already installed.", null);
				} else {
					final File file = new File(filename);
					final DownloadDialog dialog = new DownloadDialog(
							downloadUrl, file);
					dialog.setCompleteAction(new ActionListener() {

						public void actionPerformed(ActionEvent event) {
							DatabaseDriver driver = new DatabaseDriver(file,
									driverClass);
							try {
								driver.loadDriver();
								GuiSettings settings = GuiSettings
										.getSettings();
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

			}

		});

		return menuItem;
	}

	private static JMenuItem downloadItem(final String databaseName,
			final String url, String iconPath) {
		JMenuItem menuItem = new JMenuItem(databaseName, GuiHelper
				.getImageIcon(iconPath));
		try {
			menuItem.addActionListener(new OpenBrowserAction(url));
		} catch (MalformedURLException e) {
			_log.error(e);
		}
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