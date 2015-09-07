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
import static org.datacleaner.metamodel.datahub.DataHubSecurityMode.CAS;

import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.datacleaner.util.SecurityUtils;
import org.datacleaner.util.http.CASMonitorHttpClient;
import org.datacleaner.util.http.HttpBasicMonitorHttpClient;
import org.datacleaner.util.http.MonitorHttpClient;

/**
 * Describes the connection information needed to connect to the DataHub.
 */

public class DataHubConnection {

    public final static String DATASTORES_PATH = "/datastores";
    public final static String CONTEXT_PATH = "/ui";
    public final static String REPOSITORY_PATH = "/repository";
    public final static String CAS_PATH = "/cas";
    public final static String DEFAULT_SCHEMA = "MDM";
    public final static String SCHEMA_EXTENSION = ".schemas";
    public final static String QUERY_EXTENSION = ".query?";

    private final String _hostname;
    private final int _port;
    private final boolean _useHTTPS;
    private final String _tenantId;
    private final String _username;
    private final String _password;

    private final String _scheme;
    private boolean _acceptUnverifiedSslPeers;
    private final DataHubSecurityMode _securityMode;

    public DataHubConnection(String hostname, Integer port, String username, String password, String tenantId,
            boolean useHTTPS, boolean acceptUnverifiedSslPeers, DataHubSecurityMode dataHubSecurityMode) {

        _hostname = hostname;
        _port = port;
        _useHTTPS = useHTTPS;
        _tenantId = tenantId;
        _username = username;
        _password = password;
        _scheme = _useHTTPS ? "https" : "http";
        _acceptUnverifiedSslPeers = acceptUnverifiedSslPeers;
        _securityMode = dataHubSecurityMode;
    }

    public MonitorHttpClient getHttpClient() {

        final HttpClientBuilder clientBuilder = HttpClients.custom().useSystemProperties();
        if (_acceptUnverifiedSslPeers) {
            clientBuilder.setSSLSocketFactory(SecurityUtils.createUnsafeSSLConnectionSocketFactory());
        }
        final CloseableHttpClient httpClient = clientBuilder.build();

        if (CAS.equals(_securityMode)) {
            return new CASMonitorHttpClient(httpClient, getCasServerUrl(), _username, _password, getContextUrl());
        } else {
            return new HttpBasicMonitorHttpClient(httpClient, getHostname(), getPort(), _username, _password);
        }
    }

    public String getHostname() {
        return _hostname;
    }

    public int getPort() {
        return _port;
    }

    public String getRepositoryUrl() {
        return getContextUrl() + REPOSITORY_PATH
                + (isEmpty(_tenantId) ? EMPTY : "/" + urlPathSegmentEscaper().escape(_tenantId));
    }

    private String getContextUrl() {
        URIBuilder uriBuilder = getBaseUrlBuilder();
        appendToPath(uriBuilder, CONTEXT_PATH);
        
        try {
            return uriBuilder.build().toString();
        } catch (URISyntaxException uriSyntaxException) {
            throw new IllegalStateException(uriSyntaxException);
        }
    }

    private String getCasServerUrl() {
        
        URIBuilder uriBuilder = getBaseUrlBuilder();
        appendToPath(uriBuilder, CAS_PATH);

        try {
            return uriBuilder.build().toString();
        } catch (URISyntaxException uriSyntaxException) {
            throw new IllegalStateException(uriSyntaxException);
        }
    }

    private URIBuilder getBaseUrlBuilder() {
        URIBuilder baseUriBuilder = new URIBuilder();
        baseUriBuilder.setScheme(_scheme);
        baseUriBuilder.setHost(_hostname);

        if ((_useHTTPS && _port != 443) || (!_useHTTPS && _port != 80)) {
            // only add port if it differs from default ports of HTTP/HTTPS.
            baseUriBuilder.setPort(_port);
        }
        return baseUriBuilder;
    }
    
    private URIBuilder appendToPath(URIBuilder uriBuilder, String pathSegment) {
        if(uriBuilder.getPath() != null) {
            uriBuilder.setPath(uriBuilder.getPath() + pathSegment);
        }
        
        return uriBuilder.setPath(pathSegment);
    }

}
