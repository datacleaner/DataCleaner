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
package org.datacleaner.monitor.server.components;

import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.monitor.server.components.ComponentList.ComponentInfo;
import org.junit.Test;

import java.util.*;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertTrue;

public class ComponentListTest {
    private static final int COMPONENTS_COUNT = 5;
    private static final String tenant = "demo";
    private ComponentList componentList = new ComponentList();
    private ComponentDescriptor componentDescriptorMock = null;
    private ConfiguredPropertyDescriptor configuredPropertyDescriptorMock = null;

    @Test
    public void testAdd() throws Exception {
        assertTrue(componentList.getComponents().isEmpty());
        ComponentDescriptor descriptorMock = getDescriptorMock();

        replay(configuredPropertyDescriptorMock);
        replay(componentDescriptorMock);

        componentList.add(tenant, descriptorMock);
        assertTrue(componentList.getComponents().size() == 1);

        verify(configuredPropertyDescriptorMock);
        verify(componentDescriptorMock);
    }

    private ComponentDescriptor getDescriptorMock() {
        componentDescriptorMock = createNiceMock(ComponentDescriptor.class);
        expect(componentDescriptorMock.getConfiguredProperties()).andReturn(getConfiguredPropertiesMock());
        expect(componentDescriptorMock.getDisplayName()).andReturn("descriptor display name").anyTimes();

        return componentDescriptorMock;
    }

    private Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesMock() {
        configuredPropertyDescriptorMock = createNiceMock(ConfiguredPropertyDescriptor.class);
        expect(configuredPropertyDescriptorMock.getName()).andReturn("propertyName").anyTimes();
        expect(configuredPropertyDescriptorMock.isInputColumn()).andReturn(true).anyTimes();
        expect(configuredPropertyDescriptorMock.getDescription()).andReturn("property description").anyTimes();
        expect(configuredPropertyDescriptorMock.isRequired()).andReturn(true).anyTimes();

        Set<ConfiguredPropertyDescriptor> propertiesSet = new HashSet<>();
        propertiesSet.add(configuredPropertyDescriptorMock);

        return propertiesSet;
    }

    @Test
    public void testGetComponents() throws Exception {
        assertTrue(componentList.getComponents().isEmpty());
        componentList.setComponents(getComponentList());
        assertTrue(componentList.getComponents().size() == ComponentListTest.COMPONENTS_COUNT);
    }

    private List<ComponentInfo> getComponentList() {
        List<ComponentInfo> componentInfoList = new ArrayList<>();

        for (int i = 0; i < ComponentListTest.COMPONENTS_COUNT; i++) {
            componentInfoList.add(getComponentInfo(i));
        }

        return componentInfoList;
    }

    private ComponentInfo getComponentInfo(int id) {
        ComponentInfo componentInfo = new ComponentInfo();
        componentInfo.setName("name" + id);
        componentInfo.setDescription("description of " + id);
        componentInfo.setCreateURL("create URL" + id);
        String[][] properties = { { "propertyName" + id, "propertyDescription" + id, "required" } };
        componentInfo.setPropertyList(Arrays.asList(properties));

        return componentInfo;
    }
}