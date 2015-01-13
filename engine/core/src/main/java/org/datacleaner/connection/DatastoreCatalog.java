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
package org.datacleaner.connection;

import java.io.Serializable;

/**
 * Represents a collection of datastores, referenceable and usable by jobs.
 */
public interface DatastoreCatalog extends Serializable {

    /**
     * Gets all the names of the datastores in this datastore catalog.
     * 
     * @return
     */
    public String[] getDatastoreNames();

    /**
     * Gets a datastore by it's name. If no such datastore is found, null will
     * be returned.
     * 
     * @param name
     * @return
     */
    public Datastore getDatastore(String name);
}
