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
package org.datacleaner.monitor.server.dao;

import java.io.Reader;

import org.datacleaner.monitor.configuration.TenantContext;
import org.w3c.dom.Element;

/**
 * Defines a Data Access Object layer for reference data. 
 */
public interface ReferenceDataDao {

    /**
     * Reads/parses a reference data XML element. 
     * 
     * @param reader
     * @return
     */
    Element parseReferenceDataElement(Reader reader);

    /**
     * Adds a reference data to a tenant's configuration. 
     * 
     * @param tenantContext
     * @param reference dataElement
     * @return the name of the reference data that was added
     */
    String addReferenceData(TenantContext tenantContext, Element reference);

    /**
     * Removes reference data from a tenant's configuration. 
     * 
     * @param tenantContext
     * @param referenceDataName
     */
    void removeReferenceData(TenantContext tenantContext, String referenceDataName) throws IllegalArgumentException;
}
