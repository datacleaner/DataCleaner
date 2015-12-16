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
package org.datacleaner.output.csv;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.csv.CsvWriter;
import org.apache.metamodel.util.Ref;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.output.OutputRow;

final class CsvOutputRow implements OutputRow {

    private final Map<InputColumn<?>, Object> _map;
    private final InputColumn<?>[] _columns;
    private final Ref<OutputStream> _outputStreamRef;
    private final CsvConfiguration _csvConfiguration;

    public CsvOutputRow(Ref<OutputStream> outputStreamRef, CsvConfiguration csvConfiguration, InputColumn<?>[] columns) {
        _outputStreamRef = outputStreamRef;
        _csvConfiguration = csvConfiguration;
        _columns = columns;
        _map = new HashMap<>();
    }

    @Override
    public <E> OutputRow setValue(InputColumn<? super E> inputColumn, E value) {
        _map.put(inputColumn, value);
        return this;
    }

    @Override
    public OutputRow setValues(InputRow row) {
        for (InputColumn<?> column : _columns) {
            Object value = row.getValue(column);
            @SuppressWarnings("unchecked")
            InputColumn<Object> objectColumn = (InputColumn<Object>) column;
            setValue(objectColumn, value);
        }
        return this;
    }

    @Override
    public void write() {
        final String[] values = new String[_columns.length];
        for (int i = 0; i < values.length; i++) {
            final Object value = _map.get(_columns[i]);
            if (value != null) {
                values[i] = value.toString();
            }
        }
        final CsvWriter csvWriter = new CsvWriter(_csvConfiguration);
        final String line = csvWriter.buildLine(values);
        final OutputStream outputStream = _outputStreamRef.get();
        try {
            final byte[] bytes = line.getBytes(_csvConfiguration.getEncoding());
            outputStream.write(bytes);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
