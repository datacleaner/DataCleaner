package org.eobjects.datacleaner.output.datastore;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.storage.SqlDatabaseUtils;
import org.eobjects.datacleaner.output.OutputRow;

final class DatastoreOutputRow implements OutputRow {

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
		try {
			_st.executeUpdate();
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
		SqlDatabaseUtils.safeClose(null, _st);
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
