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
package org.datacleaner.metamodel.datahub;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.datacleaner.util.http.HttpBasicMonitorHttpClient;

/**
 * CAS HTTP client version specific for use with DataHub. Adds extra request headers
 * to drive security on the DataHub REST services.
 *
 */
public class DataHubDefaultMonitorHttpClient extends HttpBasicMonitorHttpClient {
    
    private final String _userName;

    public DataHubDefaultMonitorHttpClient(CloseableHttpClient client, String host, int port, String username, String password) {
        super(client, host, port, username,password);
        this._userName = username;
    }
    
    private static final String CDI_TICKET_HEADER = "CDI-ticket";
    private static final String CDI_SERVICE_URL_HEADER = "CDI-serviceUrl";
    private static final String CDI_USERID = "CDI-userId";

    @Override
    protected void addSecurityHeaders(HttpUriRequest request) throws Exception {
        request.addHeader(CDI_TICKET_HEADER, "dummy-string");
        request.addHeader(CDI_SERVICE_URL_HEADER, "dummy-string");
        request.addHeader(CDI_USERID, _userName);        
    }

}
