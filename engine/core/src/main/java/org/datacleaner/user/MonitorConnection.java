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
package org.datacleaner.user;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.datacleaner.util.SecurityUtils;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.SystemProperties;
import org.datacleaner.util.http.DefaultCasMonitorHttpClient;
import org.datacleaner.util.http.HttpBasicMonitorHttpClient;
import org.datacleaner.util.http.MonitorHttpClient;
import org.datacleaner.util.http.SimpleWebServiceHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describes the connection information needed to connect to the DataCleaner
 * monitor web application.
 */
public class MonitorConnection implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(MonitorConnection.class);

    private final String _hostname;
    private final int _port;
    private final String _contextPath;
    private final boolean _https;
    private final String _tenantId;
    private final String _username;
    private final String _encodedPassword;
    private final UserPreferences _userPreferences;

    private transient boolean _acceptUnverifiedSslPeers = false;

    public MonitorConnection(UserPreferences userPreferences, String hostname, int port, String contextPath,
            boolean isHttps, String tenantId, String username, char[] password) {
        this(userPreferences, hostname, port, contextPath, isHttps, tenantId, username, SecurityUtils
                .encodePassword(password));
    }

    public MonitorConnection(UserPreferences userPreferences, String hostname, int port, String contextPath,
            boolean isHttps, String tenantId, String username, String encodedPassword) {
        _userPreferences = userPreferences;
        _hostname = hostname;
        _port = port;
        _contextPath = removeEndingSlash(removeBeginningSlash(contextPath));
        _https = isHttps;
        _tenantId = tenantId;
        _username = username;
        _encodedPassword = encodedPassword;
    }

    public MonitorHttpClient getHttpClient() {
        final CloseableHttpClient httpClient;
        if (_userPreferences == null) {
            final HttpClientBuilder clientBuilder = HttpClients.custom().useSystemProperties();
            if (_acceptUnverifiedSslPeers) {
                clientBuilder.setSSLSocketFactory(SecurityUtils.createUnsafeSSLConnectionSocketFactory());
            }
            httpClient = clientBuilder.build();
        } else {
            httpClient = _userPreferences.createHttpClient();
        }

        if (!isAuthenticationEnabled()) {
            return new SimpleWebServiceHttpClient(httpClient);
        }

        final String password = SecurityUtils.decodePassword(getEncodedPassword());
        final String username = getUsername();

        final String securityMode = System.getProperty(SystemProperties.MONITOR_SECURITY_MODE);
        if ("CAS".equalsIgnoreCase(securityMode)) {
            final String casUrl = System.getProperty(SystemProperties.MONITOR_CAS_URL);
            return new DefaultCasMonitorHttpClient(httpClient, casUrl, username, password, getBaseUrl());
        }

        return new HttpBasicMonitorHttpClient(httpClient, getHostname(), getPort(), username, password);
    }

    public String getHostname() {
        return _hostname;
    }

    public int getPort() {
        return _port;
    }

    public boolean isHttps() {
        return _https;
    }

    public String getContextPath() {
        return _contextPath;
    }

    public String getEncodedPassword() {
        return _encodedPassword;
    }

    public String getTenantId() {
        return _tenantId;
    }

    public String getUsername() {
        return _username;
    }

    public String getBaseUrl() {
        StringBuilder sb = new StringBuilder();
        if (_https) {
            sb.append("https://");
        } else {
            sb.append("http://");
        }
        sb.append(_hostname);

        if ((_https && _port != 443) || (!_https && _port != 80)) {
            // only add port if it differs from default ports of HTTP/HTTPS.
            sb.append(':');
            sb.append(_port);
        }

        if (!StringUtils.isNullOrEmpty(_contextPath)) {
            sb.append('/');
            sb.append(_contextPath);
        }

        return sb.toString();
    }

    public String getRepositoryUrl() {
        return getBaseUrl() + "/repository" + (StringUtils.isNullOrEmpty(_tenantId) ? "" : "/" + _tenantId);
    }

    public boolean isAuthenticationEnabled() {
        return !StringUtils.isNullOrEmpty(_username);
    }

    public boolean matchesURI(String uriString) {
        if (uriString == null) {
            return false;
        }
        try {
            URI uri = new URI(uriString);
            return matchesURI(uri);
        } catch (URISyntaxException e) {
            logger.debug("Failed to create URI of string: " + uriString, e);
            return false;
        }
    }

    /**
     * Determines if a {@link URI} matches the configured DC Monitor settings.
     * 
     * @param uri
     * @return
     */
    public boolean matchesURI(URI uri) {
        if (uri == null) {
            return false;
        }
        final String host = uri.getHost();
        if (host.equals(_hostname)) {
            final int port = uri.getPort();
            if (port == _port || port == -1) {
                final String path = removeBeginningSlash(uri.getPath());
                if (StringUtils.isNullOrEmpty(_contextPath) || path.startsWith(_contextPath)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String removeBeginningSlash(String contextPath) {
        if (contextPath == null) {
            return null;
        }
        if (contextPath.startsWith("/")) {
            contextPath = contextPath.substring(1);
        }
        return contextPath;
    }

    private String removeEndingSlash(String contextPath) {
        if (contextPath == null) {
            return null;
        }
        if (contextPath.endsWith("/")) {
            contextPath = contextPath.substring(0, contextPath.length() - 1);
        }
        return contextPath;
    }

    public boolean isAcceptUnverifiedSslPeers() {
        return _acceptUnverifiedSslPeers;
    }

    public void setAcceptUnverifiedSslPeers(boolean acceptUnverifiedSslPeers) {
        _acceptUnverifiedSslPeers = acceptUnverifiedSslPeers;
    }
}
