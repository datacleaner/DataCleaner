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
package org.eobjects.datacleaner.util;

import org.eobjects.datacleaner.user.UserPreferences;

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

}
