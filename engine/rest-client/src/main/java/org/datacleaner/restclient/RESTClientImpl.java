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

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    
    private Client client = null;

    public RESTClientImpl(String username, String password) {
        if (username == null) {
            username = "";
        }

        String cacheKey = makeKey(username, password);
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

    private String makeKey(String username, String password) {
        String key = "";
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
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
    public String getResponse(HttpMethod httpMethod, String url, String requestBody) {
        WebResource webResource = client.resource(url);
        WebResource.Builder builder = webResource
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
        ClientResponse response = null;

        if (requestBody != null && ! requestBody.isEmpty()) {
            response = builder.method(httpMethod.name(), ClientResponse.class, requestBody);
        }
        else {
            response = builder.method(httpMethod.name(), ClientResponse.class);
        }

        if (response.getStatus() != HttpCode.OK.getCode() && response.getStatus() != HttpCode.CREATED.getCode()) {
            throw new RuntimeException(response.getStatusInfo().getReasonPhrase() + " (error code: " + response.getStatus() +")");
        }

        String output = response.getEntity(String.class);

        return output;
    }
}
