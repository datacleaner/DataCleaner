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

import com.fasterxml.jackson.databind.JsonNode;
import org.datacleaner.beans.transform.ConcatenatorTransformer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.InjectionManagerFactory;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.monitor.configuration.*;
import org.datacleaner.monitor.server.components.ComponentList;
import org.datacleaner.monitor.server.components.ProcessInput;
import org.datacleaner.monitor.server.components.ProcessStatelessInput;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static org.easymock.EasyMock.*;

public class ComponentsControllerV1Test {
    private String tenant = "demo";
    private String id = "component-id";
    private String componentName = "Concatenator";
    private String timeout = "42";
    private ComponentsControllerV1 componentsControllerV1 = new ComponentsControllerV1();

    @Before
    public void setUp() {
        componentsControllerV1._tenantContextFactory = getTenantContextFactoryMock();
    }

    private TenantContextFactory getTenantContextFactoryMock() {
        TenantContextFactory tenantContextFactory = createNiceMock(TenantContextFactory.class);
        expect(tenantContextFactory.getContext(tenant)).andReturn(getTenantContextMock()).anyTimes();
        replay(tenantContextFactory);

        return tenantContextFactory;
    }

    private TenantContext getTenantContextMock() {
        TenantContext tenantContext = createNiceMock(TenantContext.class);
        expect(tenantContext.getComponentsStore()).andReturn(getComponentsStoreMock()).anyTimes();
        expect(tenantContext.getConfiguration()).andReturn(getDCConfigurationMock()).anyTimes();
        replay(tenantContext);

        return tenantContext;
    }

    private ComponentsStore getComponentsStoreMock() {
        ComponentsStore componentsStore = createNiceMock(ComponentsStore.class);
        expect(componentsStore.getConfiguration(id)).andReturn(getComponentsStoreHolder()).anyTimes();
        replay(componentsStore);

        return componentsStore;
    }

    private ComponentsStoreHolder getComponentsStoreHolder() {
        CreateInput createInput = new CreateInput();
        createInput.configuration = getComponentConfigurationMock();
        long timeoutMs = 1000L;
        ComponentsStoreHolder componentsStoreHolder = new ComponentsStoreHolder(timeoutMs, createInput, id, componentName);

        return componentsStoreHolder;
    }

    private ComponentConfiguration getComponentConfigurationMock() {
        ComponentConfiguration componentConfiguration = createNiceMock(ComponentConfiguration.class);
        expect(componentConfiguration.getColumns()).andReturn(Collections.EMPTY_LIST).anyTimes();
        expect(componentConfiguration.getPropertiesNames()).andReturn(Collections.EMPTY_LIST).anyTimes();
        replay(componentConfiguration);

        return componentConfiguration;
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
        Set<TransformerDescriptor<?>> transformerDescriptorSet = new HashSet<>();
        TransformerDescriptor transformerDescriptorMock = getTransformerDescriptorMock();
        transformerDescriptorSet.add(transformerDescriptorMock);
        expect(descriptorProvider.getTransformerDescriptors()).andReturn(transformerDescriptorSet).anyTimes();
        expect(descriptorProvider.getTransformerDescriptorByDisplayName(componentName)).andReturn(transformerDescriptorMock).anyTimes();
        replay(descriptorProvider);

        return descriptorProvider;
    }

    private TransformerDescriptor getTransformerDescriptorMock() {
        TransformerDescriptor transformerDescriptor = createNiceMock(TransformerDescriptor.class);
        expect(transformerDescriptor.getDisplayName()).andReturn(componentName).anyTimes();
        expect(transformerDescriptor.getProvidedProperties()).andReturn(Collections.EMPTY_SET).anyTimes();
        expect(transformerDescriptor.getValidateMethods()).andReturn(Collections.EMPTY_SET).anyTimes();
        expect(transformerDescriptor.getInitializeMethods()).andReturn(Collections.EMPTY_SET).anyTimes();
        expect(transformerDescriptor.getCloseMethods()).andReturn(Collections.EMPTY_SET).anyTimes();
        expect(transformerDescriptor.getConfiguredProperties()).andReturn(Collections.EMPTY_SET).anyTimes();
        expect(transformerDescriptor.newInstance()).andReturn(new ConcatenatorTransformer()).anyTimes();
        replay(transformerDescriptor);

        return transformerDescriptor;
    }

    private JsonNode getJsonNodeMock() {
        JsonNode jsonNode = createNiceMock(JsonNode.class);
        Set set = new HashSet();
        expect(jsonNode.iterator()).andReturn(set.iterator()).anyTimes();
        replay(jsonNode);

        return jsonNode;
    }

    @Test
    public void testClose() throws Exception {
        componentsControllerV1.close();
    }

    @Test
    public void testGetAllComponents() throws Exception {
        ComponentList componentList = componentsControllerV1.getAllComponents(tenant);
        assertTrue(componentList.getComponents().size() > 0);
    }

    @Test
    public void testProcessStateless() throws Exception {
        ProcessStatelessInput processStatelessInput = new ProcessStatelessInput();
        processStatelessInput.configuration = new ComponentConfiguration();
        processStatelessInput.data = getJsonNodeMock();
        componentsControllerV1.processStateless(tenant, componentName, processStatelessInput);
    }

    @Test
    public void testCreateComponent() throws Exception {
        CreateInput createInput = new CreateInput();
        createInput.configuration = new ComponentConfiguration();
        componentsControllerV1.createComponent(tenant, componentName, timeout, createInput);
    }

    @Test
    public void testProcessComponent() throws Exception {
        ProcessInput processInput = new ProcessInput();
        processInput.data = getJsonNodeMock();

        componentsControllerV1.processComponent(tenant, id, processInput);
    }

    @Test
    public void testGetFinalResult() throws Exception {
        componentsControllerV1.getFinalResult(tenant, id);

    }

    @Test
    public void testDeleteComponent() throws Exception {
        componentsControllerV1.deleteComponent(tenant, id);
    }
}