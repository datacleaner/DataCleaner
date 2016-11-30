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
package org.datacleaner.output.datastore;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.output.OutputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DatastoreOutputRow implements OutputRow {

    private static final Logger logger = LoggerFactory.getLogger(DatastoreOutputRow.class);

    private final PreparedStatement _insertStatement;
    private final InputColumn<?>[] _columns;
    private final Map<Integer, Object> _parameters;

    public DatastoreOutputRow(final PreparedStatement insertStatement, final InputColumn<?>[] columns) {
        _insertStatement = insertStatement;
        _columns = columns;
        _parameters = new HashMap<>();
    }

    @Override
    public <E> OutputRow setValue(final InputColumn<? super E> inputColumn, final E value) {
        int index = -1;
        for (int i = 0; i < _columns.length; i++) {
            if (inputColumn.equals(_columns[i])) {
                index = i;
            }
        }
        if (index == -1) {
            throw new IllegalArgumentException("Column '" + inputColumn + "' is not being written");
        }

        if (value != null && !DatastoreOutputWriter.isDirectlyInsertableType(inputColumn)) {
            _parameters.put(index + 1, value.toString());
        } else {
            _parameters.put(index + 1, value);
        }
        return this;
    }

    @Override
    public void write() {
        synchronized (_insertStatement) {
            final Set<Entry<Integer, Object>> entries = _parameters.entrySet();
            try {
                for (final Entry<Integer, Object> entry : entries) {
                    _insertStatement.setObject(entry.getKey(), entry.getValue());
                }
                logger.debug("Writing row based on statement: {}", _insertStatement);
                _insertStatement.executeUpdate();
            } catch (final SQLException e) {
                logger.error("Exception occurred while executing statement with parameters: {}", _parameters);
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public OutputRow setValues(final InputRow row) {
        for (final InputColumn<?> column : _columns) {
            final Object value = row.getValue(column);
            @SuppressWarnings("unchecked") final InputColumn<Object> objectColumn = (InputColumn<Object>) column;
            setValue(objectColumn, value);
        }
        return this;
    }
}
