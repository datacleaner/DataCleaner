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

import com.fasterxml.jackson.databind.JsonNode;
import org.datacleaner.api.WSStatelessComponent;
import org.datacleaner.beans.transform.ConcatenatorTransformer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.InjectionManagerFactory;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.monitor.configuration.ComponentConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.easymock.EasyMock.*;

public class ComponentHandlerTest {
    private ComponentHandler componentHandler = null;
    private String componentName = "Hello world transformer";
    private ComponentConfiguration componentConfiguration = null;
    private JsonNode jsonData = null;

    @Before
    public void setUp() {
        componentHandler = new ComponentHandler(getDCConfigurationMock(), componentName);
        componentConfiguration = getComponentConfiguration();
        jsonData = getJsonDataMock();
    }

    private DataCleanerConfiguration getDCConfigurationMock() {
        DataCleanerConfiguration dataCleanerConfiguration = createNiceMock(DataCleanerConfiguration.class);
        expect(dataCleanerConfiguration.getEnvironment()).andReturn(getEnvironmentMock()).anyTimes();
        replay(dataCleanerConfiguration);

        return dataCleanerConfiguration;
    }

    private DataCleanerEnvironment getEnvironmentMock() {
        DataCleanerEnvironment dataCleanerEnvironment = createNiceMock(DataCleanerEnvironment.class);
        expect(dataCleanerEnvironment.getDescriptorProvider()).andReturn(getDescriptorProviderMock()).anyTimes();
        expect(dataCleanerEnvironment.getInjectionManagerFactory()).andReturn(getInjectionManagerFactoryMock()).anyTimes();
        replay(dataCleanerEnvironment);

        return dataCleanerEnvironment;
    }

    private InjectionManagerFactory getInjectionManagerFactoryMock() {
        InjectionManagerFactory injectionManagerFactory = createNiceMock(InjectionManagerFactory.class);
        expect(injectionManagerFactory.getInjectionManager(null)).andReturn(null).anyTimes();
        replay(injectionManagerFactory);

        return injectionManagerFactory;
    }


    private DescriptorProvider getDescriptorProviderMock() {
        DescriptorProvider descriptorProvider = createNiceMock(DescriptorProvider.class);
        expect(descriptorProvider.getTransformerDescriptorByDisplayName(componentName)).andReturn(getTransformerDescriptorMock()).anyTimes();
        replay(descriptorProvider);

        return descriptorProvider;
    }

    private TransformerDescriptor getTransformerDescriptorMock() {
        TransformerDescriptor transformerDescriptor = createNiceMock(TransformerDescriptor.class);
        expect(transformerDescriptor.getConfiguredProperties()).andReturn(getConfiguredPropertiesMock()).anyTimes();
        expect(transformerDescriptor.getDisplayName()).andReturn("descriptor display name").anyTimes();
        expect(transformerDescriptor.getProvidedProperties()).andReturn(Collections.emptySet()).anyTimes();
        expect(transformerDescriptor.getValidateMethods()).andReturn(Collections.emptySet()).anyTimes();
        expect(transformerDescriptor.getInitializeMethods()).andReturn(Collections.emptySet()).anyTimes();
        expect(transformerDescriptor.getCloseMethods()).andReturn(Collections.emptySet()).anyTimes();
        expect(transformerDescriptor.newInstance()).andReturn(new ConcatenatorTransformer()).anyTimes();
        Annotation statelessAnnotation = createNiceMock(WSStatelessComponent.class);
        expect(transformerDescriptor.getAnnotation(WSStatelessComponent.class)).andReturn(statelessAnnotation).anyTimes();
        replay(transformerDescriptor);

        return transformerDescriptor;
    }

    private Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesMock() {
        ConfiguredPropertyDescriptor configuredPropertyDescriptorMock = createNiceMock(ConfiguredPropertyDescriptor.class);
        expect(configuredPropertyDescriptorMock.getName()).andReturn("propertyName").anyTimes();
        expect(configuredPropertyDescriptorMock.isInputColumn()).andReturn(true).anyTimes();
        expect(configuredPropertyDescriptorMock.getDescription()).andReturn("property description").anyTimes();
        expect(configuredPropertyDescriptorMock.isRequired()).andReturn(true).anyTimes();
        replay(configuredPropertyDescriptorMock);

        Set<ConfiguredPropertyDescriptor> propertiesSet = new HashSet<>();
        propertiesSet.add(configuredPropertyDescriptorMock);

        return propertiesSet;
    }

    private JsonNode getJsonDataMock() {
        JsonNode jsonNode = createNiceMock(JsonNode.class);
        Set<JsonNode> set = new HashSet<>();
        expect(jsonNode.iterator()).andReturn(set.iterator()).anyTimes();
        replay(jsonNode);

        return jsonNode;
    }


    private ComponentConfiguration getComponentConfiguration() {
        return new ComponentConfiguration();
    }

    @Test
    public void testComponent() throws Exception {
        componentHandler.createComponent(componentConfiguration);
        componentHandler.runComponent(jsonData);
        componentHandler.closeComponent();
    }
}