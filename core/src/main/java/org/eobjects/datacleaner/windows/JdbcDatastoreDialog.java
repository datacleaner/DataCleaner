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
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.connection.UpdateableDatastoreConnection;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.database.DatabaseDriverCatalog;
import org.eobjects.datacleaner.database.DatabaseDriverDescriptor;
import org.eobjects.datacleaner.guice.Nullable;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCComboBox;
import org.eobjects.datacleaner.widgets.DCComboBox.Listener;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.jdesktop.swingx.JXTextField;

public class JdbcDatastoreDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;

	/**
	 * Number of connections to try to create (in case of non-multiple
	 * connections, this is just the number of handles to the same connection).
	 */
	private static final int TEST_CONNECTION_COUNT = 20;

	private static final String MANAGE_DATABASE_DRIVERS = "Manage database drivers...";
	private static final ImageManager imageManager = ImageManager.getInstance();

	private final JdbcDatastore _originalDatastore;
	private final DatabaseDriverCatalog _databaseDriverCatalog;
	private final MutableDatastoreCatalog _catalog;
	private final JXTextField _datastoreNameTextField;
	private final JXTextField _driverClassNameTextField;
	private final JXTextField _connectionStringTextField;
	private final JXTextField _usernameTextField;
	private final JPasswordField _passwordField;
	private final JCheckBox _multipleConnectionsCheckBox;
	private final DCComboBox<Object> _databaseDriverComboBox;
	private final Provider<OptionsDialog> _optionsDialogProvider;
	private final JButton _connectionStringTemplateButton;

	private volatile String[] _connectionUrls;

	@Inject
	protected JdbcDatastoreDialog(@Nullable JdbcDatastore datastore, MutableDatastoreCatalog catalog,
			WindowContext windowContext, Provider<OptionsDialog> optionsDialogProvider,
			DatabaseDriverCatalog databaseDriverCatalog) {
		super(windowContext, imageManager.getImage("images/window/banner-datastores.png"));
		_originalDatastore = datastore;
		_catalog = catalog;
		_optionsDialogProvider = optionsDialogProvider;
		_databaseDriverCatalog = databaseDriverCatalog;

		_multipleConnectionsCheckBox = new JCheckBox("Allow multiple concurrent connections", true);
		_multipleConnectionsCheckBox
				.setToolTipText("Indicates whether multiple connections (aka. connection pooling) may be created or not. "
						+ "Connection pooling is preferred for performance reasons, but can safely be disabled if not desired. "
						+ "The max number of connections cannot be configured, "
						+ "but no more connections than the number of threads in the task runner should be expected.");
		_multipleConnectionsCheckBox.setOpaque(false);
		_multipleConnectionsCheckBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);

		final int textFieldWidth = 30;
		_datastoreNameTextField = WidgetFactory.createTextField("Name", textFieldWidth);
		_driverClassNameTextField = WidgetFactory.createTextField("Driver class name", textFieldWidth);
		_connectionStringTextField = WidgetFactory.createTextField("Connection string / URL", textFieldWidth);
		_usernameTextField = WidgetFactory.createTextField("Username", textFieldWidth);
		_passwordField = new JPasswordField(textFieldWidth);

		_connectionStringTextField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "nextTemplateItem");
		_connectionStringTextField.getActionMap().put("nextTemplateItem", getNextTemplateItemAction());
		_connectionStringTemplateButton = new JButton(imageManager.getImageIcon("images/widgets/help.png",
				IconUtils.ICON_SIZE_SMALL));
		_connectionStringTemplateButton.setMargin(new Insets(0, 0, 0, 0));
		_connectionStringTemplateButton.setOpaque(false);
		_connectionStringTemplateButton.setBorder(null);
		_connectionStringTemplateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (_connectionUrls != null) {
					final JPopupMenu menu = new JPopupMenu();
					for (final String connectionUrl : _connectionUrls) {
						final JMenuItem menuItem = new JMenuItem(connectionUrl);
						menuItem.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								_connectionStringTextField.setText(connectionUrl);
								getNextTemplateItemAction().actionPerformed(null);
							}
						});
						menu.add(menuItem);
					}
					menu.show(_connectionStringTemplateButton, 0, 0);
				}
			}
		});

		final List<DatabaseDriverDescriptor> databaseDrivers = _databaseDriverCatalog.getInstalledWorkingDatabaseDrivers();
		final Object[] comboBoxModel = new Object[databaseDrivers.size() + 3];
		comboBoxModel[0] = "";
		for (int i = 0; i < databaseDrivers.size(); i++) {
			comboBoxModel[i + 1] = databaseDrivers.get(i);
		}
		comboBoxModel[comboBoxModel.length - 2] = new JSeparator(JSeparator.HORIZONTAL);
		comboBoxModel[comboBoxModel.length - 1] = MANAGE_DATABASE_DRIVERS;

		_databaseDriverComboBox = new DCComboBox<Object>(comboBoxModel);
		_databaseDriverComboBox.setRenderer(new DefaultListCellRenderer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel result = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				if (value instanceof DatabaseDriverDescriptor) {
					DatabaseDriverDescriptor databaseDriver = (DatabaseDriverDescriptor) value;

					String iconImagePath = DatabaseDriverCatalog.getIconImagePath(databaseDriver);
					Icon driverIcon = imageManager.getImageIcon(iconImagePath, IconUtils.ICON_SIZE_SMALL);

					result.setText(databaseDriver.getDisplayName());
					result.setIcon(driverIcon);
				} else if (MANAGE_DATABASE_DRIVERS.equals(value)) {
					result.setIcon(imageManager.getImageIcon(IconUtils.MENU_OPTIONS, IconUtils.ICON_SIZE_SMALL));
				} else if (value instanceof Component) {
					return (Component) value;
				}

				return result;
			}
		});
		_databaseDriverComboBox.addListener(new Listener<Object>() {
			@Override
			public void onItemSelected(Object item) {
				Object value = _databaseDriverComboBox.getSelectedItem();
				if (value instanceof DatabaseDriverDescriptor) {
					DatabaseDriverDescriptor driver = (DatabaseDriverDescriptor) value;

					_driverClassNameTextField.setText(driver.getDriverClassName());

					String[] connectionUrls = driver.getConnectionUrlTemplates();
					setConnectionUrlTemplates(connectionUrls);
				} else if (MANAGE_DATABASE_DRIVERS.equals(value)) {
					OptionsDialog optionsDialog = _optionsDialogProvider.get();
					optionsDialog.selectDatabaseDriversTab();
					JdbcDatastoreDialog.this.dispose();

					optionsDialog.setVisible(true);
					optionsDialog.toFront();
				}
			}
		});

		if (_originalDatastore != null) {
			_multipleConnectionsCheckBox.setSelected(_originalDatastore.isMultipleConnections());

			// the database driver has to be set as the first thing, because the
			// combobox's action listener will set other field's values as well.
			DatabaseDriverDescriptor databaseDriver = DatabaseDriverCatalog
					.getDatabaseDriverByDriverClassName(_originalDatastore.getDriverClass());
			_databaseDriverComboBox.setSelectedItem(databaseDriver);

			_datastoreNameTextField.setText(_originalDatastore.getName());
			_datastoreNameTextField.setEnabled(false);
			_connectionStringTextField.setText(_originalDatastore.getJdbcUrl());
			_driverClassNameTextField.setText(_originalDatastore.getDriverClass());
			_usernameTextField.setText(_originalDatastore.getUsername());
			_passwordField.setText(_originalDatastore.getPassword());
		}
	}

	public void setSelectedDatabase(String databaseName) {
		DatabaseDriverDescriptor databaseDriverDescriptor = DatabaseDriverCatalog
				.getDatabaseDriverByDriverDatabaseName(databaseName);
		setSelectedDatabase(databaseDriverDescriptor);
	}

	public void setSelectedDatabase(DatabaseDriverDescriptor databaseDriverDescriptor) {
		_databaseDriverComboBox.setSelectedItem(databaseDriverDescriptor);
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
		_connectionUrls = connectionUrls;
		boolean selectable = false;

		if (connectionUrls != null && connectionUrls.length > 0) {
			if (connectionUrls.length > 1) {
				selectable = true;
			}

			_connectionStringTextField.setFocusTraversalKeysEnabled(false);
			String url = connectionUrls[0];
			_connectionStringTextField.setText(url);

			getNextTemplateItemAction().actionPerformed(null);
		}

		_connectionStringTemplateButton.setVisible(selectable);
	}

	@Override
	protected JComponent getDialogContent() {
		DCPanel panel = new DCPanel();

		int row = 0;
		WidgetUtils.addToGridBag(DCLabel.bright("Datastore name:"), panel, 0, row);
		WidgetUtils.addToGridBag(_datastoreNameTextField, panel, 1, row);

		row++;
		WidgetUtils.addToGridBag(DCLabel.bright("Database:"), panel, 0, row);
		WidgetUtils.addToGridBag(_databaseDriverComboBox, panel, 1, row);

		row++;
		WidgetUtils.addToGridBag(DCLabel.bright("Driver class name:"), panel, 0, row);
		WidgetUtils.addToGridBag(_driverClassNameTextField, panel, 1, row);

		row++;
		WidgetUtils.addToGridBag(DCLabel.bright("Connection string:"), panel, 0, row);
		WidgetUtils.addToGridBag(_connectionStringTextField, panel, 1, row);
		WidgetUtils.addToGridBag(_connectionStringTemplateButton, panel, 2, row, 0.0d, 0.0d);

		row++;
		WidgetUtils.addToGridBag(_multipleConnectionsCheckBox, panel, 1, row, 2, 1);

		row++;
		WidgetUtils.addToGridBag(DCLabel.bright("Username:"), panel, 0, row);
		WidgetUtils.addToGridBag(_usernameTextField, panel, 1, row);

		row++;
		WidgetUtils.addToGridBag(DCLabel.bright("Password:"), panel, 0, row);
		WidgetUtils.addToGridBag(_passwordField, panel, 1, row);

		row++;

		final JButton testButton = WidgetFactory.createButton("Test connection", "images/actions/refresh.png");
		testButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				JdbcDatastore datastore = createDatastore();
				final List<DatastoreConnection> connections = new ArrayList<DatastoreConnection>();
				try {
					for (int i = 0; i < TEST_CONNECTION_COUNT; i++) {
						UpdateableDatastoreConnection connection = datastore.openConnection();
						connections.add(connection);
					}
				} catch (Throwable e) {
					WidgetUtils.showErrorMessage("Could not establish connection", e);
					return;
				} finally {
					for (DatastoreConnection connection : connections) {
						connection.close();
					}
				}

				JOptionPane.showMessageDialog(JdbcDatastoreDialog.this, "Connection successful!");
			}
		});

		final JButton saveButton = WidgetFactory.createButton("Save datastore", "images/model/datastore.png");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JdbcDatastore datastore = createDatastore();

				if (_originalDatastore != null) {
					_catalog.removeDatastore(_originalDatastore);
				}
				_catalog.addDatastore(datastore);
				JdbcDatastoreDialog.this.dispose();
			}
		});

		final DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		buttonPanel.add(testButton);
		buttonPanel.add(saveButton);

		WidgetUtils.addToGridBag(buttonPanel, panel, 1, row, 3, 1);

		return panel;
	}

	private JdbcDatastore createDatastore() {
		final String datastoreName = _datastoreNameTextField.getText();
		if (StringUtils.isNullOrEmpty(datastoreName)) {
			throw new IllegalStateException("No datastore name");
		}

		final String driverClass = _driverClassNameTextField.getText();
		final String connectionString = _connectionStringTextField.getText();
		final String username = _usernameTextField.getText();
		final char[] password = _passwordField.getPassword();
		final boolean multipleConnections = _multipleConnectionsCheckBox.isSelected();

		JdbcDatastore datastore = new JdbcDatastore(datastoreName, connectionString, driverClass, username, new String(
				password), multipleConnections);

		return datastore;
	}

	@Override
	public String getWindowTitle() {
		return "Database connection | Datastore";
	}

	@Override
	public Image getWindowIcon() {
		return imageManager.getImage("images/model/datastore.png");
	}
}
