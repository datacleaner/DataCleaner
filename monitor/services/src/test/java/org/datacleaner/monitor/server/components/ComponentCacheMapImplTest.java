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

import org.datacleaner.beans.transform.ConcatenatorTransformer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.InjectionManagerFactory;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.SimpleDescriptorProvider;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.monitor.configuration.ComponentStore;
import org.datacleaner.monitor.configuration.ComponentStoreHolder;
import org.datacleaner.monitor.configuration.SimpleRemoteComponentsConfigurationImpl;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.restclient.ComponentConfiguration;
import org.datacleaner.restclient.CreateInput;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * Class ComponentsCacheTest
 *
 * @since 28.7.15
 */
public class ComponentCacheMapImplTest {

    TenantContextFactory mockTenantContextFactory;
    ComponentCache cache;
    private String tenantName = "tenant";
    private String componentName = "name";

    private void createComponentCache() {
        final ComponentHandlerFactory compHandlerFac =
                new ComponentHandlerFactory(new SimpleRemoteComponentsConfigurationImpl());
        cache = new ComponentCache(compHandlerFac, mockTenantContextFactory);
    }

    @After
    public void tearDown() throws InterruptedException {
        cache.close();
    }

    @Test
    public void testCacheStoreNewConfiguration() throws Exception {
        mockTenantContextFactory = EasyMock.createMock(TenantContextFactory.class);
        final TenantContext mockTenantContext = EasyMock.createMock(TenantContext.class);
        EasyMock.expect(mockTenantContextFactory.getContext(tenantName)).andReturn(mockTenantContext).anyTimes();

        final ComponentStore store = EasyMock.createMock(ComponentStore.class);
        EasyMock.expect(mockTenantContext.getComponentStore()).andReturn(store).anyTimes();
        EasyMock.expect(mockTenantContext.getConfiguration()).andReturn(getDCConfigurationMock()).anyTimes();
        store.store(EasyMock.anyObject(ComponentStoreHolder.class));
        final boolean[] ok = { false };
        EasyMock.expectLastCall().andAnswer(() -> {
            ok[0] = true;
            return null;
        });
        EasyMock.replay(mockTenantContextFactory, mockTenantContext, store);
        createComponentCache();

        final CreateInput createInput = new CreateInput();
        createInput.configuration = new ComponentConfiguration();
        final ComponentStoreHolder componentStoreHolder =
                new ComponentStoreHolder(100000, createInput, "id", componentName);
        cache.put(tenantName, mockTenantContext, componentStoreHolder);
        Assert.assertTrue(ok[0]);
        Assert.assertEquals(componentStoreHolder,
                cache.get("id", tenantName, mockTenantContext).getComponentStoreHolder());
    }

    @Test
    public void testCacheRemoveConfig() throws Exception {
        final String instanceId = "id";
        mockTenantContextFactory = EasyMock.createMock(TenantContextFactory.class);
        final TenantContext mockTenantContext = EasyMock.createMock(TenantContext.class);
        EasyMock.expect(mockTenantContextFactory.getContext(tenantName)).andReturn(mockTenantContext).anyTimes();

        final ComponentStore store = EasyMock.createMock(ComponentStore.class);
        EasyMock.expect(mockTenantContext.getComponentStore()).andReturn(store).anyTimes();
        EasyMock.expect(mockTenantContext.getConfiguration()).andReturn(getDCConfigurationMock()).anyTimes();
        store.store(EasyMock.anyObject(ComponentStoreHolder.class));

        store.remove(EasyMock.anyString());
        final boolean[] ok = { false };
        EasyMock.expectLastCall().andAnswer(() -> {
            ok[0] = true;
            return true;
        });

        EasyMock.replay(mockTenantContextFactory, mockTenantContext, store);
        createComponentCache();
        final CreateInput createInput = new CreateInput();
        createInput.configuration = new ComponentConfiguration();

        final ComponentStoreHolder componentStoreHolder =
                new ComponentStoreHolder(100000, createInput, instanceId, componentName);
        cache.put(tenantName, mockTenantContext, componentStoreHolder);
        cache.remove(instanceId, mockTenantContext);
        Assert.assertTrue(ok[0]);
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

    private DescriptorProvider getDescriptorProviderMock() {
        final TransformerDescriptor<?> transformerDescriptorMock = getTransformerDescriptorMock();
        final SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider(false);
        descriptorProvider.addTransformerBeanDescriptor(transformerDescriptorMock);
        return descriptorProvider;
    }

    private TransformerDescriptor<?> getTransformerDescriptorMock() {
        @SuppressWarnings("unchecked") final TransformerDescriptor<ConcatenatorTransformer> transformerDescriptor =
                createNiceMock(TransformerDescriptor.class);
        expect(transformerDescriptor.getDisplayName()).andReturn(componentName).anyTimes();
        expect(transformerDescriptor.getProvidedProperties()).andReturn(Collections.emptySet()).anyTimes();
        expect(transformerDescriptor.getValidateMethods()).andReturn(Collections.emptySet()).anyTimes();
        expect(transformerDescriptor.getInitializeMethods()).andReturn(Collections.emptySet()).anyTimes();
        expect(transformerDescriptor.getCloseMethods()).andReturn(Collections.emptySet()).anyTimes();
        expect(transformerDescriptor.getConfiguredProperties()).andReturn(Collections.emptySet()).anyTimes();
        expect(transformerDescriptor.newInstance()).andReturn(new ConcatenatorTransformer()).anyTimes();
        replay(transformerDescriptor);

        return transformerDescriptor;
    }
}
