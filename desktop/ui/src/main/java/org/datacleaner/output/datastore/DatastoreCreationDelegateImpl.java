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
package org.datacleaner.output.datastore;

import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation of the DatastoreCreationDelegate.
 */
public class DatastoreCreationDelegateImpl implements DatastoreCreationDelegate {

    private static final Logger logger = LoggerFactory.getLogger(DatastoreCreationDelegateImpl.class);

    private final DatastoreCatalog _datastoreCatalog;

    public DatastoreCreationDelegateImpl(DatastoreCatalog datastoreCatalog) {
        _datastoreCatalog = datastoreCatalog;
    }

    @Override
    public void createDatastore(Datastore datastore) {
        final String name = datastore.getName();
        synchronized (_datastoreCatalog) {
            if (_datastoreCatalog.containsDatastore(name)) {
                logger.warn("Datastore '{}' already exists. No new datastore will be created!", name);
            } else {
                if (_datastoreCatalog instanceof MutableDatastoreCatalog) {
                    ((MutableDatastoreCatalog) _datastoreCatalog).addDatastore(datastore);
                } else {
                    throw new IllegalStateException("Tried to create datastore '" + name
                            + "', but the datastore catalog is not mutable");
                }
            }
        }
    }
}
