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
package org.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.datacleaner.util.SystemProperties;
import org.jdesktop.swingx.action.OpenBrowserAction;

/**
 * ActionListener class for opening up the DataCleaner website in the client
 * computer's browser.
 */
public class OpenDataCleanerWebsiteActionListener implements ActionListener {

    public static final String SYSTEM_PROPERTY_BASE_URL = "datacleaner.website.baseurl";
    public static final String DEFAULT_BASE_URL = "https://datacleaner.github.io";
    
    public static String createUrl(String path) {
        final String baseUrl = SystemProperties.getString(SYSTEM_PROPERTY_BASE_URL, DEFAULT_BASE_URL);
        return baseUrl + path;
    }

    private final String url;
    
    public OpenDataCleanerWebsiteActionListener() {
        this("");
    }

    public OpenDataCleanerWebsiteActionListener(String path) {
        this.url = createUrl(path);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final OpenBrowserAction actionListener = new OpenBrowserAction(url);
        actionListener.actionPerformed(e);
    }

}
