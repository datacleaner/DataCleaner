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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentRESTClientTest {
    private static final Logger logger = LoggerFactory.getLogger(ComponentRESTClientTest.class);

    private static final String HOST = "http://localhost:1234";
    private static final String TENANT = "admin";
    private static final String USERNAME = ComponentRESTClientTest.TENANT;
    private static final String PASSWORD = "admin";
    private static final String COMPONENT_NAME = "Concatenator";
    private static final String INSTANCE_ID = "0166fb12-d403-408f-b2bc-61eb898ee338";
    private ComponentRESTClient componentRESTClient = new ComponentRESTClient(this.HOST, this.USERNAME, this.PASSWORD);
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testGetAllComponents() throws Exception {
        try {
            ComponentList componentList = componentRESTClient.getAllComponents(false);
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
            ComponentList.ComponentInfo componentInfo = componentRESTClient.getComponentInfo(this.COMPONENT_NAME, false);
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
            ProcessStatelessOutput processStatelessOutput = componentRESTClient.processStateless(this.COMPONENT_NAME, processStatelessInput);
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
            String response  = componentRESTClient.createComponent(this.COMPONENT_NAME,
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
            ProcessOutput processOutput = componentRESTClient.processComponent(this.INSTANCE_ID, processInput);
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
            ProcessResult processResult = componentRESTClient.getFinalResult(this.INSTANCE_ID);

            if (processResult != null) {
                logger.info(processResult.toString());
            }
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

    private CreateInput getCreateInput() {
        CreateInput createInput = new CreateInput();
        createInput.configuration = getConfiguration();

        return createInput;
    }

    private ProcessInput getProcessInput() {
        ProcessInput processInput = new ProcessInput();
        processInput.data = getInputData();

        return processInput;
    }

    private ComponentConfiguration getConfiguration() {
        ComponentConfiguration componentConfiguration = new ComponentConfiguration();
        JsonNode column1 = getJsonNode("c1");
        JsonNode column2 = getJsonNode("c2");
        componentConfiguration.getColumns().add(column1);
        componentConfiguration.getColumns().add(column2);

        Map<String, JsonNode> properties = new HashMap<>();
        properties.put("Separator", getJsonNode("x"));
        componentConfiguration.getProperties().putAll(properties);

        return componentConfiguration;
    }

    private JsonNode getInputData() {
        try {
            List<String> inputData = new ArrayList<>();
            inputData.add("c1-value");
            inputData.add("c2-value");
            List list = new ArrayList<>();
            list.add(inputData);
            String serialization = objectMapper.writeValueAsString(list);

            return getJsonNode(serialization);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());

            return null;
        }
    }

    private JsonNode getJsonNode(String value) {
        try {
            JsonNode jsonNode = objectMapper.convertValue(value, JsonNode.class);

            return jsonNode;
        }
        catch (IllegalArgumentException e) {
            logger.error(e.getMessage());

            return null;
        }
    }
}