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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple collection factory, which delegates to the storage provider for
 * everything.
 * 
 * 
 * 
 */
public final class CollectionFactoryImpl implements CollectionFactory {

	private final StorageProvider _storageProvider;
	private final List<Object> _collections = new ArrayList<Object>();

	public CollectionFactoryImpl(StorageProvider storageProvider) {
		super();
		_storageProvider = storageProvider;
	}

	@Override
	public <E> List<E> createList(Class<E> elementClass) {
		List<E> list = _storageProvider.createList(elementClass);
		_collections.add(list);
		return list;
	}

	@Override
	public <E> Set<E> createSet(Class<E> elementClass) {
		Set<E> set = _storageProvider.createSet(elementClass);
		_collections.add(set);
		return set;
	}

	@Override
	public <K, V> Map<K, V> createMap(Class<K> keyClass, Class<V> valueClass) {
		Map<K, V> map = _storageProvider.createMap(keyClass, valueClass);
		_collections.add(map);
		return map;
	}
}
