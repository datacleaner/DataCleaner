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
package org.datacleaner.test;

/**
 * A utility class containing methods useful during unit testing.
 *
 */
public class AssertHelper {

    public static final String FILE_PATH_REPLACEMENT = "[MASKED FILE PATH]";

    /**
     * Replaces every occurence of a file path starting with file:// prefix with
     * [MASKED FILE PATH]. Useful for testing DataCleaner job XML files against benchmarks
     * 
     * Example: Replaces 
     * <property name="File" value="file://C:/CUSTOMERS_address_correction.csv"/> 
     * with <property name="File" value="file://[MASKED FILE PATH]"/>
     * 
     * @param fileString
     *            The text to replace file paths in
     * @return
     */
    public static String maskFilePaths(final String fileString) {
        final String filePrefix = "\"file://";
        return fileString.replaceAll(filePrefix + "(.*?)\"", filePrefix + FILE_PATH_REPLACEMENT + "\"");
    }

}
