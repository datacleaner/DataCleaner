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
package org.datacleaner.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.datacleaner.api.InputColumn;

/**
 * A mock implementation of the InputRow interface. Allows for adhoc generation
 * of a row using the put(...) method.
 */
public class MockInputRow extends AbstractLegacyAwareInputRow {

    private static final long serialVersionUID = 1L;

    private static final AtomicInteger _idGenerator = new AtomicInteger(Integer.MIN_VALUE);

    private final Map<InputColumn<?>, Object> _values;
    private final long _rowId;

    public MockInputRow() {
        this(_idGenerator.getAndIncrement());
    }

    public MockInputRow(long id, Map<InputColumn<?>, Object> values) {
        _values = values;
        _rowId = id;
    }

    public MockInputRow(Map<InputColumn<?>, Object> values) {
        this(_idGenerator.getAndIncrement(), values);
    }

    public MockInputRow(long id) {
        this(id, new LinkedHashMap<InputColumn<?>, Object>());
    }

    public MockInputRow(InputColumn<?>[] columns, Object[] values) {
        this(_idGenerator.getAndIncrement(), columns, values);
    }

    public MockInputRow(int id, InputColumn<?>[] columns, Object[] values) {
        this(id);
        for (int i = 0; i < values.length; i++) {
            put(columns[i], values[i]);
        }
    }

    @Override
    protected String getFieldNameForNewId() {
        return "_rowId";
    }

    @Override
    protected String getFieldNameForOldId() {
        return "_id";
    }

    @Override
    protected Collection<String> getFieldNamesInAdditionToId() {
        return Arrays.asList("_values");
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        doReadObject(stream);
    }

    @Override
    public long getId() {
        return _rowId;
    }

    @Override
    public List<InputColumn<?>> getInputColumns() {
        return new ArrayList<InputColumn<?>>(_values.keySet());
    }

    @Override
    public boolean containsInputColumn(InputColumn<?> inputColumn) {
        return _values.containsKey(inputColumn);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E getValueInternal(InputColumn<E> column) {
        return (E) _values.get(column);
    }

    /**
     * Puts/adds a new value to the row.
     * 
     * @param column
     * @param value
     * @return
     */
    public MockInputRow put(InputColumn<?> column, Object value) {
        _values.put(column, value);
        return this;
    }

    /**
     * Puts/adds new values to the row.
     * 
     * @param values
     * @return
     */
    public MockInputRow putAll(Map<InputColumn<?>, Object> values) {
        _values.putAll(values);
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_rowId, _values);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MockInputRow other = (MockInputRow) obj;
        if (_rowId != other._rowId)
            return false;
        if (_values == null) {
            if (other._values != null)
                return false;
        } else if (!_values.equals(other._values))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "MockInputRow[id=" + _rowId + "]";
    }
}
