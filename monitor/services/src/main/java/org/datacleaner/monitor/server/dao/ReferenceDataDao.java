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
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.reference.SynonymCatalog;
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
     * Updates reference data sub section.
     *
     * @param tenantContext
     * @param updatedReferenceDataSubSection
     * @return the name of the lastly added element
     */
    String updateReferenceDataSubSection(TenantContext tenantContext, Element updatedReferenceDataSubSection);

    /**
     * Adds a dictionary.
     *
     * @param tenantContext
     * @param dictionary
     */
    void addDictionary(TenantContext tenantContext, Dictionary dictionary);

    /**
     * Adds a synonym catalog.
     *
     * @param tenantContext
     * @param synonymCatalog
     */
    void addSynonymCatalog(TenantContext tenantContext, SynonymCatalog synonymCatalog);

    /**
     * Adds a string pattern.
     *
     * @param tenantContext
     * @param synonymCatalog
     */
    void addStringPattern(TenantContext tenantContext, StringPattern synonymCatalog);

    /**
     * Removes a dictionary from a tenant's configuration.
     *  @param tenantContext
     * @param name
     */
    void removeDictionary(TenantContext tenantContext, String name);

    /**
     * Removes a synonym catalog from a tenant's configuration.
     *
     * @param tenantContext
     * @param name
     */
    void removeSynonymCatalog(TenantContext tenantContext, String name);

    /**
     * Removes a string pattern from a tenant's configuration.
     *
     * @param tenantContext
     * @param name
     */
    void removeStringPattern(TenantContext tenantContext, String name);
}
