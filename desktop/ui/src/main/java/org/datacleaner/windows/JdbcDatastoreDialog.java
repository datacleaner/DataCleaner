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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.sql.DataSource;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.event.DocumentEvent;

import org.apache.metamodel.util.FileHelper;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.database.DatabaseDescriptorImpl;
import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.database.DatabaseDriverDescriptor;
import org.datacleaner.guice.Nullable;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.DCDocumentListener;
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
import org.datacleaner.widgets.database.CubridDatabaseConnectionPresenter;
import org.datacleaner.widgets.database.DatabaseConnectionPresenter;
import org.datacleaner.widgets.database.DefaultDatabaseConnectionPresenter;
import org.datacleaner.widgets.database.H2DatabaseConnectionPresenter;
import org.datacleaner.widgets.database.HiveDatabaseConnectionPresenter;
import org.datacleaner.widgets.database.MysqlDatabaseConnectionPresenter;
import org.datacleaner.widgets.database.OracleDatabaseConnectionPresenter;
import org.datacleaner.widgets.database.PostgresqlDatabaseConnectionPresenter;
import org.datacleaner.widgets.database.SQLServerDatabaseConnectionPresenter;
import org.datacleaner.widgets.tabs.CloseableTabbedPane;
import org.jdesktop.swingx.JXTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcDatastoreDialog extends AbstractDatastoreDialog<JdbcDatastore> {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(JdbcDatastoreDialog.class);

    public static final int TEXT_FIELD_WIDTH = 30;

    /**
     * Number of connections to try to create (in case of non-multiple
     * connections, this is just the number of handles to the same connection).
     */
    private static final int TEST_CONNECTION_COUNT = 4;

    private static final String MANAGE_DATABASE_DRIVERS = "Manage database drivers...";
    private static final ImageManager imageManager = ImageManager.get();

    private final DatabaseDriverCatalog _databaseDriverCatalog;
    private final JXTextField _driverClassNameTextField;
    private final DCCheckBox<Object> _multipleConnectionsCheckBox;
    private final DCComboBox<Object> _databaseDriverComboBox;
    private final Provider<OptionsDialog> _optionsDialogProvider;
    private final CloseableTabbedPane _tabbedPane;
    private final DatabaseConnectionPresenter[] _connectionPresenters;

    @Inject
    protected JdbcDatastoreDialog(@Nullable JdbcDatastore originalDatastore, MutableDatastoreCatalog catalog,
            WindowContext windowContext, Provider<OptionsDialog> optionsDialogProvider,
            DatabaseDriverCatalog databaseDriverCatalog, UserPreferences userPreferences) {
        super(originalDatastore, catalog, windowContext, userPreferences);

        setSaveButtonEnabled(false);

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

        if (originalDatastore == null) {
            // remove connection url templates
            setSelectedDatabase((DatabaseDriverDescriptor) null);
        } else {
            _multipleConnectionsCheckBox.setSelected(originalDatastore.isMultipleConnections());

            // the database driver has to be set as the first thing, because the
            // combobox's action listener will set other field's values as well.
            DatabaseDriverDescriptor databaseDriver = DatabaseDriverCatalog
                    .getDatabaseDriverByDriverClassName(originalDatastore.getDriverClass());
            _databaseDriverComboBox.setSelectedItem(databaseDriver);

            _datastoreNameTextField.setText(originalDatastore.getName());
            _datastoreNameTextField.setEnabled(false);

            _connectionPresenters[0].initialize(originalDatastore);

            _driverClassNameTextField.setText(originalDatastore.getDriverClass());
        }

        _databaseDriverComboBox.addListener(new Listener<Object>() {

            @Override
            public void onItemSelected(Object item) {
                validateAndUpdate();
            }
        });

        _driverClassNameTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                validateAndUpdate();
            }
        });

    }

    protected boolean validateForm() {
        final String datastoreName = _datastoreNameTextField.getText();
        if (StringUtils.isNullOrEmpty(datastoreName)) {
            setStatusError("Please enter a datastore name");
            return false;
        }

        final DatabaseDriverDescriptor databaseDriverDescriptor = (DatabaseDescriptorImpl) _databaseDriverComboBox
                .getSelectedItem();
        if (databaseDriverDescriptor == null) {
            String databaseDriverClass = _driverClassNameTextField.getText();
            if (StringUtils.isNullOrEmpty(databaseDriverClass)) {
                setStatusError("Please specify database driver class or choose one from the list");
                return false;
            }
        }

        setStatusValid();
        return true;
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
            if (getOriginalDatastore() != null) {
                accepted = customPresenter.initialize(getOriginalDatastore());
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

        switch (databaseName) {
        case DatabaseDriverCatalog.DATABASE_NAME_MYSQL:
            result = new MysqlDatabaseConnectionPresenter();
            break;
        case DatabaseDriverCatalog.DATABASE_NAME_POSTGRESQL:
            result = new PostgresqlDatabaseConnectionPresenter();
            break;
        case DatabaseDriverCatalog.DATABASE_NAME_ORACLE:
            result = new OracleDatabaseConnectionPresenter();
            break;
        case DatabaseDriverCatalog.DATABASE_NAME_MICROSOFT_SQL_SERVER_JTDS:
            result = new SQLServerDatabaseConnectionPresenter();
            break;
        case DatabaseDriverCatalog.DATABASE_NAME_CUBRID:
            result = new CubridDatabaseConnectionPresenter();
            break;
        case DatabaseDriverCatalog.DATABASE_NAME_H2:
            result = new H2DatabaseConnectionPresenter();
            break;
        case DatabaseDriverCatalog.DATABASE_NAME_HIVE:
            result = new HiveDatabaseConnectionPresenter();
            break;
        default:
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

        final JButton testButton = WidgetFactory.createDefaultButton(getTestButtonText(), IconUtils.ACTION_REFRESH);
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

        _multipleConnectionsCheckBox.addListener(new DCCheckBox.Listener<Object>() {
            @Override
            public void onItemSelected(Object item, boolean selected) {
                testButton.setText(getTestButtonText());
            }
        });

        final DCPanel buttonPanel = getButtonPanel();
        buttonPanel.add(testButton);

        final DCPanel formContainerPanel = new DCPanel(WidgetUtils.COLOR_ALTERNATIVE_BACKGROUND);
        formContainerPanel.setLayout(new BorderLayout());
        formContainerPanel.setBorder(WidgetUtils.BORDER_TOP_PADDING);
        formContainerPanel.add(formPanel, BorderLayout.NORTH);
        formContainerPanel.add(_tabbedPane, BorderLayout.CENTER);
        formContainerPanel.add(buttonPanel, BorderLayout.SOUTH);

        final DCPanel outerPanel = new DCPanel();
        outerPanel.setLayout(new BorderLayout());

        outerPanel.add(new DescriptionLabel("Use this dialog to connect to your relational database. "
                + "Connections are made using a Connection string (aka. a JDBC URL) and a set of credentials. "
                + "These can be entered directly in the 'Generic connection parameters' panel. "
                + "If you see an additional panel, this provides an alternative means of connecting "
                + "without having to know the URL format for your specific database type."), BorderLayout.NORTH);
        outerPanel.add(formContainerPanel, BorderLayout.CENTER);
        // Uncomment to add the status bar. It is hidden by default as no
        // validation on generic and specific connection settings is in place,
        // so the status bar is displaying "Datastore ready" in situations when
        // it is actually not ready.
        // outerPanel.add(_statusLabel, BorderLayout.SOUTH);

        outerPanel.setPreferredSize(getDialogWidth(), 500);

        return outerPanel;
    }

    private String getTestButtonText() {
        if (_multipleConnectionsCheckBox.isSelected()) {
            return "Test connections";
        }
        return "Test connection";
    }

    @Override
    protected JdbcDatastore createDatastore() {
        final String datastoreName = _datastoreNameTextField.getText();
        if (StringUtils.isNullOrEmpty(datastoreName)) {
            throw new IllegalStateException("Please enter a datastore name");
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
    protected String getDatastoreIconPath() {
        return IconUtils.GENERIC_DATASTORE_IMAGEPATH;
    }
}
