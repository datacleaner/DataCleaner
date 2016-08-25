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
package org.datacleaner.reference;

import java.io.Serializable;

import org.datacleaner.api.Configured;

/**
 * Represents a catalog of items that are considered as reference data that a
 * user can choose to utilize in various analyzers, transformers etc. All of
 * these implement the {@link ReferenceData} interface.
 * 
 * Reference data is typically reusable between jobs which is why it is
 * contained within the configuration. For example you could have a dictionary
 * of valid values for a particular entity type. This dictionary is then
 * reusable both as input to a Dictionary validation filter and an analyzer that
 * will match values against different dictionaries.
 * 
 * All reference data types ( {@link Dictionary} , {@link SynonymCatalog},
 * {@link StringPattern} etc.) is injectable into components using
 * the @Configured annotation.
 * 
 * @see Configured
 */
public interface ReferenceDataCatalog extends Serializable {

    /**
     * Gets the names of all registered {@link Dictionary}
     * 
     * @return
     */
    String[] getDictionaryNames();

    /**
     * Gets a {@link Dictionary} by its name.
     * 
     * @param name
     * @return
     */
    Dictionary getDictionary(String name);

    /**
     * Determines if the catalog contains a particular dictionary
     * 
     * @param name
     * @return
     */
    default boolean containsDictionary(String name) {
        return getDictionary(name) != null;
    }

    /**
     * Gets the names of all registered {@link SynonymCatalog}.
     * 
     * @return
     */
    String[] getSynonymCatalogNames();

    /**
     * Gets a {@link SynonymCatalog} by its name.
     * 
     * @param name
     * @return
     */
    SynonymCatalog getSynonymCatalog(String name);

    /**
     * Determines if the catalog contains a particular synonym catalog
     * 
     * @param name
     * @return
     */
    default boolean containsSynonymCatalog(String name) {
        return getSynonymCatalog(name) != null;
    }
    
    /**
     * Gets the names of all registered {@link StringPattern}s.
     * 
     * @return
     */
    String[] getStringPatternNames();

    /**
     * Gets a {@link StringPattern} by its name.
     * 
     * @param name
     * @return
     */
    StringPattern getStringPattern(String name);

    /**
     * Determines if the catalog contains a particular string pattern
     * 
     * @param name
     * @return
     */
    default boolean containsStringPattern(String name) {
        return getStringPattern(name) != null;
    }
}
