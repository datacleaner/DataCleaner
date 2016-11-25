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

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SerializatorTest {
    private static final Logger logger = LoggerFactory.getLogger(Serializator.class);
    private final String componentName = "Concatenator";
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testComponentList() throws Exception {
        final ComponentList componentList = new ComponentList();
        componentList.add(getComponentInfo());

        final String serialization = intoString(componentList);
        final ComponentList componentList2 = Serializator.componentList(serialization);
        final String serialization2 = intoString(componentList2);

        Assert.assertTrue(serialization != null);
        Assert.assertTrue(serialization.equals(serialization2));
    }

    private ComponentList.ComponentInfo getComponentInfo() {
        final ComponentList.ComponentInfo componentInfo = new ComponentList.ComponentInfo();
        componentInfo.setName("name");
        componentInfo.setCreateURL("http://create.url");

        return componentInfo;
    }

    private String intoString(final Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (final JsonProcessingException e) {
            logger.error(e.getMessage());

            return "";
        }
    }

    private Object fromString(final String value, final Class<?> type) {
        try {
            return objectMapper.readValue(value, type);
        } catch (final IOException e) {
            logger.error(e.getMessage());

            return null;
        }
    }

    @Test
    public void testComponentInfo() throws Exception {
        final ComponentList.ComponentInfo componentInfo = new ComponentList.ComponentInfo();
        componentInfo.setName(componentName);

        final String serialization = intoString(componentInfo);
        final ComponentList.ComponentInfo componentInfo2 = Serializator.componentInfo(serialization);
        final String serialization2 = intoString(componentInfo2);

        Assert.assertTrue(serialization != null);
        Assert.assertTrue(serialization.equals(serialization2));
    }

    @Test
    public void testStringProcessStatelessInput() throws Exception {
        final ProcessStatelessInput processStatelessInput = new ProcessStatelessInput();
        processStatelessInput.configuration = getComponentConfiguration();
        processStatelessInput.data = (JsonNode) fromString("data", JsonNode.class);

        final String serialization = intoString(processStatelessInput);
        final String serialization2 = Serializator.stringProcessStatelessInput(processStatelessInput);

        Assert.assertTrue(serialization != null);
        Assert.assertTrue(serialization.equals(serialization2));
    }

    private ComponentConfiguration getComponentConfiguration() {
        final ComponentConfiguration componentConfiguration = new ComponentConfiguration();
        final JsonNode property = (JsonNode) fromString("propertyValue", JsonNode.class);
        componentConfiguration.getProperties().put("propertyKey", property);

        return componentConfiguration;
    }

    @Test
    public void testProcessStatelessOutput() throws Exception {
        final ProcessStatelessOutput processStatelessOutput = new ProcessStatelessOutput();
        processStatelessOutput.result = (JsonNode) fromString("result", JsonNode.class);
        processStatelessOutput.rows = (JsonNode) fromString("rows", JsonNode.class);

        final String serialization = intoString(processStatelessOutput);
        final ProcessStatelessOutput processStatelessOutput2 = Serializator.processStatelessOutput(serialization);
        final String serialization2 = intoString(processStatelessOutput2);

        Assert.assertTrue(serialization != null);
        Assert.assertTrue(serialization.equals(serialization2));
    }

    @Test
    public void testStringCreateInput() throws Exception {
        final CreateInput processInput = new CreateInput();
        processInput.configuration = getComponentConfiguration();

        final String serialization = Serializator.stringCreateInput(processInput);
        final CreateInput processInput2 = (CreateInput) fromString(serialization, CreateInput.class);
        final String serialization2 = intoString(processInput2);

        Assert.assertTrue(serialization != null);
        Assert.assertTrue(serialization.equals(serialization2));
    }

    @Test
    public void testStringProcessInput() throws Exception {
        final ProcessInput processInput = new ProcessInput();
        processInput.data = (JsonNode) fromString("data", JsonNode.class);

        final String serialization = Serializator.stringProcessInput(processInput);
        final ProcessInput processInput2 = (ProcessInput) fromString(serialization, ProcessInput.class);
        final String serialization2 = intoString(processInput2);

        Assert.assertTrue(serialization != null);
        Assert.assertTrue(serialization.equals(serialization2));
    }

    @Test
    public void testProcessOutput() throws Exception {
        final ProcessOutput processOutput = new ProcessOutput();
        processOutput.rows = fromString("rows", JsonNode.class);

        final String serialization = intoString(processOutput);
        final ProcessOutput processOutput2 = Serializator.processOutput(serialization);
        final String serialization2 = intoString(processOutput2);

        Assert.assertTrue(serialization != null);
        Assert.assertTrue(serialization.equals(serialization2));
    }

    @Test
    public void testProcessResult() throws Exception {
        final ProcessResult processResult = new ProcessResult();
        processResult.result = fromString("result", JsonNode.class);

        final String serialization = intoString(processResult);
        final ProcessResult processResult2 = Serializator.processResult(serialization);
        final String serialization2 = intoString(processResult2);

        Assert.assertTrue(serialization != null);
        Assert.assertTrue(serialization.equals(serialization2));
    }
}