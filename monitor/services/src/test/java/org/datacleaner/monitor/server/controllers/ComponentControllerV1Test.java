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
package org.datacleaner.monitor.server.controllers;

import static junit.framework.TestCase.assertTrue;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.beans.transform.ConcatenatorTransformer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.InjectionManagerFactory;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.monitor.configuration.ComponentStore;
import org.datacleaner.monitor.configuration.ComponentStoreHolder;
import org.datacleaner.monitor.configuration.RemoteComponentsConfigurationImpl;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.shared.ComponentNotFoundException;
import org.datacleaner.restclient.ComponentConfiguration;
import org.datacleaner.restclient.ComponentList;
import org.datacleaner.restclient.CreateInput;
import org.datacleaner.restclient.ProcessInput;
import org.datacleaner.restclient.ProcessStatelessInput;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

public class ComponentControllerV1Test {
    private String tenant = "demo";
    private String id = "component-id";
    private String componentName = "Concatenator";
    private String timeout = "42";
    private ComponentControllerV1 componentControllerV1 = new ComponentControllerV1();

    @Before
    public void setUp() {
        componentControllerV1._tenantContextFactory = getTenantContextFactoryMock();
        componentControllerV1._remoteComponentsConfiguration = new RemoteComponentsConfigurationImpl();
        componentControllerV1.init();
    }

    private TenantContextFactory getTenantContextFactoryMock() {
        TenantContextFactory tenantContextFactory = createNiceMock(TenantContextFactory.class);
        expect(tenantContextFactory.getContext(tenant)).andReturn(getTenantContextMock()).anyTimes();
        replay(tenantContextFactory);

        return tenantContextFactory;
    }

    private TenantContext getTenantContextMock() {
        TenantContext tenantContext = createNiceMock(TenantContext.class);
        expect(tenantContext.getComponentStore()).andReturn(getComponentsStoreMock()).anyTimes();
        expect(tenantContext.getConfiguration()).andReturn(getDCConfigurationMock()).anyTimes();
        replay(tenantContext);

        return tenantContext;
    }

    private ComponentStore getComponentsStoreMock() {
        ComponentStore componentStore = createNiceMock(ComponentStore.class);
        expect(componentStore.get(id)).andReturn(getComponentsStoreHolder()).anyTimes();
        replay(componentStore);

        return componentStore;
    }

    private ComponentStoreHolder getComponentsStoreHolder() {
        CreateInput createInput = new CreateInput();
        createInput.configuration = getComponentConfigurationMock();
        long timeoutMs = 1000L;
        ComponentStoreHolder componentStoreHolder = new ComponentStoreHolder(timeoutMs, createInput, id, componentName);

        return componentStoreHolder;
    }

    private ComponentConfiguration getComponentConfigurationMock() {
        ComponentConfiguration componentConfiguration = createNiceMock(ComponentConfiguration.class);
        expect(componentConfiguration.getColumns()).andReturn(Collections.<JsonNode> emptyList()).anyTimes();
        expect(componentConfiguration.getProperties()).andReturn(Collections.<String, JsonNode> emptyMap()).anyTimes();
        replay(componentConfiguration);

        return componentConfiguration;
    }

    private DataCleanerConfiguration getDCConfigurationMock() {
        DataCleanerConfiguration dataCleanerConfiguration = createNiceMock(DataCleanerConfiguration.class);
        expect(dataCleanerConfiguration.getEnvironment()).andReturn(getEnvironmentMock()).anyTimes();
        expect(dataCleanerConfiguration.getHomeFolder()).andReturn(DataCleanerConfigurationImpl.defaultHomeFolder())
                .anyTimes();
        replay(dataCleanerConfiguration);

        return dataCleanerConfiguration;
    }

    private DataCleanerEnvironment getEnvironmentMock() {
        DataCleanerEnvironment dataCleanerEnvironment = createNiceMock(DataCleanerEnvironment.class);
        expect(dataCleanerEnvironment.getDescriptorProvider()).andReturn(getDescriptorProviderMock()).anyTimes();
        expect(dataCleanerEnvironment.getInjectionManagerFactory()).andReturn(getInjectionManagerFactoryMock())
                .anyTimes();
        replay(dataCleanerEnvironment);

        return dataCleanerEnvironment;
    }

