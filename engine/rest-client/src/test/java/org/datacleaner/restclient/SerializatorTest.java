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
import java.util.Collections;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerializatorTest {
    private static final Logger logger = LoggerFactory.getLogger(Serializator.class);
    private final String tenantName = "demo";
    private final String componentName = "Concatenator";
    private final String componentDescription = "Concatenator description";
    private static ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testComponentList() throws Exception {
        ComponentList componentList = new ComponentList();
        componentList.add(tenantName, getComponentDescriptorMock());

        String serialization = intoString(componentList);
        ComponentList componentList2 = Serializator.componentList(serialization);
        String serialization2 = intoString(componentList2);

        Assert.assertTrue(serialization != null);
        Assert.assertTrue(serialization.equals(serialization2));
    }

    private ComponentDescriptor getComponentDescriptorMock() {
        ComponentDescriptor componentDescriptor = EasyMock.createNiceMock(ComponentDescriptor.class);
        EasyMock.expect(componentDescriptor.getDisplayName()).andReturn(componentName).anyTimes();
        EasyMock.expect(componentDescriptor.getDescription()).andReturn(componentDescription).anyTimes();
        EasyMock.expect(componentDescriptor.getConfiguredProperties()).andReturn(Collections.EMPTY_SET).anyTimes();
        EasyMock.replay(componentDescriptor);

        return componentDescriptor;
    }

    private String intoString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
            return "";
        }
    }

    private Object fromString(String value, Class type) {
        try {
            return objectMapper.readValue(value, type);
        }
        catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    @Test
    public void testComponentInfo() throws Exception {
        ComponentList.ComponentInfo componentInfo = new ComponentList.ComponentInfo();
        componentInfo.setName(componentName);
        componentInfo.setDescription(componentDescription);

        String serialization = intoString(componentInfo);
        ComponentList.ComponentInfo componentInfo2 = Serializator.componentInfo(serialization);
        String serialization2 = intoString(componentInfo2);

        Assert.assertTrue(serialization != null);
        Assert.assertTrue(serialization.equals(serialization2));
    }

    @Test
    public void testStringProcessStatelessInput() throws Exception {
        ProcessStatelessInput processStatelessInput = new ProcessStatelessInput();
        ComponentConfiguration componentConfiguration = new ComponentConfiguration();
        JsonNode property = (JsonNode) fromString("propertyValue", JsonNode.class);
        componentConfiguration.getProperties().put("propertyKey", property);
        processStatelessInput.data = (JsonNode) fromString("data", JsonNode.class);

        String serialization = intoString(processStatelessInput);
        String serialization2 = Serializator.stringProcessStatelessInput(processStatelessInput);

        Assert.assertTrue(serialization != null);
        Assert.assertTrue(serialization.equals(serialization2));
    }

    @Test
    public void testProcessStatelessOutput() throws Exception {
        /* // mytodo
        ProcessStatelessOutput processStatelessOutput = new ProcessStatelessOutput();
        processStatelessOutput.result = "result";
        processStatelessOutput.rows = "rows";

        String serialization = intoString(processStatelessOutput);
        ProcessStatelessOutput processStatelessOutput2 = Serializator.processStatelessOutput(serialization);
        String serialization2 = intoString(processStatelessOutput2);

        Assert.assertTrue(serialization != null);
        Assert.assertTrue(serialization.equals(serialization2));
        */
    }

    @Test
    public void testStringCreateInput() throws Exception {

    }

    @Test
    public void testStringProcessInput() throws Exception {

    }

    @Test
    public void testProcessOutput() throws Exception {

    }

    @Test
    public void testProcessResult() throws Exception {

    }
}