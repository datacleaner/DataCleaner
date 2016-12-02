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
package org.datacleaner.monitor.server.job;

import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.metamodel.util.FileHelper;
import org.datacleaner.api.Converter;
import org.datacleaner.configuration.InjectionManager;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.job.ComponentConfiguration;
import org.datacleaner.job.ComponentConfigurationException;
import org.datacleaner.job.ImmutableComponentConfiguration;
import org.datacleaner.job.NoSuchComponentException;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.jaxb.CustomJavaComponentJob;
import org.datacleaner.monitor.jaxb.PropertiesType;
import org.datacleaner.monitor.jaxb.PropertiesType.Property;
import org.datacleaner.monitor.job.JobContext;
import org.datacleaner.monitor.job.XmlJobContext;
import org.datacleaner.monitor.server.jaxb.JaxbCustomJavaComponentJobAdaptor;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.util.convert.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link JobContext} for custom Java jobs.
 */
public class CustomJobContext implements XmlJobContext {

    private static final Logger logger = LoggerFactory.getLogger(CustomJobContext.class);

    private final RepositoryFile _file;
    private final InjectionManager _injectionManager;
    private final CustomJobEngine _engine;
    private final TenantContext _tenantContext;

    private long _cachedReadTime = -1;
    private CustomJavaComponentJob _cachedCustomJavaJob;

    public CustomJobContext(final TenantContext tenantContext, final CustomJobEngine engine, final RepositoryFile file,
            final InjectionManager injectionManager) {
        _tenantContext = tenantContext;
        _engine = engine;
        _file = file;
        _injectionManager = injectionManager;
    }

    @Override
    public String getName() {
        final int extensionLength = CustomJobEngine.EXTENSION.length();
        final String filename = _file.getName();
        return filename.substring(0, filename.length() - extensionLength);
    }

    @Override
    public TenantContext getTenantContext() {
        return _tenantContext;
    }

    @Override
    public RepositoryFile getJobFile() {
        return _file;
    }

    @Override
    public String getGroupName() {
        return getCustomJavaComponentJob().getGroupName();
    }

    @Override
    public Map<String, String> getVariables() {
        final Map<String, String> variables = new LinkedHashMap<>();
        final ComponentDescriptor<?> descriptor = getDescriptor();
        final ComponentConfiguration beanConfiguration = getComponentConfiguration(null);
        final Set<ConfiguredPropertyDescriptor> properties = descriptor.getConfiguredProperties();
        final StringConverter stringConverter = new StringConverter(_injectionManager);
        for (final ConfiguredPropertyDescriptor configuredProperty : properties) {
            final Object value = beanConfiguration.getProperty(configuredProperty);
            String valueString = null;
            try {
                final Converter<?> customConverter = configuredProperty.createCustomConverter();
                valueString = stringConverter.serialize(value, customConverter);
            } catch (final Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Could not serialize value " + value + " to string, returning null variable value", e);
                } else {
                    logger.info("Could not serialize value {} to string, returning null variable value", value);
                }
            }
            variables.put(configuredProperty.getName(), valueString);
        }
        return variables;
    }

    /**
     * Gets a {@link ComponentDescriptor} for the {@link CustomJob}
     * component that this {@link JobContext} represents.
     *
     * @return
     */
    public ComponentDescriptor<?> getDescriptor() {
        final String className = getCustomJavaComponentJob().getClassName();
        try {
            final Class<?> customJavaClass = Class.forName(className, true, getClass().getClassLoader());
            return Descriptors.ofComponent(customJavaClass);
        } catch (final Exception e) {
            throw new NoSuchComponentException(CustomJob.class, className);
        }
    }

    public CustomJavaComponentJob getCustomJavaComponentJob() {
        // there's a 2 second read cache time - enough to only need to read once
        // for executing a job under normal circumstances
        if (_cachedReadTime == -1 || System.currentTimeMillis() - _cachedReadTime > 2000) {
            _cachedCustomJavaJob = _file.readFile(in -> {
                final JaxbCustomJavaComponentJobAdaptor adaptor = new JaxbCustomJavaComponentJobAdaptor();
                final CustomJavaComponentJob result = adaptor.unmarshal(in);
                _cachedReadTime = System.currentTimeMillis();
                return result;
            });
        }
        return _cachedCustomJavaJob;
    }

    @Override
    public void toXml(final OutputStream out) {
        _file.readFile(in -> {
            FileHelper.copy(in, out);
        });
    }

    public ComponentConfiguration getComponentConfiguration(final CustomJob customJavaJob) {
        final ComponentDescriptor<?> descriptor = getDescriptor();
        final Map<ConfiguredPropertyDescriptor, Object> propertyMap = new HashMap<>();
        final PropertiesType propertiesType = getCustomJavaComponentJob().getProperties();
        final StringConverter stringConverter = new StringConverter(_injectionManager);

        // build initial map based on default values from the customJob instance
        if (customJavaJob != null) {
            final Set<ConfiguredPropertyDescriptor> configuredProperties = descriptor.getConfiguredProperties();
            for (final ConfiguredPropertyDescriptor property : configuredProperties) {
                final Object value = property.getValue(customJavaJob);
                propertyMap.put(property, value);
            }
        }

        // then build/override map based upon specified <property> elements in the XML file
        if (propertiesType != null) {
            final List<Property> propertyTypes = propertiesType.getProperty();
            for (final Property propertyType : propertyTypes) {
                final String name = propertyType.getName();
                final String value = propertyType.getValue();
                setProperty(descriptor, propertyMap, name, value, stringConverter);
            }
        }

        return new ImmutableComponentConfiguration(propertyMap);
    }

    private void setProperty(final ComponentDescriptor<?> descriptor,
            final Map<ConfiguredPropertyDescriptor, Object> propertyMap, final String name, final String valueString,
            final StringConverter stringConverter) {
        final ConfiguredPropertyDescriptor configuredProperty = descriptor.getConfiguredProperty(name);
        if (configuredProperty == null) {
            throw new ComponentConfigurationException(
                    "No such configured property in class '" + getDescriptor().getComponentClass().getName() + "': "
                            + name);
        }
        final Converter<?> customConverter = configuredProperty.createCustomConverter();
        final Object value = stringConverter.deserialize(valueString, configuredProperty.getType(), customConverter);
        propertyMap.put(configuredProperty, value);
    }

    @Override
    public CustomJobEngine getJobEngine() {
        return _engine;
    }

    @Override
    public Map<String, String> getMetadataProperties() {
        return Collections.emptyMap();
    }
}
