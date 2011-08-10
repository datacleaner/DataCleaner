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
package org.eobjects.datacleaner.user;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.util.StringUtils;

/**
 * Mutable/modifyable implementation of the datastore catalog interface. Used to
 * allow the user to change the catalog of datastores at runtime. This datastore
 * catalog wraps an immutable instance, which typically represents what is
 * configured in datacleaner's xml file.
 * 
 * @author Kasper Sørensen
 * 
 */
public class MutableDatastoreCatalog implements DatastoreCatalog, Serializable {

	private static final long serialVersionUID = 1L;

	private final DatastoreCatalog _immutableDelegate;
	private final List<Datastore> _datastores;
	private final List<DatastoreChangeListener> _listeners = new LinkedList<DatastoreChangeListener>();

	public MutableDatastoreCatalog(final DatastoreCatalog immutableDelegate, UserPreferences userPreferences) {
		_immutableDelegate = immutableDelegate;
		_datastores = userPreferences.getUserDatastores();
		String[] datastoreNames = immutableDelegate.getDatastoreNames();
		for (String name : datastoreNames) {
			if (containsDatastore(name)) {
				// remove any copies of the datastore - the immutable (XML)
				// version should always win
				removeDatastore(getDatastore(name));
			}
			addDatastore(immutableDelegate.getDatastore(name));
		}
	}

	public boolean isDatastoreMutable(String name) {
		return _immutableDelegate.getDatastore(name) == null;
	}

	public boolean containsDatastore(String name) {
		for (Datastore datastore : _datastores) {
			if (name.equals(datastore.getName())) {
				return true;
			}
		}
		return false;
	}

	public synchronized void removeDatastore(Datastore ds) {
		_datastores.remove(ds);
		for (DatastoreChangeListener listener : _listeners) {
			listener.onRemove(ds);
		}
	}

	public synchronized void addDatastore(Datastore ds) {
		String name = ds.getName();
		if (StringUtils.isNullOrEmpty(name)) {
			throw new IllegalArgumentException("Datastore has no name!");
		}
		for (Datastore datastore : _datastores) {
			if (name.equals(datastore.getName())) {
				throw new IllegalArgumentException("Datastore name '" + name + "' is not unique!");
			}
		}
		_datastores.add(ds);
		for (DatastoreChangeListener listener : _listeners) {
			listener.onAdd(ds);
		}
	}

	@Override
	public String[] getDatastoreNames() {
		String[] names = new String[_datastores.size()];
		for (int i = 0; i < names.length; i++) {
			names[i] = _datastores.get(i).getName();
		}
		return names;
	}

	@Override
	public Datastore getDatastore(String name) {
		if (name == null) {
			return null;
		}
		for (Datastore datastore : _datastores) {
			if (name.equals(datastore.getName())) {
				return datastore;
			}
		}
		return null;
	}

	public void addListener(DatastoreChangeListener listener) {
		_listeners.add(listener);
	}

	public void removeListener(DatastoreChangeListener listener) {
		_listeners.remove(listener);
	}
}
