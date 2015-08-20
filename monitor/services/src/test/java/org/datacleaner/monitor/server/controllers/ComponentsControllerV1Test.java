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
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.InjectionManagerFactory;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.monitor.configuration.*;
import org.datacleaner.monitor.server.components.ComponentList;
import org.datacleaner.monitor.server.components.ComponentNotFoundException;
import org.datacleaner.monitor.server.components.ProcessInput;
import org.datacleaner.monitor.server.components.ProcessStatelessInput;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static junit.framework.TestCase.assertTrue;
import static org.easymock.EasyMock.*;

public class ComponentsControllerV1Test {
    private String tenant = "demo";
    private String id = "component-id";
    private String componentName = "Generate ID";
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
        expect(componentsStore.getConfiguration(id)).andReturn(null).anyTimes();
        replay(componentsStore);

        return componentsStore;
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
        expect(descriptorProvider.getTransformerDescriptors()).andReturn(Collections.EMPTY_SET).anyTimes();
        expect(descriptorProvider.getTransformerDescriptorByDisplayName(componentName)).andReturn(getTransformerDescriptorMock()).anyTimes();
        replay(descriptorProvider);

        return descriptorProvider;
    }

    private TransformerDescriptor getTransformerDescriptorMock() {
        TransformerDescriptor transformerDescriptor = createNiceMock(TransformerDescriptor.class);
        expect(transformerDescriptor.getDisplayName()).andReturn(componentName).anyTimes();
        expect(transformerDescriptor.getProvidedProperties()).andReturn(Collections.EMPTY_SET).anyTimes();
        expect(transformerDescriptor.getValidateMethods()).andReturn(Collections.EMPTY_SET).anyTimes();
        expect(transformerDescriptor.getInitializeMethods()).andReturn(Collections.EMPTY_SET).anyTimes();
        replay(transformerDescriptor);

        return transformerDescriptor;
    }

    @Test
    public void testClose() throws Exception {
        componentsControllerV1.close();
    }

    @Test
    public void testGetAllComponents() throws Exception {
        ComponentList componentList = componentsControllerV1.getAllComponents(tenant);
        assertTrue(componentList.getComponents().isEmpty());
    }

    @Test(expected = ComponentNotFoundException.class)
    public void testProcessStateless() throws Exception {
        ProcessStatelessInput processStatelessInput = new ProcessStatelessInput();
        processStatelessInput.configuration = new ComponentConfiguration();
        componentsControllerV1.processStateless(tenant, id, processStatelessInput);
    }

    @Test
    public void testCreateComponent() throws Exception {
        CreateInput createInput = new CreateInput();
        createInput.configuration = new ComponentConfiguration();
        componentsControllerV1.createComponent(tenant, componentName, timeout, createInput);
    }

    @Test(expected = ComponentNotFoundException.class)
    public void testProcessComponent() throws Exception {
        ProcessInput processInput = new ProcessInput();
        processInput.data = createNiceMock(JsonNode.class);

        componentsControllerV1.processComponent(tenant, id, processInput);
    }

    @Test
    public void testGetFinalResult() throws Exception {
        componentsControllerV1.getFinalResult(tenant, id);

    }

    @Test(expected = ComponentNotFoundException.class)
    public void testDeleteComponent() throws Exception {
        componentsControllerV1.deleteComponent(tenant, id);
    }
}