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
import org.eobjects.analyzer.storage.H2StorageProvider;
import org.eobjects.analyzer.storage.SqlDatabaseUtils;
import org.eobjects.datacleaner.output.OutputRow;
import org.eobjects.datacleaner.output.OutputWriter;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.DataContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DatastoreOutputWriter implements OutputWriter {

	private static final Logger logger = LoggerFactory.getLogger(DatastoreOutputWriter.class);

	private static final String DRIVER_CLASS_NAME = H2StorageProvider.DRIVER_CLASS_NAME;

	private final String _datastoreName;
	private final String _jdbcUrl;
	private final Connection _connection;
	private final String _tableName;
	private final InputColumn<?>[] _columns;
	private final PreparedStatement _insertStatement;
	private final DatastoreCreationDelegate _datastoreCreationDelegate;

	public DatastoreOutputWriter(String datastoreName, String tableName, File directory, InputColumn<?>[] columns,
			DatastoreCreationDelegate datastoreCreationDelegate) {
		this(datastoreName, tableName, directory, columns, datastoreCreationDelegate, true);
	}

	public DatastoreOutputWriter(String datastoreName, String tableName, File directory, InputColumn<?>[] columns,
			DatastoreCreationDelegate datastoreCreationDelegate, boolean truncateExisting) {
		_datastoreName = datastoreName;
		_jdbcUrl = DatastoreOutputUtils.getJdbcUrl(directory, _datastoreName);
		_columns = columns;
		_datastoreCreationDelegate = datastoreCreationDelegate;

		try {
			Class.forName(DRIVER_CLASS_NAME);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}

		try {
			_connection = DriverManager.getConnection(_jdbcUrl, "SA", "");
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}

		// make table name safe
		tableName = DatastoreOutputUtils.safeName(tableName);

		synchronized (_jdbcUrl) {
			final DataContext dc = DataContextFactory.createJdbcDataContext(_connection);
			final String[] tableNames = dc.getDefaultSchema().getTableNames();

			if (truncateExisting) {
				_tableName = tableName;

				for (String existingTableName : tableNames) {
					SqlDatabaseUtils.performUpdate(_connection, "DROP TABLE " + existingTableName);
				}
			} else {
				int tableNumber = 0;
				boolean accepted = false;
				String proposalName = null;
				while (!accepted) {
					tableNumber++;
					proposalName = tableName + '_' + tableNumber;
					accepted = true;
					for (String existingTableName : tableNames) {
						if (existingTableName.equals(proposalName)) {
							accepted = false;
							break;
						}
					}
				}
				_tableName = proposalName;
			}
		}

		// create a CREATE TABLE statement and execute it
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ");
		sb.append(_tableName);
		sb.append(" (");
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
		sb.append("INSERT INTO ");
		sb.append(_tableName);
		sb.append(" VALUES (");
		for (int i = 0; i < _columns.length; i++) {
			if (i != 0) {
				sb.append(',');
			}
			sb.append('?');
		}
		sb.append(')');
		String sql = sb.toString();
		try {
			_insertStatement = _connection.prepareStatement(sql);
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public OutputRow createRow() {
		return new DatastoreOutputRow(_insertStatement, _columns);
	}

	@Override
	public void close() {
		SqlDatabaseUtils.safeClose(null, _insertStatement);

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

		Datastore datastore = new JdbcDatastore(_datastoreName, _jdbcUrl, DRIVER_CLASS_NAME, "SA", "");

		_datastoreCreationDelegate.createDatastore(datastore);
	}

}
