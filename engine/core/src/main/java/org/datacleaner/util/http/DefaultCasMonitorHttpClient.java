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
package org.datacleaner.util.http;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * {@link MonitorHttpClient} for CAS (Centralized Authentication System) enabled
 * environments.
 * 
 * This client requires that CAS is installed with the RESTful API, which is
 * described in detail here: https://wiki.jasig.org/display/CASUM/RESTful+API
 *
 */
public class DefaultCasMonitorHttpClient extends CASMonitorHttpClient {

    public DefaultCasMonitorHttpClient(CloseableHttpClient client, String casServerUrl, String username,
            String password, String monitorBaseUrl) {
        super(client, casServerUrl, username, password, monitorBaseUrl);
    }

    @Override
    protected void addSecurityHeaders(HttpUriRequest request) {
        // Nothing to do
    }

}
