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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLPeerUnverifiedException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.LazyRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MonitorHttpClient} for CAS (Centralized Authentication System) enabled
 * environments.
 * 
 * This client requires that CAS is installed with the RESTful API, which is
 * described in detail here: https://wiki.jasig.org/display/CASUM/RESTful+API
 */
public class CASMonitorHttpClient implements MonitorHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(CASMonitorHttpClient.class);
    
    private final Charset charset = Charset.forName("UTF-8");

    private final CloseableHttpClient _httpClient;
    private final String _casServerUrl;
    private final String _username;
    private final String _password;
    private final String _monitorBaseUrl;
    private final LazyRef<String> _ticketGrantingTicketRef;
    private String _requestedService;
    private String _casRestServiceUrl;

    public CASMonitorHttpClient(CloseableHttpClient client, String casServerUrl, String username, String password,
            String monitorBaseUrl) {
        _httpClient = client;
        _casServerUrl = casServerUrl;
        _username = username;
        _password = password;
        _monitorBaseUrl = monitorBaseUrl;
        _requestedService = _monitorBaseUrl + "/j_spring_cas_security_check";
        _casRestServiceUrl = _casServerUrl + "/v1/tickets";
        _ticketGrantingTicketRef = createTicketGrantingTicketRef();

        logger.debug("Requested service url: {}", _requestedService);
        logger.debug("Using CAS service url: {}", _casRestServiceUrl);
    }

    private LazyRef<String> createTicketGrantingTicketRef() {
        return new LazyRef<String>() {
            @Override
            protected String fetch() {
                // the requested service (from CAS's perspective) is the spring
                // security 'j_spring_cas_security_check' filter.

                // we use the RESTful CAS api to get tickets
                try {
                    final String ticketGrantingTicket = getTicketGrantingTicket(_casRestServiceUrl);
                    logger.debug("Got a ticket granting ticket: {}", ticketGrantingTicket);

                    return ticketGrantingTicket;
                } catch (Exception e) {
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    }
                    throw new IllegalStateException("Failed to fetch ticket granting ticket from CAS", e);
                }
            }
        };
    }

    @Override
    public HttpResponse execute(final HttpUriRequest request) throws Exception {
        // enable cookies
        final CookieStore cookieStore = new BasicCookieStore();
        final HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
        context.setRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build());

        final String ticketGrantingTicket = retrieveTicketGrantingTicket();

        final String ticket = getTicket(_requestedService, _casRestServiceUrl, ticketGrantingTicket, context);
        logger.debug("Got a service ticket: {}", ticketGrantingTicket);
        logger.debug("Cookies 2: {}", cookieStore.getCookies());

        // now we request the spring security CAS check service, this will set
        // cookies on the client.
        final HttpGet cookieRequest = new HttpGet(_requestedService + "?ticket=" + ticket);
        final HttpResponse cookieResponse = executeHttpRequest(cookieRequest, context);
        EntityUtils.consume(cookieResponse.getEntity());
        cookieRequest.releaseConnection();
        logger.debug("Cookies 3: {}", cookieStore.getCookies());

        addSecurityHeaders(request);
        
        final HttpResponse result = executeHttpRequest(request, context);
        logger.debug("Cookies 4: {}", cookieStore.getCookies());

        return result;
    }

    protected String retrieveTicketGrantingTicket() throws Exception {
        try {
            return _ticketGrantingTicketRef.get();
        } catch (IllegalStateException e) {
            if (e.getCause() instanceof SSLPeerUnverifiedException) {
                // Unverified SSL peer exceptions needs to be rethrown
                // specifically, since they can be caught and the user may
                // decide to remove certificate checks.
                throw (SSLPeerUnverifiedException) e.getCause();
            }
            throw e;
        }        
    }
    
    /**
     * Override this method to add extra security headers when needed.
     * 
     * @param request The request.
     * @throws Exception Adding the headers resulted in a problem.
     */
    protected void addSecurityHeaders(HttpUriRequest request) throws Exception {
        // Nothing to do...
    }
    
    protected String getTicket(final String requestedService, final String casServiceUrl,
            final String ticketGrantingTicket, HttpContext context) throws IOException, Exception {
        final HttpPost post = new HttpPost(casServiceUrl + "/" + ticketGrantingTicket);
        final List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("service", requestedService));
        final HttpEntity entity = new UrlEncodedFormEntity(parameters, charset);
        post.setEntity(entity);

        final HttpResponse response = executeHttpRequest(post, context);
        final String ticket = readResponse(response.getEntity());
        post.releaseConnection();
        return ticket;
    }

    protected HttpResponse executeHttpRequest(HttpUriRequest req, HttpContext context) throws IOException {
        logger.debug("Executing HTTP request: {}", req);
        return _httpClient.execute(req, context);
    }

    private String getTicketGrantingTicket(final String casServiceUrl) throws Exception {
        final HttpPost ticketServiceRequest = new HttpPost(casServiceUrl);
        ticketServiceRequest.setEntity(new StringEntity("username=" + _username + "&password=" + _password));
        final HttpResponse casResponse = executeHttpRequest(ticketServiceRequest, null);
        final StatusLine statusLine = casResponse.getStatusLine();
        final int statusCode = statusLine.getStatusCode();

        if (statusCode == 302) {
            final String reason = statusLine.getReasonPhrase();
            throwError("Unexpected HTTP status code from CAS service: 302. This indicates that the RESTful API for CAS is not installed. Reason: "
                    + reason);
        }

        if (statusCode != 201) {
            final String reason = statusLine.getReasonPhrase();
            logger.error("Unexpected HTTP status code from CAS service request: {}. Reason: {}", statusCode, reason);
            throwError(statusCode + " - " + reason);
        }

        final Header locationHeader = casResponse.getFirstHeader("Location");
        if (locationHeader == null) {
            throwError("Header 'Location' is null");
        }
        ticketServiceRequest.releaseConnection();

        final String locationResponse = locationHeader.getValue();
        int tgtIndex = locationResponse.indexOf("TGT");
        if (tgtIndex == -1) {
            throwError("No TGT element in 'Location' header: " + locationResponse);
        }
        final String ticketGrantingTicket = locationResponse.substring(tgtIndex);
        if (ticketGrantingTicket == null) {
            throwError("CAS ticket is null");
        }

        EntityUtils.consume(casResponse.getEntity());

        return ticketGrantingTicket;
    }

    private String readResponse(HttpEntity entity) throws Exception {
        final InputStream in = entity.getContent();
        if (in == null) {
            return null;
        }

        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            final StringBuilder sb = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                if (sb.length() != 0) {
                    sb.append('\n');
                }
                sb.append(line);
                line = reader.readLine();
            }
            final String result = sb.toString();
            logger.debug("Response: ", result);
            return result;
        } finally {
            FileHelper.safeClose(in);
        }
    }

    private void throwError(String message) throws Exception {
        throw new IllegalStateException(message);
    }

    @Override
    public void close() {
        if (_ticketGrantingTicketRef.isFetched()) {
            // Fire a HTTP DELETE request to "log out"
            final String ticketGrantingTicket = _ticketGrantingTicketRef.get();
            final HttpDelete request = new HttpDelete(_casRestServiceUrl + "/" + ticketGrantingTicket);
            try {
                final HttpResponse response = executeHttpRequest(request, null);
                if (logger.isDebugEnabled()) {
                    final String responseStr = readResponse(response.getEntity());
                    logger.debug("Log out response: {}", responseStr);
                } else {
                    EntityUtils.consume(response.getEntity());
                }
            } catch (Exception e) {
                logger.warn("Failed to log out of CAS: " + e.getMessage(), e);
            } finally {
                request.releaseConnection();
            }
        }
        
        FileHelper.safeClose(_httpClient);
    }

    /**
     * Returns the CAS server URL
     * @return the CAS Server Url
     */
    protected String getCasServerUrl() {
        return _casServerUrl;
    }
    
    /**
     * Returns the user name.
     * @return The username.
     */
    protected String getUsername() {
        return _username;
    }
}
