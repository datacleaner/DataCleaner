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

import org.datacleaner.beans.api.Close;
import org.datacleaner.beans.api.Initialize;

/**
 * A dictionary represents a set of values grouped together with a label.
 * 
 * Examples of meaningful dictionaries:
 * <ul>
 * <li>Lastnames</li>
 * <li>Female given names</li>
 * <li>Product codes</li>
 * </ul>
 * 
 * Often times a dictionary will implement a caching mechanism to prevent having
 * to hold all values of the dictionary in memory.
 * 
 * A dictionary can have methods annotated with @Initialize and @Close. These
 * will be called before and after a job is executed where the given dictionary
 * is used.
 * 
 * Note: Dictionaries should be thread-safe!! Make sure to make sensible use of
 * synchronized blocks if there are race conditions in the dictionary
 * implementation.
 * 
 * @see Initialize
 * @see Close
 * 
 * 
 */
public interface Dictionary extends ReferenceData {

	public boolean containsValue(String value);

	/**
	 * Gets the dictionaries contents as a ReferenceValues object. Use with
	 * caution because this might require the dictionary to do eager
	 * initialization of all values.
	 * 
	 * @return
	 */
	public ReferenceValues<String> getValues();
}
