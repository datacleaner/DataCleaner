/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner;

import java.io.InputStream;
import java.util.Properties;

import org.eobjects.metamodel.util.FileHelper;

/**
 * Determines and exposes the version of DataCleaner.
 */
public class Version {

    private static final String VERSION = determineVersion();
    
    public static String get() {
        return VERSION;
    }
    
    private static String determineVersion() {
        final Properties properties = new Properties();
        InputStream inputStream = Version.class.getResourceAsStream("/META-INF/maven/org.eobjects.datacleaner/DataCleaner-core/pom.properties");
        try {
            properties.load(inputStream);
        } catch (Exception e) {
            // do nothing
            System.err.println("Failed to load DataCleaner version from manifest: " + e.getMessage());
            return "UNKNOWN";
        } finally {
            FileHelper.safeClose(inputStream);
        }
        
        final String version = properties.getProperty("version");
        System.out.println("DataCleaner version: " + version);
        return version;
    }
}
