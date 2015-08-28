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

import org.datacleaner.api.Close;
import org.datacleaner.api.Initialize;
import org.datacleaner.configuration.DataCleanerConfiguration;

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
 * @see Initialize
 * @see Close
 * 
 * 
 */
public interface Dictionary extends ReferenceData, Serializable {

    /**
     * Opens a connection to the {@link Dictionary}. Keep the connection open
     * while using the dictionary in a session, job or so. Close it when you
     * don't expect more interaction.
     * 
     * @param configuration
     * @return
     */
    public DictionaryConnection openConnection(DataCleanerConfiguration configuration);
}
