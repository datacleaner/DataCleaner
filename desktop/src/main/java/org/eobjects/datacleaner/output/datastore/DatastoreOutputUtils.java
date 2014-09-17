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
package org.eobjects.datacleaner.output.datastore;

import java.io.File;

import org.eobjects.analyzer.util.CharIterator;
import org.eobjects.analyzer.util.StringUtils;

final class DatastoreOutputUtils {

    private DatastoreOutputUtils() {
        // prevent instantiation
    }

    public static String safeName(String str) {
        if (StringUtils.isNullOrEmpty(str)) {
            throw new IllegalArgumentException("Cannot create safe name from empty/null string: " + str);
        }

        CharIterator ci = new CharIterator(str);
        while (ci.hasNext()) {
            ci.next();
            if (!ci.isLetter() && !ci.isDigit()) {
                // replaces unexpected chars with underscore
                ci.set('_');
            }
        }

        str = ci.toString();
        if (!Character.isLetter(str.charAt(0))) {
            str = "db" + str;
        }
        return str;
    }

    public static String getJdbcUrl(File directory, String dbName) {
        final String urlSuffix = directory.getPath() + File.separatorChar + safeName(dbName);
        return "jdbc:h2:" + urlSuffix + ";FILE_LOCK=FS";
    }
}
