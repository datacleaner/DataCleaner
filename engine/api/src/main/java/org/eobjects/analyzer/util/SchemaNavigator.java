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
package org.eobjects.analyzer.util;

import java.util.Arrays;

import org.eobjects.analyzer.connection.Datastore;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;

/**
 * A convenient component used for exploring/navigating the schema of a
 * {@link Datastore}. It is preferred to use this component instead of the
 * {@link DataContext} directly, since it is shared amongst connections while a
 * DataContext may be created per connection (depending on the datastore type).
 */
public final class SchemaNavigator {

    private final DataContext dataContext;

    public SchemaNavigator(DataContext dataContext) {
        this.dataContext = dataContext;
    }

    public void refreshSchemas() {
        dataContext.refreshSchemas();
    }

    public Schema convertToSchema(String schemaName) {
        return dataContext.getSchemaByName(schemaName);
    }

    public Schema[] getSchemas() {
        return dataContext.getSchemas();
    }

    public Schema getDefaultSchema() {
        return dataContext.getDefaultSchema();
    }

    public Schema getSchemaByName(String name) {
        return dataContext.getSchemaByName(name);
    }

    public Table convertToTable(String schemaName, String tableName) {
        final Schema schema;
        if (schemaName == null) {
            schema = getDefaultSchema();
        } else {
            schema = getSchemaByName(schemaName);
        }

        if (schema == null) {
            throw new IllegalArgumentException("Schema " + schemaName + " not found. Available schema names are: "
                    + Arrays.toString(dataContext.getSchemaNames()));
        }

        final Table table;
        if (tableName == null) {
            if (schema.getTableCount() == 1) {
                table = schema.getTables()[0];
            } else {
                throw new IllegalArgumentException(
                        "No table name specified, and multiple options exist. Available table names are: "
                                + Arrays.toString(schema.getTableNames()));
            }
        } else {
            table = schema.getTableByName(tableName);
        }

        if (table == null) {
            throw new IllegalArgumentException("Table not found. Available table names are: "
                    + Arrays.toString(schema.getTableNames()));
        }

        return table;
    }

    public Column[] convertToColumns(String schemaName, String tableName, String[] columnNames) {
        if (columnNames == null) {
            return null;
        }

        if (columnNames.length == 0) {
            return new Column[0];
        }

        final Table table = convertToTable(schemaName, tableName);

        final Column[] columns = new Column[columnNames.length];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = table.getColumnByName(columnNames[i]);
        }

        return columns;
    }

    public Schema[] convertToSchemas(String[] schemaNames) {
        Schema[] result = new Schema[schemaNames.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = convertToSchema(schemaNames[i]);
        }
        return result;
    }

    public Table[] convertToTables(String[] tableNames) {
        Table[] result = new Table[tableNames.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = convertToTable(tableNames[i]);
        }
        return result;
    }

    public Table convertToTable(String tableName) {
        return dataContext.getTableByQualifiedLabel(tableName);
    }

    public Column[] convertToColumns(String[] columnNames) {
        Column[] result = new Column[columnNames.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = convertToColumn(columnNames[i]);
        }
        return result;
    }

    public Column convertToColumn(String columnName) {
        return dataContext.getColumnByQualifiedLabel(columnName);
    }
}
