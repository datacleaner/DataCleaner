package org.eobjects.datacleaner.output.datastore;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.storage.SqlDatabaseUtils;
import org.eobjects.datacleaner.output.OutputRow;
import org.eobjects.datacleaner.output.OutputWriter;

final class DatastoreOutputWriter implements OutputWriter {

	private final String _datastoreName;
	private final File _outputFile;
	private final Connection _connection;
	private final InputColumn<?>[] _columns;
	private final String _insertStatement;
	private final DatastoreCreationDelegate _datastoreCreationDelegate;

	public DatastoreOutputWriter(String datastoreName, File outputFile, InputColumn<?>[] columns,
			DatastoreCreationDelegate datastoreCreationDelegate) {
		_datastoreName = datastoreName;
		_outputFile = outputFile;
		_columns = columns;
		_datastoreCreationDelegate = datastoreCreationDelegate;

		try {
			Class.forName("org.hsqldb.jdbcDriver");
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}

		try {
			_connection = DriverManager.getConnection(DatastoreOutputUtils.getCreateJdbcUrl(outputFile), "SA", "");
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}

		// create a CREATE TABLE statement and execute it
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE DATASET (");
		for (int i = 0; i < columns.length; i++) {
			if (i != 0) {
				sb.append(',');
			}
			InputColumn<?> column = columns[i];
			sb.append(DatastoreOutputUtils.safeName(column.getName()));
			sb.append(' ');
			sb.append(SqlDatabaseUtils.getSqlType(column.getDataType()));
		}
		sb.append(')');
		SqlDatabaseUtils.performUpdate(_connection, sb.toString());

		// create a reusable INSERT statement
		sb = new StringBuilder();
		sb.append("INSERT INTO DATASET VALUES (");
		for (int i = 0; i < _columns.length; i++) {
			if (i != 0) {
				sb.append(',');
			}
			sb.append('?');
		}
		sb.append(')');
		_insertStatement = sb.toString();
	}

	@Override
	public OutputRow createRow() {
		try {
			PreparedStatement st = _connection.prepareStatement(_insertStatement);
			return new DatastoreOutputRow(st, _columns);
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void close() {
		try {
			_connection.close();
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
		String url = DatastoreOutputUtils.getReadOnlyJdbcUrl(_outputFile);

		Datastore datastore = new JdbcDatastore(_datastoreName, url, "org.hsqldb.jdbcDriver", "SA", "");

		_datastoreCreationDelegate.createDatastore(datastore);
	}

}
