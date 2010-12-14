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

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.storage.SqlDatabaseUtils;
import org.eobjects.datacleaner.output.OutputRow;
import org.eobjects.datacleaner.output.OutputWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DatastoreOutputWriter implements OutputWriter {

	private static final Logger logger = LoggerFactory.getLogger(DatastoreOutputWriter.class);

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

		// remove write delay (Ticket #494)
		Statement st = null;
		try {
			st = _connection.createStatement();
			st.execute("SET WRITE_DELAY 200 MILLIS");
			logger.info("Write delay removed");
		} catch (Exception e) {
			logger.error("Could not remove write delay", e);
		} finally {
			SqlDatabaseUtils.safeClose(null, st);
		}

		SqlDatabaseUtils.performUpdate(_connection, "DROP TABLE DATASET IF EXISTS");

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
		Statement st = null;
		try {
			st = _connection.createStatement();
			st.execute("SHUTDOWN");
		} catch (SQLException e) {
			logger.error("Could not invoke SHUTDOWN", e);
		} finally {
			SqlDatabaseUtils.safeClose(null, st);
		}

		try {
			logger.info("Closing connection: {}", _connection);
			_connection.close();
		} catch (SQLException e) {
			logger.error("Could not close connection", e);
			throw new IllegalStateException(e);
		}
		String url = DatastoreOutputUtils.getJdbcUrl(_outputFile);

		Datastore datastore = new JdbcDatastore(_datastoreName, url, "org.hsqldb.jdbcDriver", "SA", "");

		_datastoreCreationDelegate.createDatastore(datastore);
	}

}
