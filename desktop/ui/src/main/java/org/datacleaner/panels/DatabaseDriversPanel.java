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
package org.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.vfs2.FileObject;
import org.apache.http.client.HttpClient;
import org.datacleaner.actions.DownloadFilesActionListener;
import org.datacleaner.actions.FileDownloadListener;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.database.DatabaseDriverDescriptor;
import org.datacleaner.database.DatabaseDriverState;
import org.datacleaner.database.UserDatabaseDriver;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.util.http.SimpleWebServiceHttpClient;
import org.datacleaner.util.http.WebServiceHttpClient;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.PopupButton;
import org.datacleaner.widgets.table.DCTable;
import org.datacleaner.windows.AddDatabaseDriverDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the panel in the Options dialog where the user can get an overview
 * and configure database drivers.
 */
public class DatabaseDriversPanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(DatabaseDriversPanel.class);
    private final ImageManager imageManager = ImageManager.get();
    private final Set<String> _usedDriverClassNames = new HashSet<String>();
    private final DatabaseDriverCatalog _databaseDriverCatalog;
    private final WindowContext _windowContext;
    private final UserPreferences _userPreferences;
    private final HttpClient _httpClient;

    @Inject
    protected DatabaseDriversPanel(AnalyzerBeansConfiguration configuration, WindowContext windowContext,
            UserPreferences userPreferences, DatabaseDriverCatalog databaseDriverCatalog, HttpClient httpClient) {
        super(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        _windowContext = windowContext;
        _userPreferences = userPreferences;
        _databaseDriverCatalog = databaseDriverCatalog;
        _httpClient = httpClient;
        setLayout(new BorderLayout());

        DatastoreCatalog datastoreCatalog = configuration.getDatastoreCatalog();
        String[] datastoreNames = datastoreCatalog.getDatastoreNames();
        for (String name : datastoreNames) {
            Datastore datastore = datastoreCatalog.getDatastore(name);
            if (datastore instanceof JdbcDatastore) {
                String driverClass = ((JdbcDatastore) datastore).getDriverClass();
                if (driverClass != null) {
                    _usedDriverClassNames.add(driverClass);
                }
            }
        }

        updateComponents();
    }

    private void updateComponents() {
        this.removeAll();

        final PopupButton addDriverButton = WidgetFactory.createDefaultPopupButton("Add database driver",
                IconUtils.ACTION_ADD);
        final JPopupMenu addDriverMenu = addDriverButton.getMenu();

        final JMenu automaticDownloadAndInstallMenu = new JMenu("Automatic download and install");
        automaticDownloadAndInstallMenu.setIcon(imageManager.getImageIcon("images/actions/download.png"));

        final List<DatabaseDriverDescriptor> drivers = _databaseDriverCatalog.getDatabaseDrivers();
        for (DatabaseDriverDescriptor dd : drivers) {
            final String[] urls = dd.getDownloadUrls();
            if (urls != null && _databaseDriverCatalog.getState(dd) == DatabaseDriverState.NOT_INSTALLED) {
                final JMenuItem downloadAndInstallMenuItem = WidgetFactory.createMenuItem(dd.getDisplayName(),
                        dd.getIconImagePath());
                downloadAndInstallMenuItem.addActionListener(createDownloadActionListener(dd));
                automaticDownloadAndInstallMenu.add(downloadAndInstallMenuItem);
            }
        }

        if (automaticDownloadAndInstallMenu.getMenuComponentCount() == 0) {
            automaticDownloadAndInstallMenu.setEnabled(false);
        }

        final JMenuItem localJarFilesMenuItem = WidgetFactory.createMenuItem("Local JAR file(s)...",
                IconUtils.FILE_ARCHIVE);
        localJarFilesMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddDatabaseDriverDialog dialog = new AddDatabaseDriverDialog(_databaseDriverCatalog,
                        DatabaseDriversPanel.this, _windowContext, _userPreferences);
                dialog.setVisible(true);
            }
        });

        addDriverMenu.add(automaticDownloadAndInstallMenu);
        addDriverMenu.add(localJarFilesMenuItem);

        final DCTable table = getDatabaseDriverTable();
        this.add(DCPanel.flow(Alignment.RIGHT, addDriverButton), BorderLayout.NORTH);
        this.add(table.toPanel(), BorderLayout.CENTER);
    }

    /**
     * Called by other components in case a driver list update is needed.
     */
    public void updateDriverList() {
        updateComponents();
    }

    private DCTable getDatabaseDriverTable() {
        final List<DatabaseDriverDescriptor> databaseDrivers = _databaseDriverCatalog.getDatabaseDrivers();
        final TableModel tableModel = new DefaultTableModel(new String[] { "", "Database", "Driver class",
                "Installed?", "Used?" }, databaseDrivers.size());

        final DCTable table = new DCTable(tableModel);

        final Icon validIcon = imageManager.getImageIcon(IconUtils.STATUS_VALID, IconUtils.ICON_SIZE_SMALL);
        final Icon invalidIcon = imageManager.getImageIcon(IconUtils.STATUS_ERROR, IconUtils.ICON_SIZE_SMALL);

        final int installedCol = 3;
        final int usedCol = 4;
        int row = 0;
        for (final DatabaseDriverDescriptor dd : databaseDrivers) {
            final String driverClassName = dd.getDriverClassName();
            final String displayName = dd.getDisplayName();

            final Icon driverIcon = imageManager.getImageIcon(DatabaseDriverCatalog.getIconImagePath(dd),
                    IconUtils.ICON_SIZE_SMALL);

            tableModel.setValueAt(driverIcon, row, 0);
            tableModel.setValueAt(displayName, row, 1);
            tableModel.setValueAt(driverClassName, row, 2);
            tableModel.setValueAt("", row, 3);
            tableModel.setValueAt("", row, 4);

            final DatabaseDriverState state = _databaseDriverCatalog.getState(dd);
            if (state == DatabaseDriverState.INSTALLED_WORKING) {
                tableModel.setValueAt(validIcon, row, installedCol);
            } else if (state == DatabaseDriverState.INSTALLED_NOT_WORKING) {
                tableModel.setValueAt(invalidIcon, row, installedCol);
            } else if (state == DatabaseDriverState.NOT_INSTALLED) {
                final String[] downloadUrls = dd.getDownloadUrls();
                if (downloadUrls != null) {
                    final DCPanel buttonPanel = new DCPanel();
                    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 0));

                    final JButton downloadButton = WidgetFactory.createSmallButton("images/actions/download.png");
                    downloadButton.setToolTipText("Download and install the driver for " + dd.getDisplayName());

                    downloadButton.addActionListener(createDownloadActionListener(dd));
                    buttonPanel.add(downloadButton);

                    tableModel.setValueAt(buttonPanel, row, installedCol);
                }
            }

            if (isUsed(driverClassName)) {
                tableModel.setValueAt(validIcon, row, usedCol);
            }

            row++;
        }

        table.setAlignment(installedCol, Alignment.CENTER);
        table.setAlignment(usedCol, Alignment.CENTER);

        table.setRowHeight(IconUtils.ICON_SIZE_SMALL + 4);
        table.getColumn(0).setMaxWidth(IconUtils.ICON_SIZE_SMALL + 4);
        table.getColumn(installedCol).setMaxWidth(84);
        table.getColumn(usedCol).setMaxWidth(70);
        table.setColumnControlVisible(false);
        return table;
    }

    private boolean isUsed(String driverClassName) {
        return _usedDriverClassNames.contains(driverClassName);
    }

    private ActionListener createDownloadActionListener(final DatabaseDriverDescriptor dd) {
        final FileDownloadListener downloadListener = new FileDownloadListener() {
            @Override
            public void onFilesDownloaded(FileObject[] files) {
                final String driverClassName = dd.getDriverClassName();

                logger.info("Registering and loading driver '{}' in files '{}'", driverClassName, files);

                final UserDatabaseDriver userDatabaseDriver = new UserDatabaseDriver(files, driverClassName);
                _userPreferences.getDatabaseDrivers().add(userDatabaseDriver);

                try {
                    userDatabaseDriver.loadDriver();
                } catch (IllegalStateException e) {
                    WidgetUtils.showErrorMessage("Error while loading driver", "Error message: " + e.getMessage(), e);
                }
                updateDriverList();
            }
        };

        final WebServiceHttpClient httpClient = new SimpleWebServiceHttpClient(_httpClient);
        return new DownloadFilesActionListener(dd.getDownloadUrls(), downloadListener, _windowContext, httpClient,
                _userPreferences);
    }
}
