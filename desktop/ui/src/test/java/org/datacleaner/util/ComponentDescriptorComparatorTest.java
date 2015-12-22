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
package org.datacleaner.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.datacleaner.api.Component;
import org.datacleaner.api.ComponentCategory;
import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.configuration.RemoteServerDataImpl;
import org.datacleaner.descriptors.CloseMethodDescriptor;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.InitializeMethodDescriptor;
import org.datacleaner.descriptors.ProvidedPropertyDescriptor;
import org.datacleaner.descriptors.RemoteDescriptorProvider;
import org.datacleaner.descriptors.RemoteDescriptorProviderImpl;
import org.datacleaner.descriptors.RemoteTransformerDescriptor;
import org.datacleaner.descriptors.RemoteTransformerDescriptorImpl;
import org.datacleaner.descriptors.ValidateMethodDescriptor;
import org.junit.Assert;
import org.junit.Test;

public class ComponentDescriptorComparatorTest {
    @Test
    public void testRemoteAndLocal() {
        RemoteTransformerDescriptor<?> descriptor1 = new RemoteTransformerDescriptorImpl(getRemoteDescriptorProvider(1),
                "xyz", null, null, null);
        RemoteTransformerDescriptor<?> descriptor2 = new RemoteTransformerDescriptorImpl(getRemoteDescriptorProvider(2),
                "abc", null, null, null);

        TestLocalComponentDescriptor<?> descriptor3 = new TestLocalComponentDescriptor<Component>("xyz");
        TestLocalComponentDescriptor<?> descriptor4 = new TestLocalComponentDescriptor<Component>("abc");

        List<ComponentDescriptor<?>> list = new ArrayList<>();
        list.add(descriptor1);
        list.add(descriptor2);
        list.add(descriptor3);
        list.add(descriptor4);

        Collections.sort(list, new ComponentDescriptorComparator());

        ComponentDescriptor<?> first = list.get(0);
        ComponentDescriptor<?> second = list.get(1);
        ComponentDescriptor<?> third = list.get(2);
        ComponentDescriptor<?> fourth = list.get(3);

        Assert.assertEquals("abc", first.getDisplayName());
        Assert.assertEquals("abc", second.getDisplayName());
        Assert.assertEquals("xyz", third.getDisplayName());
        Assert.assertEquals("xyz", fourth.getDisplayName());
    }

    private RemoteDescriptorProvider getRemoteDescriptorProvider(int serverPriority) {
        RemoteServerDataImpl remoteServerData = new RemoteServerDataImpl("host", "name", serverPriority, null, null);
        RemoteDescriptorProvider remoteDescriptorProvider = new RemoteDescriptorProviderImpl(remoteServerData);

        return remoteDescriptorProvider;
    }

    @Test
    public void testCompareAllRemote() throws Exception {
        RemoteTransformerDescriptor<?> descriptor1 = new RemoteTransformerDescriptorImpl(getRemoteDescriptorProvider(1),
                "xyz", null, null, null);
        RemoteTransformerDescriptor<?> descriptor2 = new RemoteTransformerDescriptorImpl(getRemoteDescriptorProvider(2),
                "xyz", null, null, null);
        RemoteTransformerDescriptor<?> descriptor3 = new RemoteTransformerDescriptorImpl(getRemoteDescriptorProvider(2),
                "abc", null, null, null);

        List<RemoteTransformerDescriptor<?>> list = new ArrayList<>();
        list.add(descriptor1);
        list.add(descriptor2);
        list.add(descriptor3);

        Collections.sort(list, new ComponentDescriptorComparator());

        RemoteTransformerDescriptor<?> first = list.get(0);
        RemoteTransformerDescriptor<?> second = list.get(1);
        RemoteTransformerDescriptor<?> third = list.get(2);

        Assert.assertEquals("abc", first.getDisplayName());
        Assert.assertEquals(2, first.getRemoteDescriptorProvider().getServerData().getServerPriority());
        Assert.assertEquals("xyz", second.getDisplayName());
        Assert.assertEquals(2, second.getRemoteDescriptorProvider().getServerData().getServerPriority());
        Assert.assertEquals("xyz", third.getDisplayName());
        Assert.assertEquals(1, third.getRemoteDescriptorProvider().getServerData().getServerPriority());
    }

    @Test
    public void testCompareAllLocal() {
        TestLocalComponentDescriptor<?> descriptor1 = new TestLocalComponentDescriptor<Component>("xyz");
        TestLocalComponentDescriptor<?> descriptor2 = new TestLocalComponentDescriptor<Component>("abc");
        TestLocalComponentDescriptor<?> descriptor3 = new TestLocalComponentDescriptor<Component>("abc");

        List<TestLocalComponentDescriptor<?>> list = new ArrayList<>();
        list.add(descriptor1);
        list.add(descriptor2);
        list.add(descriptor3);

        Collections.sort(list, new ComponentDescriptorComparator());

        TestLocalComponentDescriptor<?> first = list.get(0);
        TestLocalComponentDescriptor<?> second = list.get(1);
        TestLocalComponentDescriptor<?> third = list.get(2);

        Assert.assertEquals("abc", first.getDisplayName());
        Assert.assertEquals("abc", second.getDisplayName());
        Assert.assertEquals("xyz", third.getDisplayName());
    }

    private class TestLocalComponentDescriptor<C extends Component> implements ComponentDescriptor<C> {
        
        private static final long serialVersionUID = 1L;
        private String displayName;

        public TestLocalComponentDescriptor(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public C newInstance() {
            return null;
        }

        @Override
        public Class<C> getComponentClass() {
            return null;
        }

        @Override
        public Set<ConfiguredPropertyDescriptor> getConfiguredProperties() {
            return null;
        }

        @Override
        public boolean isDistributable() {
            return false;
        }

        @Override
        public boolean isMultiStreamComponent() {
            return false;
        }

        @Override
        public Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesForInput() {
            return null;
        }

        @Override
        public Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesForInput(boolean includeOptional) {
            return null;
        }
        
        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public Set<ComponentCategory> getComponentCategories() {
            return null;
        }

        @Override
        public ComponentSuperCategory getComponentSuperCategory() {
            return null;
        }

        @Override
        public Set<Annotation> getAnnotations() {
            return null;
        }

        @Override
        public ConfiguredPropertyDescriptor getConfiguredProperty(String name) {
            return null;
        }

        @Override
        public Set<ValidateMethodDescriptor> getValidateMethods() {
            return null;
        }

        @Override
        public Set<InitializeMethodDescriptor> getInitializeMethods() {
            return null;
        }

        @Override
        public Set<CloseMethodDescriptor> getCloseMethods() {
            return null;
        }

        @Override
        public Set<ProvidedPropertyDescriptor> getProvidedProperties() {
            return null;
        }

        @Override
        public String[] getAliases() {
            return new String[0];
        }

        @Override
        public Set<ProvidedPropertyDescriptor> getProvidedPropertiesByType(Class<?> cls) {
            return null;
        }

        @Override
        public Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesByType(Class<?> type, boolean includeArrays) {
            return null;
        }
        
        @Override
        public Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesByAnnotation(Class<? extends Annotation> annotation) {
            return null;
        }

        @Override
        public int compareTo(ComponentDescriptor<?> o) {
            return 0;
        }
    }
}
