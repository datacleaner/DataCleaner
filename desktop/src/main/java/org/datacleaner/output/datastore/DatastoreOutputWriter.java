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
package org.datacleaner.output.datastore;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.datacleaner.api.InputColumn;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.storage.H2StorageProvider;
import org.datacleaner.storage.SqlDatabaseUtils;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.output.OutputRow;
import org.datacleaner.output.OutputWriter;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.DataContextFactory;

final class DatastoreOutputWriter implements OutputWriter {

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

		synchronized (DatastoreOutputWriter.class) {
			final DataContext dc = DataContextFactory.createJdbcDataContext(_connection);
			dc.refreshSchemas();
			final String[] tableNames = dc.getDefaultSchema().getTableNames();

			if (truncateExisting) {
				_tableName = tableName;

				for (String existingTableName : tableNames) {
					if (_tableName.equalsIgnoreCase(existingTableName)) {
						SqlDatabaseUtils.performUpdate(_connection, "DROP TABLE " + existingTableName);
					}
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
						if (existingTableName.equalsIgnoreCase(proposalName)) {
							accepted = false;
							break;
						}
					}
				}
				_tableName = proposalName;
			}

			// create a CREATE TABLE statement and execute it
			final StringBuilder createStatementBuilder = new StringBuilder();
			createStatementBuilder.append("CREATE TABLE ");
			createStatementBuilder.append(_tableName);
			createStatementBuilder.append(" (");
			for (int i = 0; i < columns.length; i++) {
				if (i != 0) {
					createStatementBuilder.append(',');
				}
				InputColumn<?> column = columns[i];
				createStatementBuilder.append(DatastoreOutputUtils.safeName(column.getName()));
				createStatementBuilder.append(' ');
				if (!isDirectlyInsertableType(column)) {
					createStatementBuilder.append(SqlDatabaseUtils.getSqlType(String.class));
				} else {
					createStatementBuilder.append(SqlDatabaseUtils.getSqlType(column.getDataType()));
				}
			}
			createStatementBuilder.append(')');
			SqlDatabaseUtils.performUpdate(_connection, createStatementBuilder.toString());
		}

		// create a reusable INSERT statement
		final StringBuilder insertStatementBuilder = new StringBuilder();
		insertStatementBuilder.append("INSERT INTO ");
		insertStatementBuilder.append(_tableName);
		insertStatementBuilder.append(" VALUES (");
		for (int i = 0; i < _columns.length; i++) {
			if (i != 0) {
				insertStatementBuilder.append(',');
			}
			insertStatementBuilder.append('?');
		}
		insertStatementBuilder.append(')');

		try {
			_insertStatement = _connection.prepareStatement(insertStatementBuilder.toString());
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public OutputRow createRow() {
		return new DatastoreOutputRow(_insertStatement, _columns);
	}

	public String getTableName() {
		return _tableName;
	}

	@Override
	public void close() {
		SqlDatabaseUtils.safeClose(null, _insertStatement);

		DatastoreOutputWriterFactory.release(this);

		Datastore datastore = new JdbcDatastore(_datastoreName, _jdbcUrl, DRIVER_CLASS_NAME, "SA", "", true);
		_datastoreCreationDelegate.createDatastore(datastore);
	}

	public String getJdbcUrl() {
		return _jdbcUrl;
	}

	public Connection getConnection() {
		return _connection;
	}
	
	public static boolean isDirectlyInsertableType(InputColumn<?> column) {
		final Class<?> dataType = column.getDataType();
		return ReflectionUtils.isNumber(dataType) || ReflectionUtils.isDate(dataType) || ReflectionUtils.isBoolean(dataType);
	}
}
