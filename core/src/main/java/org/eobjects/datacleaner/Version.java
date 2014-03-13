/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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

import org.eobjects.datacleaner.util.SystemProperties;
import org.eobjects.metamodel.util.FileHelper;

import com.google.common.base.Strings;

/**
 * Determines and exposes the version of DataCleaner.
 */
public class Version {

    private static final String VERSION = determineVersion();
    private static final String EDITION = determineEdition();

    public static final String EDITION_COMMUNITY = "Community edition";
    public static final String EDITION_PROFESSIONAL = "Professional edition";

    /**
     * @return
     * 
     * @deprecated use {@link #getVersion()} instead
     */
    @Deprecated
    public static String get() {
        return getVersion();
    }

    public static String getVersion() {
        return VERSION;
    }

    public static String getEdition() {
        return EDITION;
    }

    public static String getLicenseKey() {
        return System.getProperty(SystemProperties.LICENSE_KEY);
    }

    private static String determineVersion() {
        final Properties properties = new Properties();
        final InputStream inputStream = Version.class
                .getResourceAsStream("/META-INF/maven/org.eobjects.datacleaner/DataCleaner-core/pom.properties");
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

    private static String determineEdition() {
        String systemProperty = System.getProperty(SystemProperties.EDITION_NAME);
        if (!Strings.isNullOrEmpty(systemProperty)) {
            return systemProperty;
        }

        final Properties properties = new Properties();
        final InputStream inputStream = Version.class
                .getResourceAsStream("/META-INF/maven/com.hi.datacleaner/DataCleaner-enterprise-edition-core-components/pom.properties");
        try {
            properties.load(inputStream);
        } catch (Exception e) {
            // not a commercial edition
            return EDITION_COMMUNITY;
        }

        final String version = properties.getProperty("version");
        if (Strings.isNullOrEmpty(version)) {
            return EDITION_COMMUNITY;
        }

        return EDITION_PROFESSIONAL;
    }

    public static boolean isCommunityEdition() {
        return getEdition() == EDITION_COMMUNITY;
    }
}
