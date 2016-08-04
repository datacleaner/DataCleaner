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

import org.junit.Assert;
import org.junit.Test;

import com.sun.jersey.api.client.Client;

public class RESTClientImplTest {
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";
    private static final String URL = "http://localhost:1234";
    private RESTClient restClient = new RESTClientImpl(USERNAME, PASSWORD, "test");

    @Test
    public void testSetEndpoint() throws Exception {
        String requestBody = "";

        try {
            restClient.getResponse(RESTClient.HttpMethod.GET, "", requestBody);
        }
        catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("URI is not absolute"));
        }

        try {
            restClient.getResponse(RESTClient.HttpMethod.GET, URL, requestBody);
        }
        catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Connection refused"));
        }
    }

    @Test
    public void testGetResponse() throws Exception {
        try {
            restClient.getResponse(RESTClient.HttpMethod.GET, URL, "");
        }
        catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Connection refused"));
        }
    }

    @Test
    public void testCache() {
        String username = "username";
        String password = "password";
        Client instance1 = new RESTClientImpl(username, password, "test").getClient();
        Client instance2 = new RESTClientImpl(username + "2", password + "2", "test").getClient();
        Client instance3 = new RESTClientImpl(username, password, "test").getClient();
        Client instance4 = new RESTClientImpl(username, password + "4", "test").getClient();

        Assert.assertEquals(instance1, instance3);
        Assert.assertNotEquals(instance1, instance2);
        Assert.assertNotEquals(instance1, instance4);
    }
}