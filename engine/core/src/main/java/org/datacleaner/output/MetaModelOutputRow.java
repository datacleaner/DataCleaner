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
package org.datacleaner.output;

import org.apache.commons.lang.ArrayUtils;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;

/**
 * {@link OutputRow} implementation for the
 * {@link AbstractMetaModelOutputWriter}.
 */
final class MetaModelOutputRow implements OutputRow {

    private final Object[] _values;
    private final AbstractMetaModelOutputWriter _outputWriter;
    private final InputColumn<?>[] _columns;

    public MetaModelOutputRow(final AbstractMetaModelOutputWriter outputWriter, final InputColumn<?>[] columns) {
        _outputWriter = outputWriter;
        _columns = columns;
        _values = new Object[columns.length];
    }

    @Override
    public <E> OutputRow setValue(final InputColumn<? super E> inputColumn, final E value) {
        final int index = ArrayUtils.indexOf(_columns, inputColumn);
        if (index != -1) {
            _values[index] = value;
        }
        return this;
    }

    @Override
    public OutputRow setValues(final InputRow row) {
        for (int i = 0; i < _columns.length; i++) {
            final Object value = row.getValue(_columns[i]);
            _values[i] = value;
        }
        return this;
    }

    @Override
    public void write() {
        _outputWriter.addToBuffer(_values);
    }
}
