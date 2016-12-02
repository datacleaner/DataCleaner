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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.datacleaner.api.ExpressionBasedInputColumn;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;

/**
 * Abstract implementation of the InputRow.
 */
public abstract class AbstractInputRow implements InputRow {

    private static final long serialVersionUID = 1L;

    @Override
    public final <E> E getValue(final InputColumn<E> column) {
        if (column == null) {
            return null;
        }
        if (column instanceof ExpressionBasedInputColumn) {
            final ExpressionBasedInputColumn<E> ebic = (ExpressionBasedInputColumn<E>) column;
            return ebic.evaluate(this);
        }

        return getValueInternal(column);
    }

    @Override
    public List<Object> getValues(final List<InputColumn<?>> columns) {
        if (columns == null) {
            return new ArrayList<>(0);
        }
        final List<Object> result = new ArrayList<>(columns.size());
        for (final InputColumn<?> inputColumn : columns) {
            final Object value = getValue(inputColumn);
            result.add(value);
        }
        return result;
    }

    @Override
    public List<Object> getValues(final InputColumn<?>... columns) {
        if (columns == null) {
            return new ArrayList<>(0);
        }
        return getValues(Arrays.asList(columns));
    }

    protected abstract <E> E getValueInternal(InputColumn<E> column);
}
