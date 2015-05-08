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
package org.datacleaner.job.output;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.MutableTable;
import org.apache.metamodel.schema.Table;
import org.datacleaner.api.OutputDataStream;

final class OutputDataStreamBuilderImpl implements OutputDataStreamBuilder {

    private final String _name;
    private final MutableTable _table;

    public OutputDataStreamBuilderImpl(String name) {
        _name = name;
        _table = new MutableTable(name);
    }

    @Override
    public OutputDataStream toOutputDataStream() {
        if (_table.getColumnCount() == 0) {
            throw new IllegalStateException("No columns defined in OutputDataStream '" + _name + "'");
        }
        return new PushOutputDataStream(_name, _table);
    }

    @Override
    public OutputDataStreamBuilder likeTable(Table table) {
        final Column[] existingColumns = _table.getColumns();
        for (Column column : existingColumns) {
            _table.removeColumn(column);
        }
        final Column[] newColumns = table.getColumns();
        for (Column column : newColumns) {
            withColumn(column.getName(), column.getType());
        }
        return this;
    }

    @Override
    public OutputDataStreamBuilder withColumn(String name, ColumnType type) {
        final int columnNumber = _table.getColumnCount() + 1;
        final MutableColumn column = new MutableColumn(name, type, _table, columnNumber, true);
        _table.addColumn(column);
        return this;
    }

}
