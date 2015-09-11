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

import com.fasterxml.jackson.databind.JsonNode;

public class ComponentRESTClientTest {
    private static final Logger logger = LoggerFactory.getLogger(ComponentRESTClientTest.class);
    
    private static final String HOST = "http://localhost:1234";
    private static final String TENANT = "admin";
    private static final String USERNAME = ComponentRESTClientTest.TENANT;
    private static final String PASSWORD = "admin";
    private static final String COMPONENT_NAME = "Concatenator";
    private static final String INSTANCE_ID = "f23167f4-af49-4d58-a77c-f366b454b42d";
    private ComponentRESTClient componentRESTClient = new ComponentRESTClient(
            this.HOST, this.USERNAME, this.PASSWORD);

    @Test
    public void testGetAllComponents() throws Exception {
        try {
            ComponentList componentList = componentRESTClient.getAllComponents(this.TENANT);
            logger.info(componentList.toString());
            Assert.assertTrue(componentList != null);
        }
        catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Connection refused"));
        }
    }

    @Test
    public void testGetComponentInfo() throws Exception {
        try {
            ComponentList.ComponentInfo componentInfo = componentRESTClient.getComponentInfo(this.TENANT, this.COMPONENT_NAME);
            logger.info(componentInfo.toString());
            Assert.assertTrue(componentInfo != null);
        }
        catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Connection refused"));
        }
    }

    @Test
    public void testProcessStateless() throws Exception {
        try {
            ProcessStatelessInput processStatelessInput = new ProcessStatelessInput();
            processStatelessInput.configuration = getConfiguration();
            processStatelessInput.data = getInputData();
            ProcessStatelessOutput processStatelessOutput = componentRESTClient.processStateless(this.TENANT,
                    this.COMPONENT_NAME, processStatelessInput);
            logger.info(processStatelessOutput.toString());
            Assert.assertTrue(processStatelessOutput != null);
        }
        catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Connection refused"));
        }
    }

    @Test
    public void testCreateComponent() throws Exception {
        try {
            String timeout = "60000";
            CreateInput createInput = getCreateInput();
            String response  = componentRESTClient.createComponent(this.TENANT, this.COMPONENT_NAME,
                    timeout, createInput);
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
            ProcessInput processInput = getProcessInput();
            ProcessOutput processOutput = componentRESTClient.processComponent(this.TENANT, this.INSTANCE_ID, processInput);
            logger.info(processOutput.toString());
            Assert.assertTrue(processOutput != null);
        }
        catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Connection refused"));
        }
    }

    @Test
    public void testGetFinalResult() throws Exception {
        try {
            ProcessResult processResult = componentRESTClient.getFinalResult(this.TENANT, this.INSTANCE_ID);
            logger.info(processResult.toString());
            Assert.assertTrue(processResult != null);
        }
        catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Connection refused"));
        }
    }

    @Test
    public void testDeleteComponent() throws Exception {
        try {
            componentRESTClient.deleteComponent(this.TENANT, this.INSTANCE_ID);
        }
        catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Connection refused") || e.getMessage().contains("HTTP error code: 404"));
        }
    }

    private CreateInput getCreateInput() {
        return null; // mytodo
    }

    private ProcessInput getProcessInput() {
        return null; // mytodo
    }

    private ComponentConfiguration getConfiguration() {
        //return "\"configuration\": { \"properties\" : { \"Columns\": [ \"c1\", \"c2\" ], \"Separator\": \"x\" }, \"columns\": [ \"c1\", \"c2\" ] }";
        return new ComponentConfiguration(); // mytodo
    }

    private JsonNode getInputData() {
        //return "\"data\": [ [ \"c1-value\", \"c2-value\" ] ]";
        return null; // mytodo
    }
}