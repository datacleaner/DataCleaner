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

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.model.DatabaseDriver;
import dk.eobjects.datacleaner.gui.model.ExtensionFilter;
import dk.eobjects.datacleaner.gui.setup.GuiSettings;
import dk.eobjects.datacleaner.gui.widgets.AutoCompleteComboBox;
import dk.eobjects.datacleaner.util.ReflectionHelper;

public class DatabaseDriverDialog extends BanneredDialog {

	private static final long serialVersionUID = -7450893693170647726L;
	private static final String[] COMMON_DRIVER_NAMES = {
			"org.postgresql.Driver", "org.firebirdsql.jdbc.FBDriver",
			"org.apache.derby.jdbc.ClientDriver", "oracle.jdbc.OracleDriver",
			"com.mysql.jdbc.Driver", "net.sourceforge.jtds.jdbc.Driver",
			"com.microsoft.sqlserver.jdbc.SQLServerDriver",
			"com.ingres.jdbc.IngresDriver", "org.sqlite.JDBC" };
	private JTextField _filenameField;
	private AutoCompleteComboBox _driverClassField;
	private File _file;

	public static ActionListener getActionListener() {
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