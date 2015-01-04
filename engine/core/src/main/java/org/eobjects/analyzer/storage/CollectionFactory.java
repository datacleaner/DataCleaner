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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A factory for collections to be used by components. Typically these
 * collections are provided by the framework an are implemented using some
 * persistent storage strategy, making them safe to fill with even millions of
 * elements without running out of memory.
 * 
 * 
 */
public interface CollectionFactory {

	/**
	 * Creates a potentially large list
	 * 
	 * @param <E>
	 * @param elementClass
	 * @return
	 */
	public <E> List<E> createList(Class<E> elementClass);

	/**
	 * Creates a potentially large set
	 * 
	 * @param <E>
	 * @param elementClass
	 * @return
	 */
	public <E> Set<E> createSet(Class<E> elementClass);

	/**
	 * Creates a potentially large map
	 * 
	 * @param <K>
	 * @param <V>
	 * @param keyClass
	 * @param valueClass
	 * @return
	 */
	public <K, V> Map<K, V> createMap(Class<K> keyClass, Class<V> valueClass);
}
