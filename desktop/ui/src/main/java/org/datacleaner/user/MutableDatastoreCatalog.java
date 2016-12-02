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
package org.datacleaner.user;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.datacleaner.configuration.DomConfigurationWriter;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.util.StringUtils;

/**
 * Mutable/modifyable implementation of the datastore catalog interface. Used to
 * allow the user to change the catalog of datastores at runtime. This datastore
 * catalog wraps an immutable instance, which typically represents what is
 * configured in datacleaner's xml file.
 */
public class MutableDatastoreCatalog implements DatastoreCatalog, Serializable {

    private static final long serialVersionUID = 1L;

    private final DomConfigurationWriter _configurationWriter;
    private final List<DatastoreChangeListener> _listeners = new LinkedList<>();
    private final UserPreferences _userPreferences;

    public MutableDatastoreCatalog(final DatastoreCatalog immutableDelegate,
            final DomConfigurationWriter configurationWriter, final UserPreferences userPreferences) {
        _configurationWriter = configurationWriter;
        _userPreferences = userPreferences;
        final String[] datastoreNames = immutableDelegate.getDatastoreNames();
        for (final String name : datastoreNames) {
            if (containsDatastore(name)) {
                // remove any copies of the datastore - the immutable (XML)
                // version should always win
                removeDatastore(getDatastore(name), false);
            }
            addDatastore(immutableDelegate.getDatastore(name), false);
        }
    }

    public void removeDatastore(final Datastore ds) {
        removeDatastore(ds, true);
    }

    private synchronized void removeDatastore(final Datastore ds, final boolean externalize) {
        final List<Datastore> datastores = _userPreferences.getUserDatastores();
        if (datastores.remove(ds)) {
            for (final DatastoreChangeListener listener : _listeners) {
                listener.onRemove(ds);
            }
        }
        if (externalize) {
            _configurationWriter.removeDatastore(ds.getName());
            _userPreferences.save();
        }
    }

    public void addDatastore(final Datastore ds) {
        addDatastore(ds, true);
    }

    private synchronized void addDatastore(final Datastore ds, final boolean externalize) {
        final String name = ds.getName();
        if (StringUtils.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Datastore has no name!");
        }
        final List<Datastore> datastores = _userPreferences.getUserDatastores();
        for (final Datastore datastore : datastores) {
            if (name.equals(datastore.getName())) {
                throw new IllegalArgumentException("A datastore with the name '" + name + "' already exists!");
            }
        }
        datastores.add(ds);
        for (final DatastoreChangeListener listener : _listeners) {
            listener.onAdd(ds);
        }
        if (externalize) {
            if (_configurationWriter.isExternalizable(ds)) {
                _configurationWriter.externalize(ds);
            }
            _userPreferences.save();
        }
    }

    @Override
    public String[] getDatastoreNames() {
        final List<Datastore> datastores = _userPreferences.getUserDatastores();
        final String[] names = new String[datastores.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = datastores.get(i).getName();
        }
        return names;
    }

    @Override
    public Datastore getDatastore(final String name) {
        if (name == null) {
            return null;
        }
        final List<Datastore> datastores = _userPreferences.getUserDatastores();
        for (final Datastore datastore : datastores) {
            if (name.equals(datastore.getName())) {
                return datastore;
            }
        }
        return null;
    }

    public void addListener(final DatastoreChangeListener listener) {
        _listeners.add(listener);
    }

    public void removeListener(final DatastoreChangeListener listener) {
        _listeners.remove(listener);
    }

}
