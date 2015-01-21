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
package org.datacleaner.beans.writers;

import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.FileDatastore;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.Func;

/**
 * Default implementation of {@link WriteDataResult}.
 * 
 * 
 */
public final class WriteDataResultImpl implements WriteDataResult {

    private static final long serialVersionUID = 1L;

    private final int _writtenRowCount;
    private final int _updatesCount;
    private final String _datastoreName;
    private final String _schemaName;
    private final String _tableName;
    private final int _errorRowCount;

    private final transient Func<DatastoreCatalog, Datastore> _datastoreFunc;
    private final transient FileDatastore _errorDatastore;

    public WriteDataResultImpl(final int writtenRowCount, final int updatesCount, final int errorRowCount) {
        this(writtenRowCount, updatesCount, null, null, null, errorRowCount, null);
    }

    public WriteDataResultImpl(final int writtenRowCount, final Datastore datastore, final String schemaName,
            final String tableName) {
        this(writtenRowCount, datastore, schemaName, tableName, 0, null);
    }

    public WriteDataResultImpl(final int writtenRowCount, final Datastore datastore, final String schemaName,
            final String tableName, final int errorRowCount, final FileDatastore errorDatastore) {
        this(writtenRowCount, 0, datastore, schemaName, tableName, errorRowCount, errorDatastore);
    }

    public WriteDataResultImpl(final int writtenRowCount, final int updatesCount, final Datastore datastore,
            final String schemaName, final String tableName, final int errorRowCount, final FileDatastore errorDatastore) {
        _writtenRowCount = writtenRowCount;
        _updatesCount = updatesCount;
        _schemaName = schemaName;
        _tableName = tableName;
        _datastoreName = (datastore == null ? null : datastore.getName());
        _datastoreFunc = new Func<DatastoreCatalog, Datastore>() {
            @Override
            public Datastore eval(DatastoreCatalog catalog) {
                return datastore;
            }
        };
        _errorRowCount = errorRowCount;
        _errorDatastore = errorDatastore;
    }

    public WriteDataResultImpl(final int writtenRowCount, final String datastoreName, final String schemaName,
            final String tableName) {
        this(writtenRowCount, 0, datastoreName, schemaName, tableName);
    }

    public WriteDataResultImpl(final int writtenRowCount, final int updatesCount, final String datastoreName,
            final String schemaName, final String tableName) {
        _writtenRowCount = writtenRowCount;
        _updatesCount = updatesCount;
        _schemaName = schemaName;
        _tableName = tableName;
        _datastoreName = datastoreName;
        _datastoreFunc = new Func<DatastoreCatalog, Datastore>() {
            @Override
            public Datastore eval(DatastoreCatalog catalog) {
                return catalog.getDatastore(datastoreName);
            }
        };
        _errorRowCount = 0;
        _errorDatastore = null;
    }
    
    @Override
    public FileDatastore getErrorDatastore() {
        return _errorDatastore;
    }

    @Override
    public int getUpdatesCount() {
        return _updatesCount;
    }

    @Override
    public int getErrorRowCount() {
        return _errorRowCount;
    }

    @Override
    public int getWrittenRowCount() {
        return _writtenRowCount;
    }

    @Override
    public Datastore getDatastore(DatastoreCatalog datastoreCatalog) {
        if (_datastoreFunc == null) {
            if (_datastoreName == null) {
                return null;
            }
            return datastoreCatalog.getDatastore(_datastoreName);
        }
        return _datastoreFunc.eval(datastoreCatalog);
    }

    @Override
    public Table getPreviewTable(Datastore datastore) {
        DatastoreConnection con = datastore.openConnection();
        try {
            return con.getSchemaNavigator().convertToTable(_schemaName, _tableName);
        } finally {
            con.close();
        }
    }

    @Override
    public String toString() {
        String message = _writtenRowCount + " inserts executed";

        if (_updatesCount > 0) {
            message = message + "\n" + _updatesCount + " updates executed";
        }

        if (_errorRowCount > 0) {
            if (_errorDatastore == null) {
                message = message + "\n - WARNING! " + _errorRowCount + " record failed";
            } else {
                message = message + "\n - WARNING! " + _errorRowCount + " record failed, written to file: "
                        + _errorDatastore.getFilename();
            }
        }
        return message;
    }
}
