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
import java.util.ArrayList;
import java.util.List;

import org.datacleaner.metamodel.schema.Column;
import org.datacleaner.metamodel.schema.MutableSchema;
import org.datacleaner.metamodel.schema.MutableTable;
import org.datacleaner.metamodel.schema.Schema;
import org.datacleaner.metamodel.schema.Table;
import org.datacleaner.metamodel.schema.TableType;

/**
 * Serializable snapshot of a metamodel {@link Table}, retaining only its
 * identifying names and the snapshots of its columns. Used when persisting
 * objects that reference metamodel schema objects, since these are not
 * java.io.Serializable.
 */
public final class TableSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String _name;
    private final String _tableTypeName;
    private final String _schemaName;
    private final List<ColumnSnapshot> _columnSnapshots;

    public TableSnapshot(final Table table) {
        _name = table.getName();
        _tableTypeName = table.getType() == null ? null : table.getType().name();
        final Schema schema = table.getSchema();
        _schemaName = schema == null ? null : schema.getName();
        _columnSnapshots = new ArrayList<>();
        for (final Column column : table.getColumns()) {
            _columnSnapshots.add(new ColumnSnapshot(column));
        }
    }

    public Table rebuild() {
        final MutableTable table = _tableTypeName == null
                ? new MutableTable(_name)
                : new MutableTable(_name, TableType.valueOf(_tableTypeName));
        if (_schemaName != null) {
            final MutableSchema schema = new MutableSchema(_schemaName);
            schema.addTable(table);
            table.setSchema(schema);
        }
        for (final ColumnSnapshot columnSnapshot : _columnSnapshots) {
            table.addColumn(columnSnapshot.rebuild());
        }
        return table;
    }
}
