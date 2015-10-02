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
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;
import org.datacleaner.util.http.MonitorHttpClient;

public class DataHubRepoConnection  {
    private final static String DATASTORES_PATH = "/datastores";
    private final static String CONTEXT_PATH = "/ui";
    private final static String REPOSITORY_PATH = "/repository";
    private final static String SCHEMA_EXTENSION = ".schemas";
    private final static String QUERY_EXTENSION = ".query?";
 
    DataHubConnection _connection;

    public DataHubRepoConnection(DataHubConnection connection) {
        _connection = connection;
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
    
    public String getRepositoryUrl() {
        return getContextUrl() + REPOSITORY_PATH 
                + (isEmpty(_connection.getTenantId()) ? EMPTY : "/" + urlPathSegmentEscaper().escape(_connection.getTenantId()));
    }

    private URIBuilder appendToPath(URIBuilder uriBuilder, String pathSegment) {
        if(uriBuilder.getPath() != null) {
            uriBuilder.setPath(uriBuilder.getPath() + pathSegment);
        }
        
        return uriBuilder.setPath(pathSegment);
    }

    public String getSchemaUrl(final String datastoreName) {
        final String uri = getRepositoryUrl() + DATASTORES_PATH + "/" 
                + urlPathSegmentEscaper().escape(datastoreName) + SCHEMA_EXTENSION;
        return uri;
    }

    public String getDatastoreUrl() {
        String uri = getRepositoryUrl() + DATASTORES_PATH;
        return uri;
    }

    public String getQueryUrl(DataHubRepoConnection connection, String datastoreName) {
        return connection.getRepositoryUrl() + DATASTORES_PATH + "/"
                + urlPathSegmentEscaper().escape(datastoreName) + QUERY_EXTENSION;
    }

}
