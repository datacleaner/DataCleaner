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
package org.datacleaner.metadata;

import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreConnection;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;

/**
 * A metadata object representing a foreign key
 */
public final class ForeignKey {

    private final String _foreignDatastoreName;
    private final String _foreignSchemaName;
    private final String _foreignTableName;
    private final String _foreignColumnName;

    public ForeignKey(String foreignDatastoreName, String foreignSchemaName, String foreignTableName,
            String foreignColumnName) {
        _foreignDatastoreName = foreignDatastoreName;
        _foreignSchemaName = foreignSchemaName;
        _foreignTableName = foreignTableName;
        _foreignColumnName = foreignColumnName;
    }

    public String getForeignColumnName() {
        return _foreignColumnName;
    }

    public String getForeignDatastoreName() {
        return _foreignDatastoreName;
    }

    public String getForeignSchemaName() {
        return _foreignSchemaName;
    }

    public String getForeignTableName() {
        return _foreignTableName;
    }

    public Column resolveForeignColumn(DatastoreCatalog datastoreCatalog) {
        Datastore datastore = datastoreCatalog.getDatastore(getForeignDatastoreName());
        if (datastore == null) {
            return null;
        }
        try (DatastoreConnection connection = datastore.openConnection()) {
            DataContext dataContext = connection.getDataContext();
            Schema schema = dataContext.getSchemaByName(getForeignSchemaName());
            if (schema == null) {
                return null;
            }
            Table table = schema.getTableByName(getForeignTableName());
            if (table == null) {
                return null;
            }
            Column column = table.getColumnByName(getForeignColumnName());
            return column;
        }
    }
}
