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
package org.datacleaner.monitor.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.InjectionManagerFactory;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 * Class ComponentsCacheTest
 *
 * @author k.houzvicka
 * @since 28.7.15
 */
public class ComponentsCacheTest {

    private String componentName = "name";

    @Test
    public void testCacheStoreNewConfiguration() throws Exception {
        String tenantName = "tenant";
        TenantContextFactory mockTenantContextFactory = EasyMock.createMock(TenantContextFactory.class);
        TenantContext mockTenantContext = EasyMock.createMock(TenantContext.class);
        EasyMock.expect(mockTenantContextFactory.getContext(tenantName)).andReturn(mockTenantContext);
        EasyMock.expect(mockTenantContextFactory.getAllTenantsName()).andReturn(new HashSet(Arrays.asList(tenantName)));

        ComponentsStore store = EasyMock.createMock(ComponentsStore.class);
        EasyMock.expect(mockTenantContext.getComponentsStore()).andReturn(store);
        EasyMock.expect(mockTenantContext.getConfiguration()).andReturn(getDCConfigurationMock());
        store.storeConfiguration(EasyMock.anyObject(ComponentsStoreHolder.class));
        final boolean[] ok = {false};
        EasyMock.expectLastCall().andAnswer(new IAnswer() {
            public Object answer() {
                ok[0] = true;
                 return null;
            }
        });
        EasyMock.replay(mockTenantContextFactory, mockTenantContext, store);
        ComponentsCache cache = new ComponentsCache(mockTenantContextFactory);
        CreateInput createInput = new CreateInput();
        createInput.configuration = new ComponentConfiguration();
        ComponentsStoreHolder componentStoreHolder = new ComponentsStoreHolder(100000, createInput, "id", componentName);
        cache.putComponent(tenantName, mockTenantContext, componentStoreHolder);
        Assert.assertTrue(ok[0]);
        Assert.assertEquals(componentStoreHolder, cache.getConfigHolder("id", tenantName, mockTenantContext).getComponentsStoreHolder());
    }

    @Test
    public void testCacheRemoveConfig() throws Exception {
        String tenantName = "tenant";
        String componentID = "id";
        TenantContextFactory mockTenantContextFactory = EasyMock.createMock(TenantContextFactory.class);
        TenantContext mockTenantContext = EasyMock.createMock(TenantContext.class);
        EasyMock.expect(mockTenantContextFactory.getContext(tenantName)).andReturn(mockTenantContext);
        EasyMock.expect(mockTenantContextFactory.getAllTenantsName()).andReturn(new HashSet(Arrays.asList(tenantName)));

        ComponentsStore store = EasyMock.createMock(ComponentsStore.class);
        EasyMock.expect(mockTenantContext.getComponentsStore()).andReturn(store).anyTimes();
        EasyMock.expect(mockTenantContext.getConfiguration()).andReturn(getDCConfigurationMock());
        store.storeConfiguration(EasyMock.anyObject(ComponentsStoreHolder.class));

        store.removeConfiguration(EasyMock.anyString());
        final boolean[] ok = {false};
        EasyMock.expectLastCall().andAnswer(new IAnswer() {
            public Object answer() {
                ok[0] = true;
                return true;
            }
        });

        EasyMock.replay(mockTenantContextFactory, mockTenantContext, store);
        ComponentsCache cache = new ComponentsCache(mockTenantContextFactory);
        CreateInput createInput = new CreateInput();
        createInput.configuration = new ComponentConfiguration();

        ComponentsStoreHolder componentStoreHolder = new ComponentsStoreHolder(100000, createInput, componentID, componentName);
        cache.putComponent(tenantName, mockTenantContext, componentStoreHolder );
        cache.removeConfiguration(componentID, mockTenantContext);
        Assert.assertTrue(ok[0]);
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
        expect(transformerDescriptor.getConfiguredProperties()).andReturn(Collections.EMPTY_SET).anyTimes();
        Class<? extends TransformerDescriptor> clazz = transformerDescriptor.getClass();
        //expect(transformerDescriptor.getClass()).andReturn().anyTimes(); // mytodo
        //expect(transformerDescriptor.getClass()).andReturn((Class) clazz).anyTimes();
        expect(transformerDescriptor.getCloseMethods()).andReturn(Collections.EMPTY_SET).anyTimes();
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

}
