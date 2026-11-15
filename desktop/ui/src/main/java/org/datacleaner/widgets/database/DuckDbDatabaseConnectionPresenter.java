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
package org.datacleaner.widgets.database;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.database.DatabaseDriverDescriptor;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.ExtensionFilter;
import org.datacleaner.util.FileFilters;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.FilenameTextField;

/**
 * A {@link DatabaseConnectionPresenter} for DuckDB databases. DuckDB stores its
 * data in a single file, so this presenter allows the user to select that file
 * instead of entering a JDBC connection string.
 */
public class DuckDbDatabaseConnectionPresenter extends AbstractDatabaseConnectionPresenter {

    private static final String JDBC_URL_PREFIX = "jdbc:duckdb:";
    private static final FileFilter DUCKDB_FILE_FILTER =
            FileFilters.combined("DuckDB database files (.duckdb, .db)",
                    new ExtensionFilter("DuckDB database (.duckdb)", ".duckdb"),
                    new ExtensionFilter("Database files (.db)", ".db"));

    private final FilenameTextField _databaseFileTextField;

    public DuckDbDatabaseConnectionPresenter(final File directory) {
        _databaseFileTextField = new FilenameTextField(directory, true);
        _databaseFileTextField.addChoosableFileFilter(DUCKDB_FILE_FILTER);
        _databaseFileTextField.addChoosableFileFilter(FileFilters.ALL);
        _databaseFileTextField.setSelectedFileFilter(DUCKDB_FILE_FILTER);
    }

    @Override
    public boolean initialize(final JdbcDatastore datastore) {
        final String jdbcUrl = datastore.getJdbcUrl();
        if (jdbcUrl == null || !jdbcUrl.startsWith(JDBC_URL_PREFIX)) {
            return false;
        }

        super.initialize(datastore);
        _databaseFileTextField.setFilename(jdbcUrl.substring(JDBC_URL_PREFIX.length()));
        return true;
    }

    @Override
    protected int layoutGridBagAboveCredentials(final DCPanel panel) {
        final int row = 0;
        WidgetUtils.addToGridBag(DCLabel.dark("Database file:"), panel, 0, row);
        WidgetUtils.addToGridBag(_databaseFileTextField, panel, 1, row, 1.0, 0.0);
        return row;
    }

    @Override
    public String getJdbcUrl() {
        final String filename = _databaseFileTextField.getFilename();
        if (StringUtils.isNullOrEmpty(filename)) {
            return JDBC_URL_PREFIX;
        }
        return JDBC_URL_PREFIX + filename.trim();
    }

    @Override
    public void setSelectedDatabaseDriver(final DatabaseDriverDescriptor driver) {
        // do nothing
    }
}
