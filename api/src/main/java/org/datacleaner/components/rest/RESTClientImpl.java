package org.datacleaner.components.rest;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import javax.ws.rs.core.MediaType;

/**
 * @since 03. 09. 2015
 */
public class RESTClientImpl implements RESTClient {
    private Client client = null;
    private HttpMethod httpMethod = HttpMethod.GET;
    private String url = "";

    public RESTClientImpl(String username, String password) {
        client = Client.create();

        if (username != null && password != null) {
            client.addFilter(new HTTPBasicAuthFilter(username, password));
        }
    }

    /**
     * Setter for the endpoint (HTTP method and URL).
     *
     * @param httpMethod
     * @param url
     */
    @Override
    public void setEndpoint(HttpMethod httpMethod, String url) {
        this.httpMethod = httpMethod;
        this.url = url;
    }

    /**
     * It returns the response for the given request.
     *
     * @param requestBody
     * @return
     */
    @Override
    public String getResponse(String requestBody) {
        WebResource webResource = client.resource(url);
        webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
        ClientResponse response = webResource.method(httpMethod.name(), ClientResponse.class, requestBody);

        if (response.getStatus() != HttpCode.OK.getCode()) {
            throw new RuntimeException("Request failed. HTTP error code: " + response.getStatus());
        }

        String output = response.getEntity(String.class);

        return output;
    }
}
