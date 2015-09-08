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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentRESTClientImplTest {
    private static final Logger logger = LoggerFactory.getLogger(ComponentRESTClientImplTest.class);
    
    private static final String HOST = "http://localhost:1234";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";
    private static final String COMPONENT_NAME = "Concatenator";
    private static final String INSTANCE_ID = "f23167f4-af49-4d58-a77c-f366b454b42d";
    private ComponentRESTClient componentRESTClient = new ComponentRESTClientImpl(
            this.HOST, this.USERNAME, this.PASSWORD);

    @Test
    public void testGetAllComponents() throws Exception {
        try {
            String response = componentRESTClient.getAllComponents();
            logger.info(response);
            Assert.assertFalse(response.isEmpty());
        }
        catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Connection refused"));
        }
    }

    @Test
    public void testGetComponentInfo() throws Exception {
        try {
            String response = componentRESTClient.getComponentInfo(this.COMPONENT_NAME);
            logger.info(response);
            Assert.assertFalse(response.isEmpty());
        }
        catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Connection refused"));
        }
    }

    @Test
    public void testProcessStateless() throws Exception {
        try {
            String input = "{" + getConfiguration() + "," + getInputData() + "}";
            String response = componentRESTClient.processStateless(this.COMPONENT_NAME, input);
            logger.info(response);
            Assert.assertFalse(response.isEmpty());
        }
        catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Connection refused"));
        }
    }

    @Test
    public void testCreateComponent() throws Exception {
        try {
            String timeout = "60000";
            String input = "{" + getConfiguration() + "}";
            String response  = componentRESTClient.createComponent(this.COMPONENT_NAME, timeout, input);
            logger.info(response);
            Assert.assertFalse(response.isEmpty());
        }
        catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Connection refused"));
        }
    }

    @Test
    public void testProcessComponent() throws Exception {
        try {
            String input =  "{" + getInputData() + "}";
            String response = componentRESTClient.processComponent(this.INSTANCE_ID, input);
            logger.info(response);
            Assert.assertFalse(response.isEmpty());
        }
        catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Connection refused"));
        }
    }

    @Test
    public void testGetFinalResult() throws Exception {
        try {
            String response = componentRESTClient.getFinalResult(this.INSTANCE_ID);
            logger.info(response);
            Assert.assertTrue(response.isEmpty());
        }
        catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Connection refused"));
        }
    }

    @Test
    public void testDeleteComponent() throws Exception {
        try {
            componentRESTClient.deleteComponent(this.INSTANCE_ID);
        }
        catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Connection refused") || e.getMessage().contains("HTTP error code: 404"));
        }
    }

    private String getConfiguration() {
        return "\"configuration\": { \"properties\" : { \"Columns\": [ \"c1\", \"c2\" ], \"Separator\": \"x\" }, \"columns\": [ \"c1\", \"c2\" ] }";
    }

    private String getInputData() {
        return "\"data\": [ [ \"c1-value\", \"c2-value\" ] ]";
    }
}