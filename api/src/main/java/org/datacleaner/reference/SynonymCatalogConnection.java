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

import java.io.Closeable;
import java.util.Collection;

public interface SynonymCatalogConnection extends Closeable {

    /**
     * @return all synonyms contained within this catalog
     */
    public Collection<? extends Synonym> getSynonyms();

    /**
     * Searches the catalog for a replacement (master) term for a given term
     * 
     * @param term
     *            the term which is suspected to be a synonym of a master term
     * @return the master term found, or null if none is found
     */
    public String getMasterTerm(String term);
    
    @Override
    public void close();
}
