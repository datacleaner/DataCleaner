/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.swing.Icon;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.database.DatabaseDriverDescriptor;
import org.datacleaner.database.DatabaseDriverState;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.table.DCTable;

/**
 * Represents the panel in the Options dialog where the user can get an overview
 * and configure database drivers.
 */
public class DatabaseDriversPanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private final ImageManager imageManager = ImageManager.get();
    private final Set<String> _usedDriverClassNames = new HashSet<>();
    private final DatabaseDriverCatalog _databaseDriverCatalog;

    @Inject
    protected DatabaseDriversPanel(final DataCleanerConfiguration configuration, final DatabaseDriverCatalog databaseDriverCatalog) {
        super(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        _databaseDriverCatalog = databaseDriverCatalog;
        setLayout(new BorderLayout());

        final DatastoreCatalog datastoreCatalog = configuration.getDatastoreCatalog();
        final String[] datastoreNames = datastoreCatalog.getDatastoreNames();
        for (final String name : datastoreNames) {
            final Datastore datastore = datastoreCatalog.getDatastore(name);
            if (datastore instanceof JdbcDatastore) {
                final String driverClass = ((JdbcDatastore) datastore).getDriverClass();
                if (driverClass != null) {
                    _usedDriverClassNames.add(driverClass);
                }
            }
        }

        updateComponents();
    }

    private void updateComponents() {
        this.removeAll();

        final DCTable table = getDatabaseDriverTable();
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
        final TableModel tableModel =
                new DefaultTableModel(new String[] { "", "Database", "Driver class", "Installed?", "Used?" },
                        databaseDrivers.size());

        final DCTable table = new DCTable(tableModel);

        final Icon validIcon = imageManager.getImageIcon(IconUtils.STATUS_VALID, IconUtils.ICON_SIZE_SMALL);
        final Icon invalidIcon = imageManager.getImageIcon(IconUtils.STATUS_ERROR, IconUtils.ICON_SIZE_SMALL);

        final int installedCol = 3;
        final int usedCol = 4;
        int row = 0;
        for (final DatabaseDriverDescriptor dd : databaseDrivers) {
            final String driverClassName = dd.getDriverClassName();
            final String displayName = dd.getDisplayName();

            final Icon driverIcon =
                    imageManager.getImageIcon(DatabaseDriverCatalog.getIconImagePath(dd), IconUtils.ICON_SIZE_SMALL);

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
                // TODO: Maybe indicate that it is missing
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

    private boolean isUsed(final String driverClassName) {
        return _usedDriverClassNames.contains(driverClassName);
    }
}