    private InjectionManagerFactory getInjectionManagerFactoryMock() {
        InjectionManagerFactory injectionManagerFactory = createNiceMock(InjectionManagerFactory.class);
        expect(injectionManagerFactory.getInjectionManager(null)).andReturn(null).anyTimes();
        replay(injectionManagerFactory);

        return injectionManagerFactory;
    }

    @SuppressWarnings("unchecked")
    private DescriptorProvider getDescriptorProviderMock() {
        DescriptorProvider descriptorProvider = createNiceMock(DescriptorProvider.class);
        Set<TransformerDescriptor<?>> transformerDescriptorSet = new HashSet<>();
        @SuppressWarnings("rawtypes")
        TransformerDescriptor transformerDescriptorMock = getTransformerDescriptorMock();
        transformerDescriptorSet.add(transformerDescriptorMock);
        expect(descriptorProvider.getTransformerDescriptors()).andReturn(transformerDescriptorSet).anyTimes();
        expect(descriptorProvider.getTransformerDescriptorByDisplayName(componentName)).andReturn(
                transformerDescriptorMock).anyTimes();
        replay(descriptorProvider);

        return descriptorProvider;
    }

    @SuppressWarnings("rawtypes")
    private TransformerDescriptor<?> getTransformerDescriptorMock() {
        TransformerDescriptor transformerDescriptor = createNiceMock(TransformerDescriptor.class);
        expect(transformerDescriptor.getDisplayName()).andReturn(componentName).anyTimes();
        expect(transformerDescriptor.getProvidedProperties()).andReturn(Collections.EMPTY_SET).anyTimes();
        expect(transformerDescriptor.getValidateMethods()).andReturn(Collections.EMPTY_SET).anyTimes();
        expect(transformerDescriptor.getInitializeMethods()).andReturn(Collections.EMPTY_SET).anyTimes();
        expect(transformerDescriptor.getCloseMethods()).andReturn(Collections.EMPTY_SET).anyTimes();
        expect(transformerDescriptor.getConfiguredProperties()).andReturn(Collections.EMPTY_SET).anyTimes();
        expect(transformerDescriptor.newInstance()).andReturn(new ConcatenatorTransformer()).anyTimes();
        expect(transformerDescriptor.getComponentSuperCategory()).andReturn(getComponentSuperCategoryMock()).anyTimes();
        expect(transformerDescriptor.getComponentCategories()).andReturn(Collections.EMPTY_SET).anyTimes();
        replay(transformerDescriptor);

        return transformerDescriptor;
    }

    private ComponentSuperCategory getComponentSuperCategoryMock() {
        ComponentSuperCategory componentSuperCategory = new ComponentSuperCategory() {

            private static final long serialVersionUID = 1L;

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

    private JsonNode getJsonNodeMock() {
        JsonNode jsonNode = createNiceMock(JsonNode.class);
        Set<JsonNode> set = new HashSet<>();
        expect(jsonNode.iterator()).andReturn(set.iterator()).anyTimes();
        replay(jsonNode);

        return jsonNode;
    }

    @Test
    public void testClose() throws Exception {
        componentControllerV1.close();
    }

    @Test
    public void testGetAllComponents() throws Exception {
        ComponentList componentList = componentControllerV1.getAllComponents(tenant, false);
        assertTrue(componentList.getComponents().size() > 0);
    }

    @Test
    public void testProcessStateless() throws Exception {
        ProcessStatelessInput processStatelessInput = new ProcessStatelessInput();
        processStatelessInput.configuration = new ComponentConfiguration();
        processStatelessInput.data = getJsonNodeMock();
        componentControllerV1.processStateless(tenant, componentName, processStatelessInput);
    }

    @Test
    public void testCreateComponent() throws Exception {
        CreateInput createInput = new CreateInput();
        createInput.configuration = new ComponentConfiguration();
        componentControllerV1.createComponent(tenant, componentName, timeout, createInput);
    }

    @Test
    public void testProcessComponent() throws Exception {
        ProcessInput processInput = new ProcessInput();
        processInput.data = getJsonNodeMock();

        componentControllerV1.processComponent(tenant, id, processInput);
    }

    @Test
    public void testGetFinalResult() throws Exception {
        componentControllerV1.getFinalResult(tenant, id);

    }

    @Test(expected = ComponentNotFoundException.class)
    public void testDeleteComponent() throws Exception {
        componentControllerV1.deleteComponent(tenant, id);
    }
}