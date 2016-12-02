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
import org.apache.metamodel.schema.ColumnTypeImpl;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.MutableSchema;
import org.apache.metamodel.schema.MutableTable;
import org.apache.metamodel.schema.Table;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.OutputDataStream;

final class OutputDataStreamBuilderImpl implements OutputDataStreamBuilder {

    private final String _name;
    private final MutableTable _table;

    OutputDataStreamBuilderImpl(final String name) {
        _name = name;
        final MutableSchema schema = new MutableSchema();
        schema.setName(null);
        _table = new MutableTable(name, schema);
        schema.addTable(_table);
    }

    @Override
    public OutputDataStream toOutputDataStream() {
        if (_table.getColumnCount() == 0) {
            throw new IllegalStateException("No columns defined in OutputDataStream '" + _name + "'");
        }
        return new PushOutputDataStream(_name, _table);
    }

    @Override
    public OutputDataStreamBuilder likeTable(final Table table) {
        final Column[] existingColumns = _table.getColumns();
        for (final Column column : existingColumns) {
            _table.removeColumn(column);
        }
        final Column[] newColumns = table.getColumns();
        for (final Column column : newColumns) {
            withColumn(column.getName(), column.getType());
        }
        return this;
    }

    @Override
    public OutputDataStreamBuilder withColumn(final String name, final ColumnType type) {
        final int columnNumber = _table.getColumnCount() + 1;
        final MutableColumn column = new MutableColumn(name, type, _table, columnNumber, true);
        _table.addColumn(column);
        return this;
    }

    @Override
    public OutputDataStreamBuilder withColumnLike(final Column column) {
        return withColumn(column.getName(), column.getType());
    }

    @Override
    public OutputDataStreamBuilder withColumnLike(final InputColumn<?> column) {
        if (column.isPhysicalColumn()) {
            return withColumnLike(column.getPhysicalColumn());
        } else {
            final ColumnType columnType = ColumnTypeImpl.convertColumnType(column.getDataType());
            return withColumn(column.getName(), columnType);
        }
    }

}
