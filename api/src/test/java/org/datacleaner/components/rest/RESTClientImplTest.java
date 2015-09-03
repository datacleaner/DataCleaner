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

import org.junit.Assert;
import org.junit.Test;

public class RESTClientImplTest {
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";
    private static final String URL = "http://localhost:1234";
    private RESTClient restClient = new RESTClientImpl(this.USERNAME, this.PASSWORD);

    @Test
    public void testSetEndpoint() throws Exception {
        String requestBody = "";

        try {
            restClient.getResponse(requestBody);
        }
        catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("URI is not absolute"));
        }

        restClient.setEndpoint(RESTClient.HttpMethod.GET, this.URL);

        try {
            restClient.getResponse(requestBody);
        }
        catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Connection refused"));
        }
    }

    @Test
    public void testGetResponse() throws Exception {
        try {
            restClient.setEndpoint(RESTClient.HttpMethod.GET, this.URL);
            restClient.getResponse("");
        }
        catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Connection refused"));
        }
    }
}