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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.Database;
import com.sleepycat.je.Environment;

final class BerkeleyDbMap<K, V> implements Map<K, V> {

	private static final Logger logger = LoggerFactory.getLogger(BerkeleyDbMap.class);

	private final Map<K, V> _wrappedMap;
	private final Database _database;
	private final Environment _environment;

	@SuppressWarnings("unchecked")
	public BerkeleyDbMap(Environment environment, Database database, StoredMap map) {
		_environment = environment;
		_database = database;
		_wrappedMap = map;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		String name = _database.getDatabaseName();
		_database.close();
		_environment.removeDatabase(null, name);
	}

	@Override
	public int size() {
		return _wrappedMap.size();
	}

	@Override
	public boolean isEmpty() {
		return _wrappedMap.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return _wrappedMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return _wrappedMap.containsValue(value);
	}

	@Override
	public V get(Object key) {
		try {
			return _wrappedMap.get(key);
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.warn("get('{}') threw exception: {}", key, e.getMessage());
			logger.warn("Swallowing exception, returning null", e);
			// there's a bug in berkeley that sometime causes this exception
			// when the value is null!
			return null;
		}
	}

	@Override
	public V put(K key, V value) {
		return _wrappedMap.put(key, value);
	}

	@Override
	public V remove(Object key) {
		return _wrappedMap.remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		_wrappedMap.putAll(m);
	}

	@Override
	public void clear() {
		_wrappedMap.clear();
	}

	@Override
	public Set<K> keySet() {
		return _wrappedMap.keySet();
	}

	@Override
	public Collection<V> values() {
		return _wrappedMap.values();
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return _wrappedMap.entrySet();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		return _wrappedMap.equals(o);
	}

	@Override
	public int hashCode() {
		return _wrappedMap.hashCode();
	}
}
