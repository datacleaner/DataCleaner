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
package org.datacleaner.windows;

import javax.inject.Inject;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.DuckDbDatastore;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.guice.Nullable;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;

/**
 * Datastore dialog for DuckDB databases. Similar to the generic JDBC dialog,
 * but defaults to and enforces the DuckDB driver.
 */
public final class DuckDbDatastoreDialog extends JdbcDatastoreDialog {

    private static final long serialVersionUID = 1L;

    @Inject
    protected DuckDbDatastoreDialog(@Nullable final DuckDbDatastore originalDatastore,
            final MutableDatastoreCatalog catalog, final WindowContext windowContext,
            final javax.inject.Provider<OptionsDialog> optionsDialogProvider,
            final DatabaseDriverCatalog databaseDriverCatalog, final UserPreferences userPreferences) {
        super(originalDatastore, catalog, windowContext, optionsDialogProvider, databaseDriverCatalog,
                userPreferences);

        _driverClassNameTextField.setText(DuckDbDatastore.DRIVER_CLASS);
        _driverClassNameTextField.setEnabled(false);

        final JdbcDatastore datastoreToPresent =
                originalDatastore != null ? originalDatastore
                        : new JdbcDatastore("dummy", "jdbc:duckdb:", DuckDbDatastore.DRIVER_CLASS);
        _connectionPresenters[0].initialize(datastoreToPresent);
    }

    @Override
    protected JdbcDatastore createDatastore() {
        final String datastoreName = _datastoreNameTextField.getText();
        if (datastoreName.trim().isEmpty()) {
            throw new IllegalStateException("Please enter a datastore name");
        }

        final String connectionString = _connectionPresenters[0].getJdbcUrl();
        final boolean multipleConnections = _multipleConnectionsCheckBox.isSelected();

        return new DuckDbDatastore(datastoreName, connectionString, multipleConnections);
    }

    @Override
    protected String getBannerTitle() {
        return "DuckDB database";
    }

    @Override
    public String getWindowTitle() {
        return "DuckDB database | Datastore";
    }
}
