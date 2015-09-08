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

import org.datacleaner.configuration.DataCleanerConfiguration;

/**
 * A string pattern defines a pattern to which you can match strings to see if
 * they share a similar pattern. Examples of string patterns are:
 * 
 * <ul>
 * <li>The pattern "Aaaaaa Aaaaaaaaaa" which is a typical firstname and lastname
 * pattern.</li>
 * <li>The pattern ".*@.*" which is a simple way to identify strings with an
 * '@'-sign in them (potential email).</li>
 * </ul>
 */
public interface StringPattern extends ReferenceData {

    /**
     * Opens a connection to the {@link StringPattern}. Keep the connection open
     * while using the synonym catalog in a session, job or so. Close it when
     * you don't expect more interaction.
     * 
     * @param configuration
     * @return
     */
    public StringPatternConnection openConnection(DataCleanerConfiguration configuration);
}
