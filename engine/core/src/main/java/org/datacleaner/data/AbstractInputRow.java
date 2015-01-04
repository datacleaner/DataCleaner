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

/**
 * Abstract implementation of the InputRow.
 */
public abstract class AbstractInputRow implements InputRow {

	private static final long serialVersionUID = 1L;

	@Override
	public final <E> E getValue(InputColumn<E> column) {
		if (column == null) {
			return null;
		}
		if (column instanceof ExpressionBasedInputColumn) {
			ExpressionBasedInputColumn<E> ebic = (ExpressionBasedInputColumn<E>) column;
			E value = ebic.evaluate(this);
			return value;
		}
		
        return getValueInternal(column);
	}
	
	@Override
	public List<Object> getValues(List<InputColumn<?>> columns) {
	    if (columns == null) {
            return new ArrayList<Object>(0);
        }
	    List<Object> result = new ArrayList<Object>(columns.size());
	    for (InputColumn<?> inputColumn : columns) {
            Object value = getValue(inputColumn);
            result.add(value);
        }
	    return result;
	}
	
	@Override
	public List<Object> getValues(InputColumn<?>... columns) {
	    if (columns == null) {
	        return new ArrayList<Object>(0);
	    }
	    return getValues(Arrays.asList(columns));
	}

	protected abstract <E> E getValueInternal(InputColumn<E> column);
}
