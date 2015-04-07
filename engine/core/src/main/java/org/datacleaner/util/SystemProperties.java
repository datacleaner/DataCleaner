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

import org.datacleaner.job.AnalysisJob;

import com.google.common.base.Strings;

/**
 * Represents commonly referenced system properties which DataCleaner makes use
 * of.
 */
public class SystemProperties {

    /**
     * Property which in case of a "true" value makes the UI visible even with
     * command line parameters.
     */
    public static final String UI_VISIBLE = "datacleaner.ui.visible";

    /**
     * Property which defines which panel to use for the welcome screen of the
     * desktop UI.
     */
    public static final String UI_DESKTOP_WELCOME_PANEL = "datacleaner.ui.desktop.panel";

    /**
     * Property which defines the text of the "new job" button on the welcome
     * panel
     */
    public static final String UI_DESKTOP_TEXT_NEW_JOB_BUTTON = "datacleaner.ui.desktop.text.button.newjob";

    /**
     * Identifies the name of a client that is embedding datacleaner.
     */
    public static final String EMBED_CLIENT = "datacleaner.embed.client";

    /**
     * Property which in case of a "true" value makes DataCleaner work in
     * "Sandbox" mode which means that it will not assume there's any
     * {@link DataCleanerHome} folder and will not attempt to write any
     * {@link UserPreferences} file etc.
     */
    public static final String SANDBOX = "datacleaner.sandbox";

    /**
     * Property used for keeping the license key for commercial DataCleaner
     * editions.
     */
    public static final String LICENSE_KEY = "datacleaner.license.key";

    /**
     * Identifies the name of the current DataCleaner edition
     */
    public static final String EDITION_NAME = "datacleaner.edition.name";

    /**
     * Property for the hostname of the DC monitor app
     */
    public static final String MONITOR_HOSTNAME = "datacleaner.monitor.hostname";

    /**
     * Property for the port of the DC monitor app
     */
    public static final String MONITOR_PORT = "datacleaner.monitor.port";

    /**
     * Property for the context path of the DC monitor app
     */
    public static final String MONITOR_CONTEXT = "datacleaner.monitor.context";

    /**
     * Property for the tenant of the DC monitor app
     */
    public static final String MONITOR_TENANT = "datacleaner.monitor.tenant";

    /**
     * Property for determining of the DC monitor app is running on HTTPS.
     */
    public static final String MONITOR_HTTPS = "datacleaner.monitor.https";

    /**
     * Property for for the username of the DC monitor app
     */
    public static final String MONITOR_USERNAME = "datacleaner.monitor.username";

    /**
     * Property for the security mode. Set to "CAS" for CAS security, otherwise
     * will default to HTTP BASIC security.
     */
    public static final String MONITOR_SECURITY_MODE = "datacleaner.monitor.security.mode";

    /**
     * Property for the CAS server url, eg. "https://localhost:8443/cas"
     */
    public static final String MONITOR_CAS_URL = "datacleaner.monitor.security.casserverurl";

    /**
     * Property for disabling expected row count in userlogs"
     */
    public static final String MONITOR_LOG_ROWCOUNT = "datacleaner.userlog.rowcount";

    /**
     * Determines if the select clause of queries in DataCleaner should be
     * optimized. If set to "true", DataCleaner may disregard columns set in a
     * {@link AnalysisJob} that are not consumed by any component in the job.
     */
    public static final String QUERY_SELECTCLAUSE_OPTIMIZE = "datacleaner.query.selectclause.optimize";

    /**
     * Gets a system property string, or a replacement value if the property is
     * null or blank.
     * 
     * @param key
     * @param valueIfNull
     * @return
     */
    public static String getString(String key, String valueIfNull) {
        String value = System.getProperty(key);
        if (Strings.isNullOrEmpty(value)) {
            return valueIfNull;
        }
        return value;
    }

    /**
     * Gets a system property long, or a replacement value if the property is
     * null or blank or not parseable
     * 
     * @param key
     * @param valueIfNullOrNotParseable
     * @return
     */
    public static long getLong(String key, long valueIfNullOrNotParseable) {
        String value = System.getProperty(key);
        if (Strings.isNullOrEmpty(value)) {
            return valueIfNullOrNotParseable;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return valueIfNullOrNotParseable;
        }
    }

    /**
     * Gets a system property int, or a replacement value if the property is
     * null or blank or not parseable
     * 
     * @param key
     * @param valueIfNullOrNotParseable
     * @return
     */
    public static long getInt(String key, int valueIfNullOrNotParseable) {
        String value = System.getProperty(key);
        if (Strings.isNullOrEmpty(value)) {
            return valueIfNullOrNotParseable;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return valueIfNullOrNotParseable;
        }
    }

    /**
     * Gets a system property boolean, or a replacement value if the property is
     * null or blank or not parseable as a boolean.
     * 
     * @param key
     * @param valueIfNull
     * @return
     */
    public static boolean getBoolean(String key, boolean valueIfNull) {
        String value = System.getProperty(key);
        if (Strings.isNullOrEmpty(value)) {
            return valueIfNull;
        }

        value = value.trim().toLowerCase();

        if ("true".equals(value)) {
            return true;
        } else if ("false".equals(value)) {
            return false;
        }

        return valueIfNull;
    }
}
