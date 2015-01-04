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
import java.util.Iterator;
import java.util.Set;

import com.sleepycat.collections.StoredKeySet;
import com.sleepycat.je.Database;
import com.sleepycat.je.Environment;

final class BerkeleyDbSet<E> implements Set<E> {

	private final Set<E> _wrappedSet;
	private final Environment _environment;
	private final Database _database;

	@SuppressWarnings("unchecked")
	public BerkeleyDbSet(Environment environment, Database database, StoredKeySet set) {
		_environment = environment;
		_database = database;
		_wrappedSet = set;
	}

	public Set<E> getWrappedSet() {
		return _wrappedSet;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		String name = _database.getDatabaseName();
		_database.close();
		_environment.removeDatabase(null, name);
	}

	public int size() {
		return _wrappedSet.size();
	}

	public boolean isEmpty() {
		return _wrappedSet.isEmpty();
	}

	public boolean contains(Object o) {
		return _wrappedSet.contains(o);
	}

	public Iterator<E> iterator() {
		return _wrappedSet.iterator();
	}

	public Object[] toArray() {
		return _wrappedSet.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return _wrappedSet.toArray(a);
	}

	public boolean add(E e) {
		return _wrappedSet.add(e);
	}

	public boolean remove(Object o) {
		return _wrappedSet.remove(o);
	}

	public boolean containsAll(Collection<?> c) {
		return _wrappedSet.containsAll(c);
	}

	public boolean addAll(Collection<? extends E> c) {
		return _wrappedSet.addAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return _wrappedSet.retainAll(c);
	}

	public boolean removeAll(Collection<?> c) {
		return _wrappedSet.removeAll(c);
	}

	public void clear() {
		_wrappedSet.clear();
	}

	public boolean equals(Object o) {
		return _wrappedSet.equals(o);
	}

	public int hashCode() {
		return _wrappedSet.hashCode();
	}
}
