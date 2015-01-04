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

import java.util.AbstractList;
import java.util.List;
import java.util.Map;

/**
 * A map wrapped in the List interface. This is used because the berkeley db.
 * Does not support persistent lists, but only maps. Instead a persistent map is
 * wrapped by this List-implementation.
 */
final class BerkeleyDbList<E> extends AbstractList<E> implements List<E> {

	private final BerkeleyDbMap<Integer, E> _wrappedMap;

	public Map<Integer, E> getWrappedMap() {
		return _wrappedMap;
	}

	public BerkeleyDbList(BerkeleyDbMap<Integer, E> map) {
		super();
		_wrappedMap = map;
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		_wrappedMap.finalize();
	}
	
	@Override
	public E get(int index) {
		if (!_wrappedMap.containsKey(index)) {
			throw new IndexOutOfBoundsException();
		}
		E element = _wrappedMap.get(index);
		return element;
	}

	@Override
	public int size() {
		return _wrappedMap.size();
	}

	@Override
	public boolean add(E element) {
		_wrappedMap.put(_wrappedMap.size(), element);
		return true;
	};

	@Override
	public void add(int index, E element) {
		if (!_wrappedMap.containsKey(index)) {
			throw new IndexOutOfBoundsException();
		}
		for (int i = _wrappedMap.size(); i > index; i--) {
			_wrappedMap.put(i, _wrappedMap.get(i - 1));
		}
		_wrappedMap.put(index, element);
	};

	@Override
	public E set(int index, E element) {
		if (!_wrappedMap.containsKey(index)) {
			throw new IndexOutOfBoundsException();
		}
		return _wrappedMap.put(index, element);
	};

	@Override
	public E remove(int index) {
		if (!_wrappedMap.containsKey(index)) {
			throw new IndexOutOfBoundsException();
		}
		E element = _wrappedMap.get(index);
		for (int i = index; i < _wrappedMap.size() - 1; i++) {
			_wrappedMap.put(i, _wrappedMap.get(i + 1));
		}
		_wrappedMap.remove(_wrappedMap.size() - 1);
		return element;
	}
}
