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
package org.eobjects.datacleaner.output.datastore;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation of the DatastoreCreationDelegate.
 * 
 * @author Kasper Sørensen
 */
public class DatastoreCreationDelegateImpl implements DatastoreCreationDelegate {

	private static final Logger logger = LoggerFactory.getLogger(DatastoreCreationDelegateImpl.class);

	private final MutableDatastoreCatalog _datastoreCatalog;

	public DatastoreCreationDelegateImpl(MutableDatastoreCatalog datastoreCatalog) {
		_datastoreCatalog = datastoreCatalog;
	}

	@Override
	public void createDatastore(Datastore datastore) {
		final String name = datastore.getName();
		synchronized (_datastoreCatalog) {
			if (_datastoreCatalog.containsDatastore(name)) {
				logger.warn("Datastore '{}' already exists. No new datastore will be created!", name);
			} else {
				_datastoreCatalog.addDatastore(datastore);
			}
		}
	}
}
