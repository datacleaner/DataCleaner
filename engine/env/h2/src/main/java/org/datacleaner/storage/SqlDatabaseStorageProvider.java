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
package org.datacleaner.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for storage providers that use an SQL database as a backend to
 * store values.
 * 
 * 
 */
public abstract class SqlDatabaseStorageProvider implements StorageProvider {

	private static final Logger logger = LoggerFactory.getLogger(SqlDatabaseStorageProvider.class);

	private final AtomicInteger _nextTableId = new AtomicInteger(1);
	private final Connection _connection;

	public SqlDatabaseStorageProvider(String driverClassName, String connectionUrl) {
		this(driverClassName, connectionUrl, null, null);
	}

	public SqlDatabaseStorageProvider(String driverClassName, String connectionUrl, String username, String password) {
		logger.info("Creating new storage provider, driver={}, url={}", driverClassName, connectionUrl);
		try {
			Class.forName(driverClassName);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Could not initialize the Hsqldb driver", e);
		}

		try {
			if (username != null) {
				_connection = DriverManager.getConnection(connectionUrl, username, password);
			} else {
				_connection = DriverManager.getConnection(connectionUrl);
			}

			// optimize
			_connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		} catch (SQLException e) {
			throw new IllegalStateException("Could not open connection to database: " + connectionUrl, e);
		}
	}

	protected Connection getConnection() {
		return _connection;
	}

	@Override
	protected void finalize() {
		try {
			_connection.close();
		} catch (SQLException e) {
			// nothing to do
		}
	}

	/**
	 * Subclasses can override this method to control table name generation
	 * 
	 * @return the name of the next table to create
	 */
	protected String getNextTableName() {
		return "ab_" + _nextTableId.getAndIncrement();
	}

	@Override
	public <E> List<E> createList(Class<E> valueType) throws IllegalStateException {
		String tableName = getNextTableName();
		String valueTypeName = SqlDatabaseUtils.getSqlType(valueType);
		logger.info("Creating table {} for List", tableName);
		return new SqlDatabaseList<E>(_connection, tableName, valueTypeName);
	}

	@Override
	public <E> Set<E> createSet(Class<E> valueType) throws IllegalStateException {
		String tableName = getNextTableName();
		String valueTypeName = SqlDatabaseUtils.getSqlType(valueType);
		logger.info("Creating table {} for Set", tableName);
		return new SqlDatabaseSet<E>(_connection, tableName, valueTypeName);
	}

	@Override
	public <K, V> Map<K, V> createMap(Class<K> keyType, Class<V> valueType) throws IllegalStateException {
		String tableName = getNextTableName();
		String keyTypeName = SqlDatabaseUtils.getSqlType(keyType);
		String valueTypeName = SqlDatabaseUtils.getSqlType(valueType);
		logger.info("Creating table {} for Map", tableName);
		return new SqlDatabaseMap<K, V>(_connection, tableName, keyTypeName, valueTypeName);
	}

	@Override
	public final RowAnnotationFactory createRowAnnotationFactory() {
		String tableName = getNextTableName();
		logger.info("Creating table {} for RowAnnotationFactory", tableName);
		SqlDatabaseRowAnnotationFactory factory = new SqlDatabaseRowAnnotationFactory(_connection, tableName);
		return factory;
	}
}
