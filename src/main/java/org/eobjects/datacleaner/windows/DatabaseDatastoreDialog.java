/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.datacleaner.windows;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPasswordField;
import javax.swing.KeyStroke;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.datacleaner.database.DatabaseDriverCatalog;
import org.eobjects.datacleaner.database.DatabaseDriverDescriptor;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.JXTextField;

public class DatabaseDatastoreDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;

	private static final String MANAGE_DATABASE_DRIVERS = "Manage database drivers...";

	private final AnalyzerBeansConfiguration _configuration;
	private final ImageManager imageManager = ImageManager.getInstance();
	private final DatabaseDriverCatalog _databaseDriverCatalog = new DatabaseDriverCatalog();
	private final MutableDatastoreCatalog _catalog;
	private final JXTextField _datastoreNameTextField;
	private final JXTextField _driverClassNameTextField;
	private final JXTextField _connectionStringTextField;
	private final JXTextField _usernameTextField;
	private final JPasswordField _passwordField;
	private final JComboBox _databaseDriverComboBox;

	public DatabaseDatastoreDialog(AnalyzerBeansConfiguration configuration, MutableDatastoreCatalog catalog) {
		_configuration = configuration;
		_catalog = catalog;

		_datastoreNameTextField = WidgetFactory.createTextField("Name");
		_driverClassNameTextField = WidgetFactory.createTextField("Driver class name");
		_connectionStringTextField = WidgetFactory.createTextField("Connection string / URL");
		_connectionStringTextField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "nextTemplateItem");
		_connectionStringTextField.getActionMap().put("nextTemplateItem", getNextTemplateItemAction());

		final List<DatabaseDriverDescriptor> databaseDrivers = _databaseDriverCatalog.getInstalledDatabaseDrivers();
		Object[] databaseNames = new Object[databaseDrivers.size() + 2];
		for (int i = 0; i < databaseDrivers.size(); i++) {
			databaseNames[i + 1] = databaseDrivers.get(i);
		}
		databaseNames[databaseNames.length - 1] = MANAGE_DATABASE_DRIVERS;

		_databaseDriverComboBox = new JComboBox(databaseNames);
		_databaseDriverComboBox.setRenderer(new DefaultListCellRenderer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel result = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				if (value instanceof DatabaseDriverDescriptor) {
					DatabaseDriverDescriptor databaseDriver = (DatabaseDriverDescriptor) value;

					String iconImagePath = _databaseDriverCatalog.getIconImagePath(databaseDriver);
					Icon driverIcon = imageManager.getImageIcon(iconImagePath, IconUtils.ICON_SIZE_SMALL);

					result.setText(databaseDriver.getDisplayName());
					result.setIcon(driverIcon);
				} else if (MANAGE_DATABASE_DRIVERS.equals(value)) {
					result.setIcon(imageManager.getImageIcon("images/menu/options.png", IconUtils.ICON_SIZE_SMALL));
				}

				return result;
			}
		});
		_databaseDriverComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object value = _databaseDriverComboBox.getSelectedItem();
				if (value instanceof DatabaseDriverDescriptor) {
					DatabaseDriverDescriptor driver = (DatabaseDriverDescriptor) value;

					_driverClassNameTextField.setText(driver.getDriverClassName());

					String[] connectionUrls = driver.getConnectionUrlTemplates();
					setConnectionUrlTemplates(connectionUrls);
				} else if (MANAGE_DATABASE_DRIVERS.equals(value)) {
					OptionsDialog optionsDialog = new OptionsDialog(_configuration);
					optionsDialog.selectDatabaseDriversTab();
					optionsDialog.setVisible(true);
					DatabaseDatastoreDialog.this.dispose();
				}
			}
		});

		_usernameTextField = WidgetFactory.createTextField("Username");
		_passwordField = new JPasswordField(17);
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}

	/**
	 * @return an action listener that will set the correct focus, either inside
	 *         a template connection url or the next text field.
	 */
	private Action getNextTemplateItemAction() {
		return new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				String text = _connectionStringTextField.getText();
				int selectionEnd = _connectionStringTextField.getSelectionEnd();
				int selectionStart = text.indexOf('<', selectionEnd);
				if (selectionStart != -1) {
					selectionEnd = text.indexOf('>', selectionStart);
				}

				if (selectionStart != -1 && selectionEnd != -1) {
					_connectionStringTextField.setSelectionStart(selectionStart);
					_connectionStringTextField.setSelectionEnd(selectionEnd + 1);
					_connectionStringTextField.requestFocus();
				} else {
					selectionStart = text.indexOf('<');
					if (selectionStart != -1) {
						selectionEnd = text.indexOf('>', selectionStart);
						if (selectionEnd != -1) {
							_connectionStringTextField.setSelectionStart(selectionStart);
							_connectionStringTextField.setSelectionEnd(selectionEnd + 1);
							_connectionStringTextField.requestFocus();
						} else {
							_usernameTextField.requestFocus();
						}
					} else {
						_usernameTextField.requestFocus();
					}
				}
				
				_connectionStringTextField.getHorizontalVisibility().setValue(0);
			}
		};
	}

	@Override
	protected String getBannerTitle() {
		return "Database connection";
	}

	@Override
	protected int getDialogWidth() {
		return 500;
	}

	private void setConnectionUrlTemplates(String[] connectionUrls) {
		if (connectionUrls != null && connectionUrls.length > 0) {
			_connectionStringTextField.setFocusTraversalKeysEnabled(false);
			String url = connectionUrls[0];
			_connectionStringTextField.setText(url);
			
			getNextTemplateItemAction().actionPerformed(null);
		}
	}

	@Override
	protected JComponent getDialogContent() {
		DCPanel panel = new DCPanel();

		int row = 0;
		WidgetUtils.addToGridBag(new JLabel("Datastore name:"), panel, 0, row);
		WidgetUtils.addToGridBag(_datastoreNameTextField, panel, 1, row);

		row++;
		WidgetUtils.addToGridBag(new JLabel("Database:"), panel, 0, row);
		WidgetUtils.addToGridBag(_databaseDriverComboBox, panel, 1, row);

		row++;
		WidgetUtils.addToGridBag(new JLabel("Driver class name:"), panel, 0, row);
		WidgetUtils.addToGridBag(_driverClassNameTextField, panel, 1, row);

		row++;
		WidgetUtils.addToGridBag(new JLabel("Connection string:"), panel, 0, row);
		WidgetUtils.addToGridBag(_connectionStringTextField, panel, 1, row);

		row++;
		WidgetUtils.addToGridBag(new JLabel("Username:"), panel, 0, row);
		WidgetUtils.addToGridBag(_usernameTextField, panel, 1, row);

		row++;
		WidgetUtils.addToGridBag(new JLabel("Password:"), panel, 0, row);
		WidgetUtils.addToGridBag(_passwordField, panel, 1, row);

		row++;

		JButton saveButton = WidgetFactory.createButton("Save datastore", "images/model/datastore.png");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String datastoreName = _datastoreNameTextField.getText();
				String driverClass = _driverClassNameTextField.getText();

				JdbcDatastore datastore = new JdbcDatastore(datastoreName, _connectionStringTextField.getText(),
						driverClass, _usernameTextField.getText(), new String(_passwordField.getPassword()));

				_catalog.addDatastore(datastore);
				DatabaseDatastoreDialog.this.dispose();
			}
		});

		DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		buttonPanel.add(saveButton);

		WidgetUtils.addToGridBag(buttonPanel, panel, 1, row, 2, 1);

		return panel;
	}

	@Override
	protected String getWindowTitle() {
		return "Database connection | Datastore";
	}

}
