package org.datacleaner.configuration;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.datacleaner.api.Component;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.ComponentConfiguration;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Class RemoteComponentsConfigurationImplTest
 * 
 * @since 23.9.15
 */
@RunWith(EasyMockRunner.class)
public class RemoteComponentsConfigurationImplTest {

    @Test
    public void testIsAllowedAll() throws Exception {
        RemoteComponentsConfigurationImpl remoteComponentsConfiguration = new RemoteComponentsConfigurationImpl();
        Assert.assertTrue(remoteComponentsConfiguration.isAllowed("a"));
        Assert.assertTrue(remoteComponentsConfiguration.isAllowed("Separator"));
    }

    @Test
    public void testIsAllowedOnlyOne() throws Exception {
        Set<String> includes = new HashSet<>();
        Set<String> excludes = new HashSet<>();
        Map<String, List<RemoteComponentsConfigurationImpl.Property>> properties = new HashMap<>();
        includes.add("a");
        RemoteComponentsConfigurationImpl remoteComponentsConfiguration = new RemoteComponentsConfigurationImpl(
                includes, excludes, properties);
        Assert.assertTrue(remoteComponentsConfiguration.isAllowed("a"));
        Assert.assertFalse(remoteComponentsConfiguration.isAllowed("Separator"));
    }

    @Test
    public void testIsAllowedOnlyOneExclude() throws Exception {
        Set<String> includes = new HashSet<>();
        Set<String> excludes = new HashSet<>();
        Map<String, List<RemoteComponentsConfigurationImpl.Property>> properties = new HashMap<>();
        excludes.add("a");
        RemoteComponentsConfigurationImpl remoteComponentsConfiguration = new RemoteComponentsConfigurationImpl(
                includes, excludes, properties);
        Assert.assertFalse(remoteComponentsConfiguration.isAllowed("a"));
        Assert.assertTrue(remoteComponentsConfiguration.isAllowed("Separator"));
    }

    @Test
    public void testIsAllowedOneToOne() throws Exception {
        Set<String> includes = new HashSet<>();
        Set<String> excludes = new HashSet<>();
        Map<String, List<RemoteComponentsConfigurationImpl.Property>> properties = new HashMap<>();
        includes.add("a");
        excludes.add("b");
        RemoteComponentsConfigurationImpl remoteComponentsConfiguration = new RemoteComponentsConfigurationImpl(
                includes, excludes, properties);
        Assert.assertTrue(remoteComponentsConfiguration.isAllowed("a"));
        Assert.assertFalse(remoteComponentsConfiguration.isAllowed("Separator"));
    }

    @Test
    public void testIsAllowedOneToOne2() throws Exception {
        Set<String> includes = new HashSet<>();
        Set<String> excludes = new HashSet<>();
        Map<String, List<RemoteComponentsConfigurationImpl.Property>> properties = new HashMap<>();
        includes.add("a");
        excludes.add("a");
        RemoteComponentsConfigurationImpl remoteComponentsConfiguration = new RemoteComponentsConfigurationImpl(
                includes, excludes, properties);
        Assert.assertTrue(remoteComponentsConfiguration.isAllowed("a"));
    }

    @Test
    public void testSetDefaultValues() throws Exception {
        Set<String> includes = new HashSet<>();
        Set<String> excludes = new HashSet<>();
        Map<String, List<RemoteComponentsConfigurationImpl.Property>> properties = new HashMap<>();
        List<RemoteComponentsConfigurationImpl.Property> propertyList = new ArrayList<>();
        propertyList.add(new RemoteComponentsConfigurationImpl.Property("propertyName", "value", true));
        properties.put("Component", propertyList);
        RemoteComponentsConfigurationImpl remoteComponentsConfiguration = new RemoteComponentsConfigurationImpl(
                includes, excludes, properties);
        ComponentDescriptor componentDescriptorMock = createNiceMock(ComponentDescriptor.class);
        expect(componentDescriptorMock.getDisplayName()).andReturn("Component").anyTimes();

        ConfiguredPropertyDescriptor configuredPropertyDescriptorMock = createNiceMock(ConfiguredPropertyDescriptor.class);
        expect(configuredPropertyDescriptorMock.getType()).andReturn((Class) String.class).anyTimes();

        expect(componentDescriptorMock.getConfiguredProperty(anyString())).andReturn(configuredPropertyDescriptorMock)
                .anyTimes();

        HashSet<ConfiguredPropertyDescriptor> descriptorProperties = new HashSet();
        descriptorProperties.add(configuredPropertyDescriptorMock);
        expect(componentDescriptorMock.getConfiguredProperties()).andReturn(descriptorProperties);

        Component componentMock = createNiceMock(Component.class);

        configuredPropertyDescriptorMock.setValue(anyObject(), anyString());

        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Object value = EasyMock.getCurrentArguments()[1];
                Assert.assertEquals("value", value);
                return null;
            }
        });

        replay(componentDescriptorMock, configuredPropertyDescriptorMock, componentMock);
        remoteComponentsConfiguration.setDefaultValues(componentDescriptorMock, componentMock);

        verify(configuredPropertyDescriptorMock);
    }

    private Object getValue(ConfiguredPropertyDescriptor propertyDescriptor,
            ComponentConfiguration componentConfiguration) {
        final Object value = componentConfiguration.getProperty(propertyDescriptor);
        return value;
    }

}
