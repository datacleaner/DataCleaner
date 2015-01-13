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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

final class SqlDatabaseSet<E> extends AbstractSet<E> implements Set<E>, SqlDatabaseCollection {

	private final Connection _connection;
	private final String _tableName;
	private volatile int _size;

	public SqlDatabaseSet(Connection connection, String tableName, String valueTypeName) {
		_connection = connection;
		_tableName = tableName;

		SqlDatabaseUtils.performUpdate(_connection, SqlDatabaseUtils.CREATE_TABLE_PREFIX + tableName + " (set_value " + valueTypeName
				+ " PRIMARY KEY)");
	}

	public synchronized boolean add(E elem) {
		if (contains(elem)) {
			return false;
		}
		PreparedStatement st = null;
		try {
			st = _connection.prepareStatement("INSERT INTO " + _tableName + " VALUES(?)");
			st.setObject(1, elem);
			st.executeUpdate();
			_size++;
			return true;
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		} finally {
			SqlDatabaseUtils.safeClose(null, st);
		}
	};

	@Override
	public boolean remove(Object o) {
		if (!contains(o)) {
			return false;
		}

		PreparedStatement st = null;
		try {
			st = _connection.prepareStatement("DELETE FROM " + _tableName + " WHERE set_value=?");
			st.setObject(1, o);
			st.executeUpdate();
			_size--;
			return true;
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		} finally {
			SqlDatabaseUtils.safeClose(null, st);
		}
	}

	@Override
	public boolean contains(Object o) {
		ResultSet rs = null;
		PreparedStatement st = null;
		try {
			st = _connection.prepareStatement("SELECT COUNT(*) FROM " + _tableName + " WHERE set_value=?");
			st.setObject(1, o);
			rs = st.executeQuery();
			if (rs.next()) {
				return rs.getInt(1) > 0;
			}
			return false;
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		} finally {
			SqlDatabaseUtils.safeClose(rs, st);
		}
	}

	@Override
	public Iterator<E> iterator() {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = _connection.prepareCall("SELECT set_value FROM " + _tableName);
			rs = st.executeQuery();
			return new SqlDatabaseSetIterator<E>(this, rs, st);
		} catch (SQLException e) {
			SqlDatabaseUtils.safeClose(rs, st);
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int size() {
		return _size;
	}

	@Override
	public String getTableName() {
		return _tableName;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		SqlDatabaseUtils.performUpdate(_connection, "DROP TABLE " + getTableName());
	}
}
