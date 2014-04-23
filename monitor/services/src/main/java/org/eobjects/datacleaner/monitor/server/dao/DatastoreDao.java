/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.monitor.server.dao;

import java.io.Reader;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.w3c.dom.Element;

/**
 * Defines a Data Access Object layer for datastores
 */
public interface DatastoreDao {

    /**
     * Reads/parses a datastore XML element
     * 
     * @param reader
     * @return
     */
    public Element parseDatastoreElement(Reader reader);

    /**
     * Adds a datastore to a tenant's configuration
     * 
     * @param tenantContext
     * @param datastoreElement
     * @return the name of the datastore that was added
     */
    public String addDatastore(TenantContext tenantContext, Element datastoreElement);

    /**
     * Adds a datastore to a tenant's configuration, if possible using standard
     * XML serialization mechanims.
     * 
     * Beware that not all datastore types are supported to be saved using this
     * method. Refer to {@link #addDatastore(TenantContext, Element)} to be
     * completely safe.
     * 
     * @param tenantContext
     * @param datastore
     * @return
     * @throws UnsupportedOperationException
     */
    public String addDatastore(TenantContext tenantContext, Datastore datastore) throws UnsupportedOperationException;

    /**
     * Removes a datastore from a tenant's configuration
     * 
     * @param tenantContext
     * @param datastoreName
     */
    public void removeDatastore(TenantContext tenantContext, String datastoreName) throws IllegalArgumentException;
}