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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a row with transformed values as well as a delegate row (typically
 * a {@link MetaModelInputRow} delegate).
 */
public final class TransformedInputRow extends AbstractLegacyAwareInputRow {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(TransformedInputRow.class);

    private final InputRow _delegate;
    private final Map<InputColumn<?>, Object> _values;
    private final long _id;

    /**
     * Constructs a {@link TransformedInputRow} based on another row and a row
     * ID.
     *
     * @param delegate
     * @param rowId
     */
    public TransformedInputRow(final InputRow delegate, final Number rowId) {
        if (delegate == null) {
            throw new IllegalArgumentException("Delegate cannot be null");
        }
        _delegate = delegate;
        if (rowId == null) {
            _id = delegate.getId();
        } else {
            _id = rowId.longValue();
        }
        _values = new LinkedHashMap<>();
    }

    public TransformedInputRow(final InputRow delegate, final long rowId) {
        if (delegate == null) {
            throw new IllegalArgumentException("Delegate cannot be null");
        }
        _delegate = delegate;
        _id = rowId;
        _values = new LinkedHashMap<>();
    }

    /**
     * Constructs a {@link TransformedInputRow} based on another row, or returns
     * the row if it is already a {@link TransformedInputRow}.
     *
     * @param row
     * @return
     */
    public static TransformedInputRow of(final InputRow row) {
        if (row instanceof TransformedInputRow) {
            // re-use existing transformed input row.
            return (TransformedInputRow) row;
        } else {
            return new TransformedInputRow(row, row.getId());
        }
    }

    @Override
    protected String getFieldNameForNewId() {
        return "_id";
    }

    @Override
    protected String getFieldNameForOldId() {
        return "_rowId";
    }

    @Override
    protected Collection<String> getFieldNamesInAdditionToId() {
        return Arrays.asList("_delegate", "_values");
    }

    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        doReadObject(stream);
    }

    @Override
    public long getId() {
        return _id;
    }

    @Override
    public boolean containsInputColumn(final InputColumn<?> inputColumn) {
        if (inputColumn.isVirtualColumn() && _values.containsKey(inputColumn)) {
            return true;
        }
        return _delegate.containsInputColumn(inputColumn);
    }

    public void addValue(final InputColumn<?> inputColumn, final Object value) {
        if (inputColumn.isPhysicalColumn()) {
            throw new IllegalArgumentException("Cannot add physical column values to transformed InputRow.");
        }
        _values.put(inputColumn, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E getValueInternal(final InputColumn<E> column) {
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
        final List<InputColumn<?>> inputColumns = _delegate.getInputColumns();
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
