package org.datacleaner.components.rest;

/**
 * Basic REST client that requires the endpoint and the requestBody to provide the response.
 * @since 03. 09. 2015
 */
public interface RESTClient {
    public static enum HttpMethod {
        POST,
        GET,
        PUT,
        DELETE,
        ;
    };

    public static enum HttpCode {
        OK(200),
        ;

        private int code = 0;

        private HttpCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    };

    /**
     * Setter for the endpoint (HTTP method and URL).
     *
     * @param httpMethod
     * @param url
     */
    public void setEndpoint(HttpMethod httpMethod, String url);

    /**
     * It returns the response for the given request.
     *
     * @param requestBody
     * @return
     */
    public String getResponse(String requestBody);
}
