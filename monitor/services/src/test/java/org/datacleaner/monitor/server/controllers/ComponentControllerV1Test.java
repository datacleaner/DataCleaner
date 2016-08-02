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

import org.datacleaner.beans.transform.ConcatenatorTransformer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.InjectionManagerFactory;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.SimpleDescriptorProvider;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.job.concurrent.SingleThreadedTaskRunner;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.monitor.configuration.ComponentStore;
import org.datacleaner.monitor.configuration.ComponentStoreHolder;
import org.datacleaner.monitor.configuration.RemoteComponentsConfiguration;
import org.datacleaner.monitor.configuration.SimpleRemoteComponentsConfigurationImpl;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.server.components.ComponentCache;
import org.datacleaner.monitor.server.components.ComponentHandlerFactory;
import org.datacleaner.monitor.shared.ComponentNotFoundException;
import org.datacleaner.restclient.ComponentConfiguration;
import org.datacleaner.restclient.ComponentList;
import org.datacleaner.restclient.CreateInput;
import org.datacleaner.restclient.ProcessInput;
import org.datacleaner.restclient.ProcessStatelessInput;
import org.datacleaner.restclient.ProcessStatelessOutput;
import org.datacleaner.restclient.Serializator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.easymock.EasyMock.*;

public class ComponentControllerV1Test {
    private String tenant = "demo";
    private String id = "component-id";
    private String componentName = "Concatenator";
    private String timeout = "42";
    private ComponentControllerV1 componentControllerV1 = new ComponentControllerV1();
    private ComponentCache componentCache;
    private TaskRunner taskRunner;

    @Before
    public void setUp() {
        taskRunner = new SingleThreadedTaskRunner();
        RemoteComponentsConfiguration remoteCfg = new SimpleRemoteComponentsConfigurationImpl();
        ComponentHandlerFactory compHandlerFac = new ComponentHandlerFactory(remoteCfg);
        TenantContextFactory tenantCtxFac = getTenantContextFactoryMock();
        componentCache = new ComponentCache(compHandlerFac, tenantCtxFac);
        componentControllerV1._tenantContextFactory = tenantCtxFac;
        componentControllerV1._remoteComponentsConfiguration = remoteCfg;
        componentControllerV1.componentHandlerFactory = compHandlerFac;
        componentControllerV1._componentCache = componentCache;
    }

    @After
    public void tearDown() throws InterruptedException {
        componentCache.close();
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
        ProcessStatelessInput input = createSampleInput();
        createInput.configuration =  input.configuration;
        long timeoutMs = 1000L;
        ComponentStoreHolder componentStoreHolder = new ComponentStoreHolder(timeoutMs, createInput, id, componentName);

        return componentStoreHolder;
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
        expect(dataCleanerEnvironment.getTaskRunner()).andReturn(taskRunner).anyTimes();
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
        final SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider(false);
        final TransformerDescriptor<?> transformerDescriptor = Descriptors.ofTransformer(ConcatenatorTransformer.class);
        descriptorProvider.addTransformerBeanDescriptor(transformerDescriptor);
        return descriptorProvider;
    }

    private ProcessStatelessInput createSampleInput() {
        JsonNodeFactory json = Serializator.getJacksonObjectMapper().getNodeFactory();
        ProcessStatelessInput input = new ProcessStatelessInput();
        input.configuration = new ComponentConfiguration();
        ArrayNode cols = json.arrayNode().add("c1").add("c2");
        input.configuration.getProperties().put("Columns", cols);
        input.configuration.getColumns().add(json.textNode("c1"));
        input.configuration.getColumns().add(json.textNode("c2"));

        input.data = json.arrayNode();
        ArrayNode row = json.arrayNode();
        row.add(json.textNode("Hello"));
        row.add(json.textNode("World"));
        ((ArrayNode)input.data).add(row);
        return input;
    }

    @Test
    public void testGetAllComponents() throws Exception {
        ComponentList componentList = componentControllerV1.getAllComponents("1.0", tenant, false);
        assertTrue(componentList.getComponents().size() > 0);
        assertNull(componentList.getComponents().get(0).isEnabled());

        ComponentList componentList2 = componentControllerV1.getAllComponents("55.0.0", tenant, false);
        assertTrue(componentList2.getComponents().size() > 0);
        assertNotNull(componentList2.getComponents().get(0).isEnabled());
    }

    @Test
    public void testProcessStateless() throws Exception {
        ProcessStatelessInput input = createSampleInput();
        ProcessStatelessOutput output = componentControllerV1.processStateless(tenant, componentName, null, false, input);
        JsonNode rows = output.rows;
        Assert.assertEquals("Output should have one row group", 1, rows.size());
        Assert.assertEquals("Output should have one row", 1, rows.get(0).size());
        JsonNode row1 = rows.get(0).get(0);
        Assert.assertEquals("Wrong output value", "HelloWorld", row1.get(0).asText());
    }

    @Test
    public void testProcessStatelessOutputFormatColumnMap() throws Exception {

        ProcessStatelessInput input = createSampleInput();

        ProcessStatelessOutput output = componentControllerV1.processStateless(tenant, componentName, "map", false, input);
        JsonNode rows = output.rows;
        Assert.assertEquals("Output should have one row group", 1, rows.size());
        Assert.assertEquals("Output should have one row", 1, rows.get(0).size());
        JsonNode row1 = rows.get(0).get(0);
        JsonNode value = row1.get("Concat of c1,c2");
        Assert.assertNotNull("Output column 'Concat of c1,c2' doesn't exist", value);
        Assert.assertEquals("Output column 'Concat of c1,c2' has wrong value", "HelloWorld", value.asText());
    }

    @Test
    public void testCreateComponent() throws Exception {
        CreateInput createInput = new CreateInput();
        createInput.configuration = new ComponentConfiguration();
        componentControllerV1.createComponent(tenant, componentName, timeout, createInput);
    }

    @Test
    public void testProcessComponent() throws Exception {
        ProcessStatelessInput input = createSampleInput();
        ProcessInput processInput = new ProcessInput();
        processInput.data = input.data;

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