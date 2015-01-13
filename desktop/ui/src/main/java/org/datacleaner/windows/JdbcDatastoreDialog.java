/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.windows;

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
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;

import org.apache.metamodel.util.FileHelper;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.database.DatabaseDriverDescriptor;
import org.datacleaner.guice.Nullable;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCCheckBox;
import org.datacleaner.widgets.DCComboBox;
import org.datacleaner.widgets.DCComboBox.Listener;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DCListCellRenderer;
import org.datacleaner.widgets.DescriptionLabel;
import org.datacleaner.widgets.NeopostToolbarButton;
import org.datacleaner.widgets.database.CubridDatabaseConnectionPresenter;
import org.datacleaner.widgets.database.DatabaseConnectionPresenter;
import org.datacleaner.widgets.database.DefaultDatabaseConnectionPresenter;
import org.datacleaner.widgets.database.H2DatabaseConnectionPresenter;
import org.datacleaner.widgets.database.MysqlDatabaseConnectionPresenter;
import org.datacleaner.widgets.database.OracleDatabaseConnectionPresenter;
import org.datacleaner.widgets.database.PostgresqlDatabaseConnectionPresenter;
import org.datacleaner.widgets.database.SQLServerDatabaseConnectionPresenter;
import org.datacleaner.widgets.tabs.CloseableTabbedPane;
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
    private static final ImageManager imageManager = ImageManager.get();

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
        _databaseDriverComboBox.setRenderer(new DCListCellRenderer() {

            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
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
        } else if (DatabaseDriverCatalog.DATABASE_NAME_H2.equals(databaseName)) {
            result = new H2DatabaseConnectionPresenter();
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
        final DCPanel formPanel = new DCPanel();
        {
            int row = 0;
            WidgetUtils.addToGridBag(DCLabel.bright("Datastore name:"), formPanel, 0, row);
            WidgetUtils.addToGridBag(_datastoreNameTextField, formPanel, 1, row);

            row++;
            WidgetUtils.addToGridBag(DCLabel.bright("Database driver:"), formPanel, 0, row);
            WidgetUtils.addToGridBag(_databaseDriverComboBox, formPanel, 1, row);

            row++;
            WidgetUtils.addToGridBag(DCLabel.bright("Driver class name:"), formPanel, 0, row);
            WidgetUtils.addToGridBag(_driverClassNameTextField, formPanel, 1, row);

            row++;
            WidgetUtils.addToGridBag(_multipleConnectionsCheckBox, formPanel, 1, row);
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
        toolBar.add(new NeopostToolbarButton());
        toolBar.add(WidgetFactory.createToolBarSeparator());
        toolBar.add(testButton);
        toolBar.add(Box.createHorizontalStrut(4));
        toolBar.add(saveButton);

        final DCPanel toolBarPanel = new DCPanel(WidgetUtils.COLOR_ALTERNATIVE_BACKGROUND);
        toolBarPanel.setLayout(new BorderLayout());
        toolBarPanel.add(toolBar, BorderLayout.CENTER);

        final DCPanel formContainerPanel = new DCPanel(WidgetUtils.COLOR_ALTERNATIVE_BACKGROUND);
        formContainerPanel.setLayout(new BorderLayout());
        formContainerPanel.setBorder(WidgetUtils.BORDER_TOP_PADDING);
        formContainerPanel.add(formPanel, BorderLayout.NORTH);
        formContainerPanel.add(_tabbedPane, BorderLayout.CENTER);
        formContainerPanel.add(toolBarPanel, BorderLayout.SOUTH);

        final DCPanel outerPanel = new DCPanel();
        outerPanel.setLayout(new BorderLayout());

        outerPanel.add(new DescriptionLabel("Use this dialog to connect to your relational database. "
                + "Connections are made using a Connection string (aka. a JDBC URL) and a set of credentials. "
                + "These can be entered directly in the 'Generic connection parameters' panel. "
                + "If you see an additional panel, this provides an alternative means of connecting "
                + "without having to know the URL format for your specific database type."), BorderLayout.NORTH);
        outerPanel.add(formContainerPanel, BorderLayout.CENTER);

        outerPanel.setPreferredSize(getDialogWidth(), 500);

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
