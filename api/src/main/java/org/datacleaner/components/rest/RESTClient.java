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
        CREATED(201),
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
