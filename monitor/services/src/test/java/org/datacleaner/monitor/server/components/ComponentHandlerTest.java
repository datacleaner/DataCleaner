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

import static org.easymock.EasyMock.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.datacleaner.beans.transform.ConcatenatorTransformer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.InjectionManagerFactory;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.monitor.configuration.SimpleRemoteComponentsConfigurationImpl;
import org.datacleaner.restclient.ComponentConfiguration;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;


public class ComponentHandlerTest {
    private ComponentHandler componentHandler = null;
    private String componentName = "Hello world transformer";
    private ComponentConfiguration componentConfiguration = null;
    private JsonNode jsonData = null;

    @Before
    public void setUp() {
        componentConfiguration = getComponentConfiguration();
        final DataCleanerConfiguration dcConfigMock = getDCConfigurationMock();
        componentHandler = new ComponentHandler(dcConfigMock, dcConfigMock.getEnvironment().getDescriptorProvider()
                .getTransformerDescriptorByDisplayName(componentName), componentConfiguration,
                new SimpleRemoteComponentsConfigurationImpl(), null);
        jsonData = getJsonDataMock();
    }

    private DataCleanerConfiguration getDCConfigurationMock() {
        final DataCleanerConfiguration dataCleanerConfiguration = createNiceMock(DataCleanerConfiguration.class);
        expect(dataCleanerConfiguration.getEnvironment()).andReturn(getEnvironmentMock()).anyTimes();
        expect(dataCleanerConfiguration.getHomeFolder()).andReturn(DataCleanerConfigurationImpl.defaultHomeFolder())
                .anyTimes();
        replay(dataCleanerConfiguration);

        return dataCleanerConfiguration;
    }

    private DataCleanerEnvironment getEnvironmentMock() {
        final DataCleanerEnvironment dataCleanerEnvironment = createNiceMock(DataCleanerEnvironment.class);
        expect(dataCleanerEnvironment.getDescriptorProvider()).andReturn(getDescriptorProviderMock()).anyTimes();
        expect(dataCleanerEnvironment.getInjectionManagerFactory()).andReturn(getInjectionManagerFactoryMock())
                .anyTimes();
        replay(dataCleanerEnvironment);

        return dataCleanerEnvironment;
    }

    private InjectionManagerFactory getInjectionManagerFactoryMock() {
        final InjectionManagerFactory injectionManagerFactory = createNiceMock(InjectionManagerFactory.class);
        expect(injectionManagerFactory.getInjectionManager(null)).andReturn(null).anyTimes();
        replay(injectionManagerFactory);

        return injectionManagerFactory;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private DescriptorProvider getDescriptorProviderMock() {
        final DescriptorProvider descriptorProvider = createNiceMock(DescriptorProvider.class);
        final TransformerDescriptor mock = getTransformerDescriptorMock();
        expect(descriptorProvider.getTransformerDescriptorByDisplayName(componentName)).andReturn(mock).anyTimes();
        replay(descriptorProvider);

        return descriptorProvider;
    }

    private TransformerDescriptor<?> getTransformerDescriptorMock() {
        @SuppressWarnings("unchecked") final TransformerDescriptor<ConcatenatorTransformer> transformerDescriptor =
                createNiceMock(TransformerDescriptor.class);
        expect(transformerDescriptor.getConfiguredProperties()).andReturn(getConfiguredPropertiesMock()).anyTimes();
        expect(transformerDescriptor.getDisplayName()).andReturn("descriptor display name").anyTimes();
        expect(transformerDescriptor.getProvidedProperties()).andReturn(Collections.emptySet()).anyTimes();
        expect(transformerDescriptor.getValidateMethods()).andReturn(Collections.emptySet()).anyTimes();
        expect(transformerDescriptor.getInitializeMethods()).andReturn(Collections.emptySet()).anyTimes();
        expect(transformerDescriptor.getCloseMethods()).andReturn(Collections.emptySet()).anyTimes();
        expect(transformerDescriptor.newInstance()).andReturn(new ConcatenatorTransformer()).anyTimes();
        replay(transformerDescriptor);

        return transformerDescriptor;
    }

    private Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesMock() {
        final ConfiguredPropertyDescriptor configuredPropertyDescriptorMock =
                createNiceMock(ConfiguredPropertyDescriptor.class);
        expect(configuredPropertyDescriptorMock.getName()).andReturn("propertyName").anyTimes();
        expect(configuredPropertyDescriptorMock.isInputColumn()).andReturn(true).anyTimes();
        expect(configuredPropertyDescriptorMock.getDescription()).andReturn("property description").anyTimes();
        expect(configuredPropertyDescriptorMock.isRequired()).andReturn(true).anyTimes();
        replay(configuredPropertyDescriptorMock);

        final Set<ConfiguredPropertyDescriptor> propertiesSet = new HashSet<>();
        propertiesSet.add(configuredPropertyDescriptorMock);

        return propertiesSet;
    }

    private JsonNode getJsonDataMock() {
        final JsonNode jsonNode = createNiceMock(JsonNode.class);
        final Set<JsonNode> set = new HashSet<>();
        expect(jsonNode.iterator()).andReturn(set.iterator()).anyTimes();
        replay(jsonNode);

        return jsonNode;
    }

    private ComponentConfiguration getComponentConfiguration() {
        return new ComponentConfiguration();
    }

    @Test
    public void testComponent() throws Exception {
        componentHandler.runComponent(jsonData, Integer.MAX_VALUE);
        componentHandler.closeComponent();
    }
}
