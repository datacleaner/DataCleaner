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

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.monitor.server.controllers.ComponentControllerV1;
import org.datacleaner.restclient.ComponentList;
import org.datacleaner.restclient.ComponentList.ComponentInfo;
import org.easymock.IExpectationSetters;
import org.junit.Test;

public class ComponentListTest {
    private static final int COMPONENTS_COUNT = 5;
    private static final String tenant = "demo";
    private ComponentList componentList = new ComponentList();
    private ComponentDescriptor componentDescriptorMock = null;
    private ConfiguredPropertyDescriptor configuredPropertyDescriptorMock = null;

    @Test
    public void testUrl() {
        try {
            String encoding = "UTF-8";
            String url = "a/b.net";
            String encoded = URLEncoder.encode(url, encoding);
            assertFalse(encoded.contains("/"));
            String decoded = URLDecoder.decode(encoded, encoding);
            assertTrue(decoded.equals(url));
        }
        catch (UnsupportedEncodingException e) {
            fail();
        }
    }

    @Test
    public void testAdd() throws Exception {
        assertTrue(componentList.getComponents().isEmpty());
        ComponentDescriptor descriptorMock = getDescriptorMock();

        componentList.add(ComponentControllerV1.createComponentInfo(tenant, descriptorMock));
        assertTrue(componentList.getComponents().size() == 1);

        verify(configuredPropertyDescriptorMock);
        verify(componentDescriptorMock);
    }

    private ComponentDescriptor getDescriptorMock() {
        componentDescriptorMock = createNiceMock(ComponentDescriptor.class);
        expect(componentDescriptorMock.getConfiguredProperties()).andReturn(getConfiguredPropertiesMock()).anyTimes();
        expect(componentDescriptorMock.getDisplayName()).andReturn("descriptor display name").anyTimes();
        expect(componentDescriptorMock.getComponentSuperCategory()).andReturn(getComponentSuperCategoryMock()).anyTimes();
        expect(componentDescriptorMock.getComponentCategories()).andReturn(Collections.EMPTY_SET).anyTimes();
        replay(componentDescriptorMock);

        return componentDescriptorMock;
    }

    private ComponentSuperCategory getComponentSuperCategoryMock() {
        ComponentSuperCategory componentSuperCategory = new ComponentSuperCategory() {
            @Override
            public String getName() {
                return "superCategory";
            }

            @Override
            public String getDescription() {
                return getName();
            }

            @Override
            public int getSortIndex() {
                return 0;
            }

            @Override
            public int compareTo(ComponentSuperCategory o) {
                return 0;
            }
        };

        return componentSuperCategory;
    }

    private Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesMock() {
        configuredPropertyDescriptorMock = createNiceMock(ConfiguredPropertyDescriptor.class);
        expect(configuredPropertyDescriptorMock.getName()).andReturn("propertyName").anyTimes();
        expect(configuredPropertyDescriptorMock.isInputColumn()).andReturn(true).anyTimes();
        expect(configuredPropertyDescriptorMock.getDescription()).andReturn("property description").anyTimes();
        expect(configuredPropertyDescriptorMock.isRequired()).andReturn(true).anyTimes();
        Class baseType = String.class;
        expect(configuredPropertyDescriptorMock.getBaseType()).andReturn(baseType).anyTimes();
        IExpectationSetters getTypeExpectation = expect(configuredPropertyDescriptorMock.getType());
        getTypeExpectation.andReturn(String.class).anyTimes();

        Set<ConfiguredPropertyDescriptor> propertiesSet = new HashSet<>();
        propertiesSet.add(configuredPropertyDescriptorMock);
        replay(configuredPropertyDescriptorMock);

        return propertiesSet;
    }

    @Test
    public void testGetComponents() throws Exception {
        assertTrue(componentList.getComponents().isEmpty());
        componentList.getComponents().addAll(getComponentList());
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
        Map<String, ComponentList.PropertyInfo> props = new HashMap<>();
        ComponentList.PropertyInfo prop = new ComponentList.PropertyInfo();
        prop.setName("propertyName" + id);
        prop.setDescription("propertyDescription" + id);
        prop.setRequired(true);
        props.put(prop.getName(), prop);
        componentInfo.setProperties(props);
        return componentInfo;
    }
}