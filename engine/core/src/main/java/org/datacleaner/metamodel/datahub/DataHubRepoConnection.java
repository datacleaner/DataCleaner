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

import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;

import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;
import org.datacleaner.util.http.MonitorHttpClient;

/**
 * Implements a connection to a DataCleaner Monitor repository built into a
 * DataHub. This class can construct URL's suitable to query (virtual)
 * datastores, retrieve the repository schema.
 * <p>
 * The information is gathered using the authentication included in the call
 * to the REST service, see {@link DataHubConnection}. Use {@link getHttpClient}
 * to get a connection with authentication.
 * <p>
 * Most methods to retrieve a URL require a tenantName, except the method to
 * retrieve the tenant name {@link getUserInfoUrl} .
 */
public class DataHubRepoConnection {
    private final static String DATASTORES_PATH = "/datastores";
    private final static String CONTEXT_PATH = "/ui";
    private final static String REPOSITORY_PATH = "/repository";
    private final static String SCHEMA_EXTENSION = ".schemas";
    private final static String QUERY_EXTENSION = ".query?";
    private final static String USERINFO_PATH = "/_user";

    DataHubConnection _connection;

    public DataHubRepoConnection(DataHubConnection connection) {
        _connection = connection;
    }

    /**
     * Returns a HTTP client containing authentication based on the 
     * information in the underlying connection.
     * @return An HTTP client.
     */
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

    /**
     * Returns the URL to a service to retrieve the user and tenant name of the
     * user.
     * 
     * @return URL to REST service.
     */
    public String getUserInfoUrl() {
        return getContextUrl() + REPOSITORY_PATH + USERINFO_PATH;
    }

    private URIBuilder appendToPath(URIBuilder uriBuilder, String pathSegment) {
        if (uriBuilder.getPath() != null) {
            uriBuilder.setPath(uriBuilder.getPath() + pathSegment);
        }

        return uriBuilder.setPath(pathSegment);
    }

    /**
     * Returns the schema of a specific datastore, including column
     * descriptions, key information.
     * 
     * @param tenantName
     *            The tenant
     * @param datastoreName
     *            The datastore name.
     * @return URL to REST service
     */
    public String getSchemaUrl(String tenantName, String datastoreName) {
        final String uri = getRepoUrlWithTenant(tenantName) + DATASTORES_PATH + "/"
                + urlPathSegmentEscaper().escape(datastoreName) + SCHEMA_EXTENSION;
        return uri;
    }

    /**
     * Returns the URL to retrieve the datastore schemas
     * 
     * @param tenantName
     *            The tenant name
     * @return URL to REST service
     */
    public String getDatastoreUrl(String tenantName) {
        String uri = getRepoUrlWithTenant(tenantName) + DATASTORES_PATH;
        return uri;
    }

    /**
     * Returns the URL to execute queries on the given datastore.
     * 
     * @param tenantName
     *            The tenant name
     * @param datastoreName
     *            The datastore name
     * @return URL to REST service
     */
    public String getQueryUrl(String tenantName, String datastoreName) {
        return getRepoUrlWithTenant(tenantName) + DATASTORES_PATH + "/" + urlPathSegmentEscaper().escape(datastoreName)
                + QUERY_EXTENSION;
    }

    private String getRepoUrlWithTenant(String tenantName) {
        return getContextUrl() + REPOSITORY_PATH + "/" + urlPathSegmentEscaper().escape(tenantName);
    }

    /**
     * Returns the base URL of the DataCleaner monitor, e.g.
     * <code>http://<base-url>/repository</code>.
     * 
     * @return
     */
    public String getRepoUrl() {
        return getContextUrl() + REPOSITORY_PATH;
    }
}
