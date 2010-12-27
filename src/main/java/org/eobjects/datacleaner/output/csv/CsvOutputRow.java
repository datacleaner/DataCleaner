/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.output.csv;

import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

import au.com.bytecode.opencsv.CSVWriter;

import org.eobjects.datacleaner.output.OutputRow;

final class CsvOutputRow implements OutputRow {

	private final InputColumn<?>[] _columns;
	private final CSVWriter _csvWriter;
	private final Map<InputColumn<?>, Object> _values;

	public CsvOutputRow(CSVWriter csvWriter, InputColumn<?>... columns) {
		_csvWriter = csvWriter;
		_values = new HashMap<InputColumn<?>, Object>();
		_columns = columns;
	}

	@Override
	public <E> OutputRow setValue(InputColumn<? super E> inputColumn, E value) {
		_values.put(inputColumn, value);
		return this;
	}

	@Override
	public void write() {
		final String[] valueArray = new String[_columns.length];
		for (int i = 0; i < _columns.length; i++) {
			InputColumn<?> column = _columns[i];
			Object value = _values.get(column);
			if (value != null) {
				valueArray[i] = value.toString();
			}
		}
		_csvWriter.writeNext(valueArray);
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
}
