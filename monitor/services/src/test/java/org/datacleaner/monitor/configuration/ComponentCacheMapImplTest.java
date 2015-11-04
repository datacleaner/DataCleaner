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

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.datacleaner.beans.transform.ConcatenatorTransformer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.InjectionManagerFactory;
import org.datacleaner.descriptors.CloseMethodDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.InitializeMethodDescriptor;
import org.datacleaner.descriptors.ProvidedPropertyDescriptor;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.descriptors.ValidateMethodDescriptor;
import org.datacleaner.restclient.ComponentConfiguration;
import org.datacleaner.restclient.CreateInput;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Test;

/**
 * Class ComponentsCacheTest
 *
 * @since 28.7.15
 */
public class ComponentCacheMapImplTest {
    private String tenantName = "tenant";
    private String componentName = "name";

    @Test
    public void testCacheStoreNewConfiguration() throws Exception {
        TenantContextFactory mockTenantContextFactory = EasyMock.createMock(TenantContextFactory.class);
        TenantContext mockTenantContext = EasyMock.createMock(TenantContext.class);
        EasyMock.expect(mockTenantContextFactory.getContext(tenantName)).andReturn(mockTenantContext).anyTimes();

        ComponentStore store = EasyMock.createMock(ComponentStore.class);
        EasyMock.expect(mockTenantContext.getComponentStore()).andReturn(store).anyTimes();
        EasyMock.expect(mockTenantContext.getConfiguration()).andReturn(getDCConfigurationMock()).anyTimes();
        store.store(EasyMock.anyObject(ComponentStoreHolder.class));
        final boolean[] ok = { false };
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() {
                ok[0] = true;
                return null;
            }
        });
        EasyMock.replay(mockTenantContextFactory, mockTenantContext, store);
        ComponentCache cache = new ComponentCacheMapImpl(mockTenantContextFactory, new SimpleRemoteComponentsConfigurationImpl());
        CreateInput createInput = new CreateInput();
        createInput.configuration = new ComponentConfiguration();
        ComponentStoreHolder componentStoreHolder = new ComponentStoreHolder(100000, createInput, "id", componentName);
        cache.put(tenantName, mockTenantContext, componentStoreHolder);
        Assert.assertTrue(ok[0]);
        Assert.assertEquals(componentStoreHolder, cache.get("id", tenantName, mockTenantContext)
                .getComponentStoreHolder());
    }

    @Test
    public void testCacheRemoveConfig() throws Exception {
        String instanceId = "id";
        TenantContextFactory mockTenantContextFactory = EasyMock.createMock(TenantContextFactory.class);
        TenantContext mockTenantContext = EasyMock.createMock(TenantContext.class);
        EasyMock.expect(mockTenantContextFactory.getContext(tenantName)).andReturn(mockTenantContext).anyTimes();

        ComponentStore store = EasyMock.createMock(ComponentStore.class);
        EasyMock.expect(mockTenantContext.getComponentStore()).andReturn(store).anyTimes();
        EasyMock.expect(mockTenantContext.getConfiguration()).andReturn(getDCConfigurationMock()).anyTimes();
        store.store(EasyMock.anyObject(ComponentStoreHolder.class));

        store.remove(EasyMock.anyString());
        final boolean[] ok = { false };
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() {
                ok[0] = true;
                return true;
            }
        });

        EasyMock.replay(mockTenantContextFactory, mockTenantContext, store);
        ComponentCache cache = new ComponentCacheMapImpl(mockTenantContextFactory, new SimpleRemoteComponentsConfigurationImpl());
        CreateInput createInput = new CreateInput();
        createInput.configuration = new ComponentConfiguration();

        ComponentStoreHolder componentStoreHolder = new ComponentStoreHolder(100000, createInput, instanceId,
                componentName);
        cache.put(tenantName, mockTenantContext, componentStoreHolder);
        cache.remove(instanceId, mockTenantContext);
        Assert.assertTrue(ok[0]);
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

    private TransformerDescriptor<?> getTransformerDescriptorMock() {
        @SuppressWarnings("unchecked")
        TransformerDescriptor<ConcatenatorTransformer> transformerDescriptor = createNiceMock(TransformerDescriptor.class);
        expect(transformerDescriptor.getDisplayName()).andReturn(componentName).anyTimes();
        expect(transformerDescriptor.getProvidedProperties()).andReturn(
                Collections.<ProvidedPropertyDescriptor> emptySet()).anyTimes();
        expect(transformerDescriptor.getValidateMethods()).andReturn(Collections.<ValidateMethodDescriptor> emptySet())
                .anyTimes();
        expect(transformerDescriptor.getInitializeMethods()).andReturn(
                Collections.<InitializeMethodDescriptor> emptySet()).anyTimes();
        expect(transformerDescriptor.getCloseMethods()).andReturn(Collections.<CloseMethodDescriptor> emptySet())
                .anyTimes();
        expect(transformerDescriptor.getConfiguredProperties()).andReturn(
                Collections.<ConfiguredPropertyDescriptor> emptySet()).anyTimes();
        expect(transformerDescriptor.newInstance()).andReturn(new ConcatenatorTransformer()).anyTimes();
        replay(transformerDescriptor);

        return transformerDescriptor;
    }
}
