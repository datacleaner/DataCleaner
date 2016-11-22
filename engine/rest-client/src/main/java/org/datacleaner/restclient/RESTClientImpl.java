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
package org.datacleaner.restclient;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * @since 03. 09. 2015
 */
public class RESTClientImpl implements RESTClient {

    private static final Logger logger = LoggerFactory.getLogger(RESTClient.class);
    private static final Map<String, Client> clientCache = new ConcurrentHashMap<>();
    private final String dataCleanerVersion;

    private Client client = null;

    public RESTClientImpl(String username, final String password, final String dataCleanerVersion) {
        this.dataCleanerVersion = dataCleanerVersion;
        if (username == null) {
            username = "";
        }

        final String cacheKey = makeKey(username, password);
        client = clientCache.get(cacheKey);

        if (client == null) {
            client = Client.create();
            client.setConnectTimeout(30000);

            if (username != null && password != null) {
                client.addFilter(new HTTPBasicAuthFilter(username, password));
            }

            clientCache.put(cacheKey, client);
        }
    }

    protected Client getClient() {
        return client;
    }

    private String makeKey(final String username, final String password) {
        String key = "";
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (final NoSuchAlgorithmException e) {
            logger.warn("Creation of cache index has failed. " + e.getMessage());
        }

        key = new String(md.digest((username + password).getBytes()));

        return key;
    }

    /**
     * It returns the response for the given request.
     *
     * @param httpMethod
     * @param url
     * @param requestBody
     * @return
     */
    @Override
    public String getResponse(final HttpMethod httpMethod, final String url, final String requestBody) throws RESTClientException {
        final WebResource webResource = client.resource(url);
        final WebResource.Builder builder = webResource
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HEADER_DC_VERSION, dataCleanerVersion);
        ClientResponse response = null;

        if (requestBody != null && !requestBody.isEmpty()) {
            response = builder.method(httpMethod.name(), ClientResponse.class, requestBody);
        } else {
            response = builder.method(httpMethod.name(), ClientResponse.class);
        }

        if (response.getStatus() != ClientResponse.Status.OK.getStatusCode()
                && response.getStatus() != ClientResponse.Status.CREATED.getStatusCode()) {
            String msg = "";

            try {
                final String output = response.getEntity(String.class);
                final String contentType = response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
                if (contentType != null && contentType.contains("json") && output != null && !output.isEmpty()) {
                    final JsonNode respJson = Serializator.getJacksonObjectMapper().readValue(output, JsonNode.class);
                    final JsonNode error = respJson.get("error");
                    if (error != null) {
                        final JsonNode msgNode = error.get("message");
                        if (msgNode != null) {
                            msg = msgNode.asText();
                        }
                    }
                }
            } catch (final Exception e) {
                // DO NOTHING
            }
            if (msg.isEmpty()) {
                msg = response.getStatusInfo().getReasonPhrase();
            }

            throw new RESTClientException(response.getStatus(), msg);

        }

        final String output = response.getEntity(String.class);

        return output;
    }
}
