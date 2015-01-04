/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A mock implementation of the InputRow interface. Allows for adhoc generation
 * of a row using the put(...) method.
 */
public class MockInputRow extends AbstractInputRow {

    private static final long serialVersionUID = 1L;

    private static final AtomicInteger _idGenerator = new AtomicInteger(Integer.MIN_VALUE);

    private final Map<InputColumn<?>, Object> _values;
    private final int _id;

    public MockInputRow() {
        this(_idGenerator.getAndIncrement());
    }

    public MockInputRow(int id, Map<InputColumn<?>, Object> values) {
        _values = values;
        _id = id;
    }

    public MockInputRow(Map<InputColumn<?>, Object> values) {
        this(_idGenerator.getAndIncrement(), values);
    }

    public MockInputRow(int id) {
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
    public int getId() {
        return _id;
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
        final int prime = 31;
        int result = 1;
        result = prime * result + _id;
        result = prime * result + ((_values == null) ? 0 : _values.hashCode());
        return result;
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
        if (_id != other._id)
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
        return "MockInputRow[id=" + _id + "]";
    }
}