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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.sql.DataSource;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;

import org.eobjects.analyzer.connection.JdbcDatastore;
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
import org.eobjects.datacleaner.widgets.DCCheckBox;
import org.eobjects.datacleaner.widgets.DCComboBox;
import org.eobjects.datacleaner.widgets.DCComboBox.Listener;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.HumanInferenceToolbarButton;
import org.eobjects.datacleaner.widgets.database.CubridDatabaseConnectionPresenter;
import org.eobjects.datacleaner.widgets.database.DatabaseConnectionPresenter;
import org.eobjects.datacleaner.widgets.database.DefaultDatabaseConnectionPresenter;
import org.eobjects.datacleaner.widgets.database.MysqlDatabaseConnectionPresenter;
import org.eobjects.datacleaner.widgets.database.OracleDatabaseConnectionPresenter;
import org.eobjects.datacleaner.widgets.database.PostgresqlDatabaseConnectionPresenter;
import org.eobjects.datacleaner.widgets.database.SQLServerDatabaseConnectionPresenter;
import org.eobjects.datacleaner.widgets.tabs.CloseableTabbedPane;
import org.eobjects.metamodel.util.FileHelper;
import org.jdesktop.swingx.JXTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcDatastoreDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(JdbcDatastoreDialog.class);

	public static final int TEXT_FIELD_WIDTH = 30;

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
	private final DCCheckBox<Object> _multipleConnectionsCheckBox;
	private final DCComboBox<Object> _databaseDriverComboBox;
	private final Provider<OptionsDialog> _optionsDialogProvider;
	private final CloseableTabbedPane _tabbedPane;
	private final DatabaseConnectionPresenter[] _connectionPresenters;

	@Inject
	protected JdbcDatastoreDialog(@Nullable JdbcDatastore datastore, MutableDatastoreCatalog catalog,
			WindowContext windowContext, Provider<OptionsDialog> optionsDialogProvider,
			DatabaseDriverCatalog databaseDriverCatalog) {
		super(windowContext, imageManager.getImage("images/window/banner-datastores.png"));
		_originalDatastore = datastore;
		_catalog = catalog;
		_optionsDialogProvider = optionsDialogProvider;
		_databaseDriverCatalog = databaseDriverCatalog;

		// there will always be 2 connection presenters, but the second is
		// optional
		_connectionPresenters = new DatabaseConnectionPresenter[2];
		_connectionPresenters[0] = new DefaultDatabaseConnectionPresenter();

		_tabbedPane = new CloseableTabbedPane(true);
		_tabbedPane.addTab("Generic connection parameters",
				imageManager.getImageIcon(IconUtils.GENERIC_DATASTORE_IMAGEPATH, IconUtils.ICON_SIZE_MEDIUM),
				_connectionPresenters[0].getWidget());
		_tabbedPane.setUnclosableTab(0);

		_multipleConnectionsCheckBox = new DCCheckBox<Object>("Allow multiple concurrent connections", true);
		_multipleConnectionsCheckBox
				.setToolTipText("Indicates whether multiple connections (aka. connection pooling) may be created or not. "
						+ "Connection pooling is preferred for performance reasons, but can safely be disabled if not desired. "
						+ "The max number of connections cannot be configured, "
						+ "but no more connections than the number of threads in the task runner should be expected.");
		_multipleConnectionsCheckBox.setOpaque(false);
		_multipleConnectionsCheckBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);

		_datastoreNameTextField = WidgetFactory.createTextField("Name", TEXT_FIELD_WIDTH);
		_driverClassNameTextField = WidgetFactory.createTextField("Driver class name", TEXT_FIELD_WIDTH);

		final List<DatabaseDriverDescriptor> databaseDrivers = _databaseDriverCatalog
				.getInstalledWorkingDatabaseDrivers();
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
				if ("".equals(value)) {
					value = "- select -";
				}

				JLabel result = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);

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
			public void onItemSelected(Object value) {
				if ("".equals(value)) {
					setSelectedDatabase((DatabaseDriverDescriptor) null);
					_driverClassNameTextField.setText("");
					_driverClassNameTextField.setEnabled(true);
				} else if (value instanceof DatabaseDriverDescriptor) {
					DatabaseDriverDescriptor driver = (DatabaseDriverDescriptor) value;

					setSelectedDatabase(driver);

					_driverClassNameTextField.setText(driver.getDriverClassName());
					_driverClassNameTextField.setEnabled(false);
				} else if (MANAGE_DATABASE_DRIVERS.equals(value)) {
					OptionsDialog optionsDialog = _optionsDialogProvider.get();
					optionsDialog.selectDatabaseDriversTab();
					JdbcDatastoreDialog.this.dispose();

					optionsDialog.setVisible(true);
					optionsDialog.toFront();
				}
			}
		});

		if (_originalDatastore == null) {
			// remove connection url templates
			setSelectedDatabase((DatabaseDriverDescriptor) null);
		} else {
			_multipleConnectionsCheckBox.setSelected(_originalDatastore.isMultipleConnections());

			// the database driver has to be set as the first thing, because the
			// combobox's action listener will set other field's values as well.
			DatabaseDriverDescriptor databaseDriver = DatabaseDriverCatalog
					.getDatabaseDriverByDriverClassName(_originalDatastore.getDriverClass());
			_databaseDriverComboBox.setSelectedItem(databaseDriver);

			_datastoreNameTextField.setText(_originalDatastore.getName());
			_datastoreNameTextField.setEnabled(false);

			_connectionPresenters[0].initialize(_originalDatastore);

			_driverClassNameTextField.setText(_originalDatastore.getDriverClass());
		}
	}

	public void setSelectedDatabase(String databaseName) {
		DatabaseDriverDescriptor databaseDriverDescriptor = DatabaseDriverCatalog
				.getDatabaseDriverByDriverDatabaseName(databaseName);
		setSelectedDatabase(databaseDriverDescriptor);
	}

	public void setSelectedDatabase(DatabaseDriverDescriptor databaseDriverDescriptor) {
		_databaseDriverComboBox.setSelectedItem(databaseDriverDescriptor);

		if (_tabbedPane.getTabCount() > 1) {
			_tabbedPane.removeTabAt(1);
		}

		// register the presenter
		final DatabaseConnectionPresenter customPresenter = createDatabaseConnectionPresenter(databaseDriverDescriptor);
		_connectionPresenters[1] = customPresenter;

		if (customPresenter != null) {
			boolean accepted = true;

			// init if original datastore is available
			if (_originalDatastore != null) {
				accepted = customPresenter.initialize(_originalDatastore);
			}

			if (accepted) {

				_tabbedPane.setUnclosableTab(1);
				_tabbedPane.addTab(databaseDriverDescriptor.getDisplayName() + " connection", imageManager
						.getImageIcon(databaseDriverDescriptor.getIconImagePath(), IconUtils.ICON_SIZE_MEDIUM,
								customPresenter.getClass().getClassLoader()), customPresenter.getWidget());
				_tabbedPane.setSelectedIndex(1);
			} else {
				// unregister the presenter
				_connectionPresenters[1] = null;
			}
		}

		for (DatabaseConnectionPresenter connectionPresenter : _connectionPresenters) {
			if (connectionPresenter != null) {
				connectionPresenter.setSelectedDatabaseDriver(databaseDriverDescriptor);
			}
		}
	}

	public DatabaseConnectionPresenter createDatabaseConnectionPresenter(
			DatabaseDriverDescriptor databaseDriverDescriptor) {
		if (databaseDriverDescriptor == null) {
			return null;
		}
		final String databaseName = databaseDriverDescriptor.getDisplayName();

		final DatabaseConnectionPresenter result;
		
		if (DatabaseDriverCatalog.DATABASE_NAME_MYSQL.equals(databaseName)) {
			result = new MysqlDatabaseConnectionPresenter();
		} else if (DatabaseDriverCatalog.DATABASE_NAME_POSTGRESQL.equals(databaseName)) {
			result = new PostgresqlDatabaseConnectionPresenter();
		} else if (DatabaseDriverCatalog.DATABASE_NAME_ORACLE.equals(databaseName)) {
			result = new OracleDatabaseConnectionPresenter();
		} else if (DatabaseDriverCatalog.DATABASE_NAME_MICROSOFT_SQL_SERVER_JTDS.equals(databaseName)) {
			result = new SQLServerDatabaseConnectionPresenter();
		} else if (DatabaseDriverCatalog.DATABASE_NAME_CUBRID.equals(databaseName)) {
			result = new CubridDatabaseConnectionPresenter();
		} else {
			result = null;
		}
		
		return result;
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}

	@Override
	protected String getBannerTitle() {
		return "Database connection";
	}

	@Override
	protected int getDialogWidth() {
		return 500;
	}

	@Override
	protected JComponent getDialogContent() {
		final DCPanel panel = new DCPanel();
		{
			int row = 0;
			WidgetUtils.addToGridBag(DCLabel.bright("Datastore name:"), panel, 0, row);
			WidgetUtils.addToGridBag(_datastoreNameTextField, panel, 1, row);

			row++;
			WidgetUtils.addToGridBag(DCLabel.bright("Database driver:"), panel, 0, row);
			WidgetUtils.addToGridBag(_databaseDriverComboBox, panel, 1, row);

			row++;
			WidgetUtils.addToGridBag(DCLabel.bright("Driver class name:"), panel, 0, row);
			WidgetUtils.addToGridBag(_driverClassNameTextField, panel, 1, row);

			row++;
			WidgetUtils.addToGridBag(_multipleConnectionsCheckBox, panel, 1, row);
		}

		final JButton testButton = WidgetFactory.createButton(getTestButtonText(), "images/actions/refresh.png");
		testButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				JdbcDatastore datastore = createDatastore();

				final List<Connection> connections = new ArrayList<Connection>();

				try {
					if (datastore.isMultipleConnections()) {
						DataSource ds = datastore.createDataSource();
						for (int i = 0; i < TEST_CONNECTION_COUNT; i++) {
							Connection connection = ds.getConnection();
							connections.add(connection);
						}
					} else {
						Connection connnection = datastore.createConnection();
						connections.add(connnection);
					}
				} catch (Throwable e) {
					WidgetUtils.showErrorMessage("Could not establish connection", e);
					return;
				} finally {
					for (Connection connection : connections) {
						FileHelper.safeClose(connection);
					}
				}

				JOptionPane.showMessageDialog(JdbcDatastoreDialog.this, "Connection successful!");
			}
		});

		final JButton saveButton = WidgetFactory.createButton("Save datastore", "images/model/datastore.png");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JdbcDatastore datastore = createDatastore();

				if (_originalDatastore != null) {
					_catalog.removeDatastore(_originalDatastore);
				}
				_catalog.addDatastore(datastore);
				JdbcDatastoreDialog.this.dispose();
			}
		});

		_multipleConnectionsCheckBox.addListener(new DCCheckBox.Listener<Object>() {
			@Override
			public void onItemSelected(Object item, boolean selected) {
				testButton.setText(getTestButtonText());
			}
		});

		final JToolBar toolBar = WidgetFactory.createToolBar();
		toolBar.add(new HumanInferenceToolbarButton());
		toolBar.add(WidgetFactory.createToolBarSeparator());
		toolBar.add(testButton);
		toolBar.add(Box.createHorizontalStrut(4));
		toolBar.add(saveButton);

		final DCPanel toolBarPanel = new DCPanel(WidgetUtils.BG_COLOR_DARKEST, WidgetUtils.BG_COLOR_DARKEST);
		toolBarPanel.setLayout(new BorderLayout());
		toolBarPanel.add(toolBar, BorderLayout.CENTER);

		final DCPanel outerPanel = new DCPanel(WidgetUtils.BG_COLOR_DARK, WidgetUtils.BG_COLOR_DARK);
		outerPanel.setLayout(new BorderLayout());
		outerPanel.add(panel, BorderLayout.NORTH);
		outerPanel.add(_tabbedPane, BorderLayout.CENTER);
		outerPanel.add(toolBarPanel, BorderLayout.SOUTH);
		return outerPanel;
	}

	private String getTestButtonText() {
		if (_multipleConnectionsCheckBox.isSelected()) {
			return "Test connections";
		}
		return "Test connection";
	}

	private JdbcDatastore createDatastore() {
		final String datastoreName = _datastoreNameTextField.getText();
		if (StringUtils.isNullOrEmpty(datastoreName)) {
			throw new IllegalStateException("No datastore name");
		}

		final int connectionPresenterIndex = _tabbedPane.getSelectedIndex();
		final DatabaseConnectionPresenter connectionPresenter = _connectionPresenters[connectionPresenterIndex];

		logger.info("Creating datastore using connection presenter ({}): {}", connectionPresenterIndex,
				connectionPresenter);

		final String driverClass = _driverClassNameTextField.getText();
		final String connectionString = connectionPresenter.getJdbcUrl();
		final String username = connectionPresenter.getUsername();
		final String password = connectionPresenter.getPassword();
		final boolean multipleConnections = _multipleConnectionsCheckBox.isSelected();

		final JdbcDatastore datastore = new JdbcDatastore(datastoreName, connectionString, driverClass, username,
				password, multipleConnections);

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
