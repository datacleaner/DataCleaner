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
package org.eobjects.analyzer.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a row with transformed values as well as a delegate row (typically
 * a {@link MetaModelInputRow} delegate).
 */
public final class TransformedInputRow extends AbstractInputRow {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(TransformedInputRow.class);

    private final InputRow _delegate;
    private final Map<InputColumn<?>, Object> _values;
    private final int _rowId;

    public TransformedInputRow(InputRow delegate) {
        this(delegate, null);
    }

    public TransformedInputRow(InputRow delegate, Integer rowId) {
        if (delegate == null) {
            throw new IllegalArgumentException("Delegate cannot be null");
        }
        _delegate = delegate;
        if (rowId == null) {
            _rowId = delegate.getId();
        } else {
            _rowId = rowId;
        }
        _values = new LinkedHashMap<InputColumn<?>, Object>();
    }

    @Override
    public int getId() {
        return _rowId;
    }

    @Override
    public boolean containsInputColumn(InputColumn<?> inputColumn) {
        if (inputColumn.isVirtualColumn() && _values.containsKey(inputColumn)) {
            return true;
        }
        return _delegate.containsInputColumn(inputColumn);
    }

    public void addValue(InputColumn<?> inputColumn, Object value) {
        if (inputColumn.isPhysicalColumn()) {
            throw new IllegalArgumentException("Cannot add physical column values to transformed InputRow.");
        }
        _values.put(inputColumn, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E getValueInternal(InputColumn<E> column) {
        if (column.isPhysicalColumn()) {
            logger.debug("Column is physical, delegating.");
            return _delegate.getValue(column);
        }
        if (_values.containsKey(column)) {
            return (E) _values.get(column);
        }
        return _delegate.getValue(column);
    }

    public InputRow getDelegate() {
        return _delegate;
    }

    @Override
    public List<InputColumn<?>> getInputColumns() {
        List<InputColumn<?>> inputColumns = _delegate.getInputColumns();
        inputColumns.addAll(_values.keySet());
        return inputColumns;
    }

    public Set<InputColumn<?>> getTransformedInputColumns() {
        return _values.keySet();
    }

    @Override
    public String toString() {
        return "TransformedInputRow[values=" + _values + ",delegate=" + _delegate + "]";
    }
}
