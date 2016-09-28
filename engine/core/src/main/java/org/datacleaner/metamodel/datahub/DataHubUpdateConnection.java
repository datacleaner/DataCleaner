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

import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;
import org.datacleaner.util.http.MonitorHttpClient;

/**
 * Implements a connection from the DataHub datastore to the DataHub REST
 * services in the service war.
 *
 * Note: Some REST controllers do not need the tenant info. Others do.
 */
public class DataHubUpdateConnection {
    public final static String CONTEXT_PATH = "/service/v1";
    public final static String GOLDEN_RECORDS_PATH = "/goldenrecords/batch";
    public final static String SOURCE_RECORDS_PATH = "/sourcerecords/batch";

    private final DataHubConnection _connection;

    public DataHubUpdateConnection(DataHubConnection connection) {
        _connection = connection;
    }

    public String getGoldenRecordBatchUrl() {
        return getContextUrl() + GOLDEN_RECORDS_PATH;
    }

    public String getSourceRecordBatchUrl() {
        return getContextUrl() + SOURCE_RECORDS_PATH;
    }

    public MonitorHttpClient getHttpClient() {
        return _connection.getHttpClient(getContextUrl());
    }

    private String getContextUrl() {
        URIBuilder uriBuilder = _connection.getBaseUrlBuilder();
        appendToPath(uriBuilder, CONTEXT_PATH);

        try {
            return uriBuilder.build().toString();
        } catch (URISyntaxException uriSyntaxException) {
            throw new IllegalStateException(uriSyntaxException);
        }
    }

    private URIBuilder appendToPath(URIBuilder uriBuilder, String pathSegment) {
        if (uriBuilder.getPath() != null) {
            uriBuilder.setPath(uriBuilder.getPath() + pathSegment);
        }

        return uriBuilder.setPath(pathSegment);
    }

}
