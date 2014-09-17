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
package org.eobjects.datacleaner.monitor.wizard.common;

import java.util.List;
import java.util.Map;

import org.eobjects.datacleaner.monitor.wizard.WizardPageController;

/**
 * Abstract {@link WizardPageController} containing just basic convenience
 * methods for handling requests and responses in the Wizard MVC framework.
 */
public abstract class AbstractWizardPage implements WizardPageController {

    /**
     * Gets the integer value of some form parameter
     * 
     * @param formParameters
     * @param key
     * @return
     * @throws NumberFormatException
     */
    protected static Integer getInteger(Map<String, List<String>> formParameters, String key) throws NumberFormatException {
        String string = getString(formParameters, key);
        if (string == null) {
            return null;
        }
        return Integer.parseInt(string);
    }

    /**
     * Gets the string value of some form parameter
     * 
     * @param formParameters
     * @param key
     * @return
     */
    protected static String getString(Map<String, List<String>> formParameters, String key) {
        List<String> list = formParameters.get(key);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     * Gets the boolean value of some form parameter
     * 
     * @param formParameters
     * @param key
     * @return
     */
    protected static boolean getBoolean(Map<String, List<String>> formParameters, String key) {
        List<String> list = formParameters.get(key);
        if (list == null || list.isEmpty()) {
            return false;
        }
        return "true".equalsIgnoreCase(list.get(0));
    }
}
