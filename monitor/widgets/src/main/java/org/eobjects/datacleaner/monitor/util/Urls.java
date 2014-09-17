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
package org.eobjects.datacleaner.monitor.util;

import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

/**
 * Helper class for generating URL's based on a page's current parameters
 */
public final class Urls {

    public static final String CONTEXT_PATH;

    static {
        String host = Window.Location.getHost();
        String baseUrl = GWT.getHostPageBaseURL();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        GWT.log("Base url (0): " + baseUrl);
        baseUrl = baseUrl.substring(baseUrl.indexOf(host));
        GWT.log("Base url (1): " + baseUrl);
        if (baseUrl.indexOf('/') != -1) {
            baseUrl = baseUrl.substring(baseUrl.lastIndexOf('/'));
            GWT.log("Base url (2): " + baseUrl);
        } else {
            baseUrl = "";
        }
        CONTEXT_PATH = baseUrl + '/';
        GWT.log("CONTEXT_PATH: " + CONTEXT_PATH);
    }

    /**
     * Creates a URL relative to the webapp context root.
     * 
     * @param relativePath
     * @return
     */
    public static String createRelativeUrl(String relativePath) {
        if (!GWT.isProdMode()) {
            String gwtCodeServer = Window.Location.getParameter("gwt.codesvr");
            if (gwtCodeServer != null) {
                // build a proper URL with the 'gwt.codesvr' parameter in it.
                final int historyTokenIndex = relativePath.indexOf("#");
                final String historyToken;
                if (historyTokenIndex != -1) {
                    historyToken = "#" + relativePath.substring(historyTokenIndex + 1);
                    relativePath = relativePath.substring(0, historyTokenIndex);
                } else {
                    historyToken = "";
                }

                final int questionMarkIndex = relativePath.indexOf("?");
                if (questionMarkIndex == -1) {
                    relativePath = relativePath + "?gwt.codesvr=" + gwtCodeServer + historyToken;
                } else {
                    relativePath = relativePath + "&gwt.codesvr=" + gwtCodeServer + historyToken;
                }
            }
        }
        return CONTEXT_PATH + relativePath;
    }

    /**
     * Creates a URL relative to the tenant's repository folder URL.
     * 
     * @param tenant
     * @param relativePath
     * @return
     */
    public static String createRepositoryUrl(TenantIdentifier tenant, String relativePath) {
        return createRelativeUrl("repository/" + tenant.getId() + "/" + relativePath);
    }

    /**
     * Utility function to ensure that a URL is properly assigned to the window
     * location, and the location is reloaded no matter what.
     * 
     * @param url
     */
    public static void assign(String url) {
        Window.Location.assign(url);
        Window.Location.reload();
    }
}