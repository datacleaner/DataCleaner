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
package org.datacleaner.util;

/**
 * StringUtils for datacleaner Api
 */
public class ApiStringUtils {

    public static String explodeCamelCase(String str, boolean excludeGetOrSet) {
        if (str == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(str.trim());
        if (sb.length() > 1) {
            if (excludeGetOrSet) {
                if (str.startsWith("get") || str.startsWith("set")) {
                    sb.delete(0, 3);
                }
            }

            // Special handling for instance variables that have the "_" prefix
            if (sb.charAt(0) == '_') {
                sb.deleteCharAt(0);
            }

            // First character is set to uppercase
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));

            boolean previousUpperCase = true;

            for (int i = 1; i < sb.length(); i++) {
                char currentChar = sb.charAt(i);
                if (!previousUpperCase) {
                    if (Character.isUpperCase(currentChar)) {
                        sb.setCharAt(i, Character.toLowerCase(currentChar));
                        sb.insert(i, ' ');
                        i++;
                    }
                } else {
                    if (Character.isLowerCase(currentChar)) {
                        previousUpperCase = false;
                    }
                }
            }
        }
        return sb.toString();
    }
}
