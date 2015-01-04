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
package org.eobjects.analyzer.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eobjects.analyzer.util.CollectionUtils2;

final class SqlDatabaseMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, SqlDatabaseCollection {

    private final Map<K, V> _cache = CollectionUtils2.<K, V> createCache(1000, 60).asMap();
    
    private final Connection _connection;
    private final String _tableName;
    private final PreparedStatement _getPreparedStatement;
    private final PreparedStatement _updatePreparedStatement;
    private final PreparedStatement _insertPreparedStatement;
    private volatile int _size;

    public SqlDatabaseMap(Connection connection, String tableName, String keyTypeName, String valueTypeName) {
        _connection = connection;
        _tableName = tableName;

        SqlDatabaseUtils.performUpdate(_connection, SqlDatabaseUtils.CREATE_TABLE_PREFIX + tableName + " (map_key "
                + keyTypeName + " PRIMARY KEY, map_value " + valueTypeName + ")");

        try {
            _getPreparedStatement = _connection.prepareStatement("SELECT map_value FROM " + _tableName
                    + " WHERE map_key = ?;");
            _updatePreparedStatement = _connection.prepareStatement("UPDATE " + _tableName
                    + " SET map_value = ? WHERE map_key = ?;");
            _insertPreparedStatement = _connection.prepareStatement("INSERT INTO  " + _tableName + " VALUES (?,?);");
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int size() {
        return _size;
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized V get(Object key) {
        V v = _cache.get(key);
        if (v != null) {
            return v;
        }

        ResultSet rs = null;
        try {
            _getPreparedStatement.setObject(1, key);
            rs = _getPreparedStatement.executeQuery();
            if (rs.next()) {
                return (V) rs.getObject(1);
            }
            return null;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        } finally {
            SqlDatabaseUtils.safeClose(rs, null);
        }
    }

    @Override
    public synchronized boolean containsKey(Object key) {
        if (_cache.containsKey(key)) {
            return true;
        }

        ResultSet rs = null;
        PreparedStatement st = null;
        try {
            st = _connection.prepareStatement("SELECT COUNT(*) FROM  " + _tableName + " WHERE map_key = ?");
            st.setObject(1, key);
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

    public synchronized V put(K key, V value) {
        try {
            final V v;
            if (containsKey(key)) {
                v = get(key);
                _updatePreparedStatement.setObject(1, value);
                _updatePreparedStatement.setObject(2, key);
                _updatePreparedStatement.executeUpdate();
            } else {
                _insertPreparedStatement.setObject(1, key);
                _insertPreparedStatement.setObject(2, value);
                _insertPreparedStatement.executeUpdate();
                _size++;
                v = null;
            }
            _cache.put(key, value);
            return v;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    };

    @Override
    public synchronized V remove(Object key) {
        if (containsKey(key)) {
            V result = get(key);
            PreparedStatement st = null;
            try {
                st = _connection.prepareStatement("DELETE FROM " + _tableName + " WHERE map_key = ?");
                st.setObject(1, key);
                st.executeUpdate();
                _size--;
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            } finally {
                SqlDatabaseUtils.safeClose(null, st);
            }
            _cache.remove(key);
            return result;
        }
        return null;
    }

    @Override
    public synchronized Set<java.util.Map.Entry<K, V>> entrySet() {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            Set<Entry<K, V>> result = new HashSet<Map.Entry<K, V>>();
            st = _connection.prepareStatement("SELECT map_key FROM " + _tableName + " ORDER BY map_key ASC;");
            rs = st.executeQuery();
            while (rs.next()) {
                @SuppressWarnings("unchecked")
                K key = (K) rs.getObject(1);
                result.add(new SqlDatabaseEntry(key));
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        } finally {
            SqlDatabaseUtils.safeClose(rs, st);
        }
    }

    @Override
    public String getTableName() {
        return _tableName;
    }

    private class SqlDatabaseEntry implements Entry<K, V> {

        private final K _key;

        public SqlDatabaseEntry(K key) {
            _key = key;
        }

        @Override
        public K getKey() {
            return _key;
        }

        @Override
        public V getValue() {
            return get(_key);
        }

        @Override
        public V setValue(V value) {
            return put(_key, value);
        }

        @Override
        public int hashCode() {
            return _key.hashCode();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        SqlDatabaseUtils.performUpdate(_connection, "DROP TABLE " + getTableName());
    }
}
