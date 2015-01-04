/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.reference;

import java.util.Collection;

import org.eobjects.analyzer.beans.api.Close;
import org.eobjects.analyzer.beans.api.Initialize;

/**
 * A synonym catalog represents a set of synonyms which are related.
 * 
 * Meaningful examples of synonym catalogs:
 * <ul>
 * <li>Country names (with ISO code as master term)</li>
 * <li>Given name synonyms (eg. 'William' is the master term for 'Billy')</li>
 * </ul>
 * 
 * A synonym catalog can have methods annotated with @Initialize and @Close.
 * These will be called before and after a job is executed where the given
 * synonym catalog is used.
 * 
 * Note: Synonym catalogs should be thread-safe!! Make sure to make sensible use
 * of synchronized blocks if there are race conditions in the SynonymCatalog
 * implementation.
 * 
 * @see Initialize
 * @see Close
 * 
 * 
 */
public interface SynonymCatalog extends ReferenceData {

	/**
	 * @return The name of this synonym catalog
	 */
	public String getName();

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
}
