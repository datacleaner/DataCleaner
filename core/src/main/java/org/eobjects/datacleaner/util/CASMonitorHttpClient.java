/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.eobjects.metamodel.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MonitorHttpClient} for CAS (Centralized Authentication System) enabled
 * environments.
 */
public class CASMonitorHttpClient implements MonitorHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(CASMonitorHttpClient.class);

    private final HttpClient _httpClient;
    private final String _casUrl;
    private final String _username;
    private final String _password;

    public CASMonitorHttpClient(HttpClient client, String casUrl, String username, String password) {
        _httpClient = client;
        _casUrl = casUrl;
        _username = username;
        _password = password;
    }

    @Override
    public HttpResponse execute(HttpUriRequest request) throws Exception {
        final String ticketServiceUrl = _casUrl + "/v1/tickets";

        logger.debug("Using ticket service url: {}", ticketServiceUrl);

        final HttpPost ticketServiceRequest = new HttpPost(ticketServiceUrl);
        ticketServiceRequest.setEntity(new StringEntity("username=" + _username + "&password=" + _password));
        final HttpResponse casResponse = _httpClient.execute(ticketServiceRequest);
        final StatusLine statusLine = casResponse.getStatusLine();
        final int statusCode = statusLine.getStatusCode();
        
        if (statusCode == 302) {
            final String reason = statusLine.getReasonPhrase();
            throwError(casResponse,
                    "Unexpected HTTP status code from CAS service: 302. This indicates that the RESTful API for CAS is not installed. Reason: " + reason);
        }

        if (statusCode != 201) {
            final String reason = statusLine.getReasonPhrase();
            logger.error("Unexpected HTTP status code from CAS service request: {}. Reason: {}", statusCode, reason);
            throwError(casResponse, statusCode + " - " + reason);
        }

        readResponse(casResponse);

        final Header locationHeader = casResponse.getFirstHeader("Location");
        if (locationHeader == null) {
            throwError(casResponse, "Header 'Location' is null");
        }
        ticketServiceRequest.releaseConnection();

        final String locationResponse = locationHeader.getValue();
        int tgtIndex = locationResponse.indexOf("TGT");
        if (tgtIndex == -1) {
            throwError(casResponse, "No TGT element in 'Location' header: " + locationResponse);
        }
        final String ticketGrantingTicket = locationResponse.substring(tgtIndex);
        if (ticketGrantingTicket == null) {
            throwError(casResponse, "CAS ticket is null");
        }

        logger.debug("Ticket: {}", ticketGrantingTicket);

        // user is authenticated
        if (request instanceof HttpEntityEnclosingRequest) {
            final HttpEntityEnclosingRequest httpEntityEnclosingRequest = (HttpEntityEnclosingRequest) request;
            final List<NameValuePair> parameters = new ArrayList<NameValuePair>();
            parameters.add(new BasicNameValuePair("pgtId", ticketGrantingTicket));
            parameters.add(new BasicNameValuePair("pgtIou", _username));
            Charset charset = Charset.forName("UTF-8");
            final HttpEntity entity = new UrlEncodedFormEntity(parameters, charset);
            httpEntityEnclosingRequest.setEntity(entity);
        } else {
            final HttpParams params = new BasicHttpParams();
            params.setParameter("pgtId", ticketGrantingTicket);
            params.setParameter("pgtIou", _username);
            request.setParams(params);
        }

        return _httpClient.execute(request);

    }

    private void throwError(HttpResponse casResponse, String message) throws Exception {
        readResponse(casResponse);

        throw new IllegalStateException(message);
    }

    public void readResponse(HttpResponse response) throws IOException {
        if (!logger.isDebugEnabled()) {
            return;
        }
        final HttpEntity entity = response.getEntity();
        if (entity == null) {
            return;
        }
        final InputStream in = entity.getContent();
        if (in == null) {
            return;
        }

        try {
            logger.debug("Response:");
            final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            
            String line = reader.readLine();
            while (line != null) {
                logger.debug(line);
                line = reader.readLine();
            }
        } catch (Exception e) {
            logger.warn("Failed to read response entity: " + e.getMessage(), e);
        } finally {
            FileHelper.safeClose(in);
        }
    }
}
