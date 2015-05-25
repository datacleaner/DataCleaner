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
package org.datacleaner;

import java.io.InputStream;
import java.util.Properties;

import org.apache.metamodel.util.FileHelper;
import org.datacleaner.util.SystemProperties;

import com.google.common.base.Strings;

/**
 * Determines and exposes the version of DataCleaner.
 */
public class Version {
    
    public static final String UNKNOWN_VERSION = "UNKNOWN";

    private static final String VERSION;
    private static final String DISTRIBUTION_VERSION;
    private static final String EDITION;

    static {
        VERSION = determineVersion();
        EDITION = determineEdition();
        DISTRIBUTION_VERSION = determineDistributionVersion();
    }

    public static final String EDITION_COMMUNITY = "Community edition";

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
    
    /**
     * Return the major version of current version, e.g. return 4 for 4.0.6
     * 
     * @return major version or null if the current version is UNKNOWN
     */
    public static Integer getMajorVersion() {
        if (VERSION.equals("UNKNOWN")) {
            return null;
        }
        
        String[] versionParts = VERSION.split("\\.");
        int majorVersion = Integer.parseInt(versionParts[0]);
        return majorVersion;
    }
    
    public static String getDistributionVersion() {
        return DISTRIBUTION_VERSION;
    }

    public static String getLicenseKey() {
        return System.getProperty(SystemProperties.LICENSE_KEY);
    }

    private static String determineDistributionVersion() {
        String version = null;

        // try commercial editions packaging
        if (version == null) {
            version = determineVersionFromMavenProperties("com.hi.datacleaner",
                    "DataCleaner-enterprise-edition-core-components", null);
        }

        // try community edition packaging
        if (version == null) {
            version = determineVersionFromMavenProperties("com.hi.datacleaner", "DataCleaner-desktop-app", null);
        }

        // fallback to core version
        if (version == null) {
            version = determineVersion();
        }
        return version;
    }

    private static String determineVersion() {
        return determineVersionFromMavenProperties("org.eobjects.datacleaner", "DataCleaner-api", UNKNOWN_VERSION);
    }

    private static String determineVersionFromMavenProperties(String groupId, String artifactId, String valueIfNull) {
        final Properties properties = new Properties();
        final String resourcePath = "/META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties";
        final InputStream inputStream = Version.class.getResourceAsStream(resourcePath);
        try {
            properties.load(inputStream);
        } catch (Exception e) {
            // do nothing
            System.err.println("Failed to load DataCleaner version from manifest: " + e.getMessage());
            return valueIfNull;
        } finally {
            FileHelper.safeClose(inputStream);
        }

        final String version = properties.getProperty("version", valueIfNull);
        return version;
    }

    private static String determineEdition() {
        final String systemProperty = System.getProperty(SystemProperties.EDITION_NAME);
        if (!Strings.isNullOrEmpty(systemProperty)) {
            return systemProperty;
        }
        return EDITION_COMMUNITY;
    }

    public static boolean isCommunityEdition() {
        return getEdition() == EDITION_COMMUNITY;
    }
}
