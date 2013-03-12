/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.output;

import org.apache.commons.lang.ArrayUtils;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

/**
 * {@link OutputRow} implementation for the
 * {@link AbstractMetaModelOutputWriter}.
 * 
 * @author Kasper Sørensen
 */
final class MetaModelOutputRow implements OutputRow {

	private final Object[] _values;
	private final AbstractMetaModelOutputWriter _outputWriter;
	private final InputColumn<?>[] _columns;

	public MetaModelOutputRow(AbstractMetaModelOutputWriter outputWriter, InputColumn<?>[] columns) {
		_outputWriter = outputWriter;
		_columns = columns;
		_values = new Object[columns.length];
	}

	@Override
	public <E> OutputRow setValue(InputColumn<? super E> inputColumn, E value) {
		int index = ArrayUtils.indexOf(_columns, inputColumn);
		if (index != -1) {
			_values[index] = value;
		}
		return this;
	}

	@Override
	public OutputRow setValues(InputRow row) {
		for (int i = 0; i < _columns.length; i++) {
			Object value = row.getValue(_columns[i]);
			_values[i] = value;
		}
		return this;
	}

	@Override
	public void write() {
		_outputWriter.addToBuffer(_values);
	}
}