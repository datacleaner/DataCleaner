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
package org.datacleaner.monitor.server.wizard.shared;

import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.reference.ReferenceDataCatalog;

public class ReferenceDataHelper {
    public static void checkUniqueDictionary(String name, ReferenceDataCatalog catalog) 
            throws DCUserInputException {
        checkNonEmpty(name);

        if (catalog.containsDictionary(name)) {
            throw new DCUserInputException(String.format("Dictionary '%s' already exists. ", name));
        }
    }
    
    public static void checkUniqueStringPattern(String name, ReferenceDataCatalog catalog) 
            throws DCUserInputException {
        checkNonEmpty(name);
        
        if (catalog.containsStringPattern(name)) {
            throw new DCUserInputException(String.format("String pattern '%s' already exists. ", name));
        }
    }
    
    public static void checkUniqueSynonymCatalog(String name, ReferenceDataCatalog catalog) 
            throws DCUserInputException {
        checkNonEmpty(name);
        
        if (catalog.containsSynonymCatalog(name)) {
            throw new DCUserInputException(String.format("Synonym catalog '%s' already exists. ", name));
        }
    }
    
    private static void checkNonEmpty(String name) throws DCUserInputException {
        if (name == null || name.length() <= 0) {
            throw new DCUserInputException("Specified name is empty. Please provide a name. ");
        } 
    }
}
