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
package org.eobjects.datacleaner.output.datastore;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.storage.SqlDatabaseUtils;
import org.eobjects.datacleaner.output.OutputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DatastoreOutputRow implements OutputRow {

	private static final Logger logger = LoggerFactory.getLogger(DatastoreOutputRow.class);

	private final PreparedStatement _st;
	private final InputColumn<?>[] _columns;

	public DatastoreOutputRow(PreparedStatement st, InputColumn<?>[] columns) {
		_st = st;
		_columns = columns;
	}

	@Override
	public <E> OutputRow setValue(InputColumn<E> inputColumn, E value) {
		int index = -1;
		for (int i = 0; i < _columns.length; i++) {
			if (inputColumn.equals(_columns[i])) {
				index = i;
			}
		}
		if (index == -1) {
			throw new IllegalArgumentException("Column '" + inputColumn + "' is not being written");
		}

		try {
			_st.setObject(index + 1, value);
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
		return this;
	}

	@Override
	public void write() {
		logger.info("Writing row based on statement: {}", _st);
		try {
			_st.executeUpdate();
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		} finally {
			SqlDatabaseUtils.safeClose(null, _st);
		}
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
