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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
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

public class DatahubConnection {

    private final String _hostname;
    private final int _port;
    private final boolean _https;
    private final String _tenantId;
    private final String _username;
    private final String _password;
    private final String _contextPath = "/ui";
    private final String _datahubContext = "/datastores/Golden%20record";
    private final String _scheme;
//    HttpClientContext _context;
    private boolean _acceptUnverifiedSslPeers;

    public DatahubConnection(String hostname, Integer port, String username,
            String password, String tenantId, boolean https) {
        _hostname = hostname;
        _port = port;
        _https = https;
        _tenantId = tenantId;
        _username = username;
        _password = password;
        if (_https) {
            _scheme = "https";
        } else {
            _scheme = "http";
        }
        _acceptUnverifiedSslPeers = true;

//        CredentialsProvider credsProvider = new BasicCredentialsProvider();
//        credsProvider.setCredentials(
//                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
//                new UsernamePasswordCredentials(getUsername(), getPassword()));
//        _context = HttpClientContext.create();
//        _context.setCredentialsProvider(credsProvider);
    }

    public MonitorHttpClient getHttpClient() {

        final HttpClientBuilder clientBuilder = HttpClients.custom()
                .useSystemProperties();
        if (_acceptUnverifiedSslPeers) {
            clientBuilder.setSSLSocketFactory(
                    SecurityUtils.createUnsafeSSLConnectionSocketFactory());
        }
        final CloseableHttpClient httpClient = clientBuilder.build();

        // if (!isAuthenticationEnabled()) {
        // return new SimpleWebServiceHttpClient(httpClient);
        // }

        // final String password =
        // SecurityUtils.decodePassword(getEncodedPassword());
        // final String username = getUsername();

//        final String securityMode = System
//                .getProperty(SystemProperties.MONITOR_SECURITY_MODE);
        final String securityMode = "cas";
        if ("CAS".equalsIgnoreCase(securityMode)) {
//            final String casUrl = System
//                    .getProperty(SystemProperties.MONITOR_CAS_URL);
            final String casUrl = "https://mdmregtest.humaninference.com:8443/cas";
            return new CASMonitorHttpClient(httpClient, casUrl, _username,
                    _password, getBaseUrl());
        }

        return new HttpBasicMonitorHttpClient(httpClient, getHostname(),
                getPort(), _username, _password);
    }

    public String getHostname() {
        return _hostname;
    }

    public String getContextPath() {
        return _contextPath;
    }

    public boolean isHttps() {
        return _https;
    }

    public int getPort() {
        return _port;
    }

    public String getTenantId() {
        return _tenantId;
    }

    public String getUsername() {
        return _username;
    }

    public String getPassword() {
        return _password;
    }

    public String getRepositoryUrl() {
        return getBaseUrl() + "/repository"
                + (StringUtils.isEmpty(_tenantId) ? "" : "/" + _tenantId);
    }

    public String getBaseUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append(_scheme).append("://" + _hostname);

        if ((_https && _port != 443) || (!_https && _port != 80)) {
            // only add port if it differs from default ports of HTTP/HTTPS.
            sb.append(':');
            sb.append(_port);
        }

        if (!StringUtils.isEmpty(_contextPath)) {
            sb.append(_contextPath);
        }

        return sb.toString();
    }

    public HttpHost getHttpHost() {
        return new HttpHost(_hostname, _port, _scheme);
    }

    public String getDatahubContextPath() {
        return _datahubContext;
    }

//    public HttpContext getContext() {
//        return _context;
//    }

}
