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
package org.datacleaner.util;

import java.io.Serializable;

import org.datacleaner.metamodel.schema.Column;
import org.datacleaner.metamodel.schema.ColumnType;
import org.datacleaner.metamodel.schema.ColumnTypeImpl;
import org.datacleaner.metamodel.schema.MutableColumn;
import org.datacleaner.metamodel.schema.MutableSchema;
import org.datacleaner.metamodel.schema.MutableTable;
import org.datacleaner.metamodel.schema.Schema;
import org.datacleaner.metamodel.schema.Table;
import org.datacleaner.metamodel.schema.TableType;

/**
 * Serializable snapshot of a metamodel {@link Column}, retaining only its
 * identifying names and type. Used when persisting objects that reference
 * metamodel schema objects, since these are not java.io.Serializable.
 *
 * Deserialization produces a {@link MutableColumn} with the same identifying
 * properties (name, column number, type, table name, table type and schema
 * name) so that it compares equal to other columns deserialized from the same
 * snapshot family.
 */
public class ColumnSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String _name;
    private final int _columnNumber;
    private final String _columnTypeName;
    private final String _tableName;
    private final String _tableTypeName;
    private final String _schemaName;

    public ColumnSnapshot(final Column column) {
        _name = column.getName();
        _columnNumber = column.getColumnNumber();
        _columnTypeName = column.getType().getName();

        final Table table = column.getTable();
        _tableName = table == null ? null : table.getName();
        _tableTypeName = table == null ? null : table.getType().name();
        _schemaName = table == null || table.getSchema() == null ? null : table.getSchema().getName();
    }

    public Column rebuild() {
        ColumnType columnType = ColumnTypeImpl.valueOf(_columnTypeName);
        if (columnType == null) {
            columnType = ColumnType.OTHER;
        }
        final MutableColumn column = new MutableColumn(_name, columnType);
        column.setColumnNumber(_columnNumber);

        if (_tableName != null) {
            final MutableTable table = _tableTypeName == null
                    ? new MutableTable(_tableName)
                    : new MutableTable(_tableName, TableType.valueOf(_tableTypeName));
            if (_schemaName != null) {
                final MutableSchema schema = new MutableSchema(_schemaName);
                schema.addTable(table);
                table.setSchema(schema);
            }
            column.setTable(table);
        }
        return column;
    }
}
