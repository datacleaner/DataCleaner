/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.monitor.server.job;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eobjects.analyzer.beans.api.Converter;
import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.descriptors.ComponentDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.job.BeanConfiguration;
import org.eobjects.analyzer.job.ComponentConfigurationException;
import org.eobjects.analyzer.job.ImmutableBeanConfiguration;
import org.eobjects.analyzer.job.NoSuchComponentException;
import org.eobjects.analyzer.util.convert.StringConverter;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.jaxb.CustomJavaComponentJob;
import org.eobjects.datacleaner.monitor.jaxb.PropertiesType;
import org.eobjects.datacleaner.monitor.jaxb.PropertiesType.Property;
import org.eobjects.datacleaner.monitor.job.JobContext;
import org.eobjects.datacleaner.monitor.job.XmlJobContext;
import org.eobjects.datacleaner.monitor.server.jaxb.JaxbCustomJavaComponentJobAdaptor;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Func;
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

    public CustomJobContext(TenantContext tenantContext, CustomJobEngine engine, RepositoryFile file, InjectionManager injectionManager) {
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
        String groupName = getCustomJavaComponentJob().getGroupName();
        return groupName;
    }

    @Override
    public Map<String, String> getVariables() {
        final Map<String, String> variables = new LinkedHashMap<String, String>();
        final ComponentDescriptor<?> descriptor = getDescriptor();
        final BeanConfiguration beanConfiguration = getBeanConfiguration(null);
        final Set<ConfiguredPropertyDescriptor> properties = descriptor.getConfiguredProperties();
        final StringConverter stringConverter = new StringConverter(_injectionManager);
        for (ConfiguredPropertyDescriptor configuredProperty : properties) {
            Object value = beanConfiguration.getProperty(configuredProperty);
            String valueString = null;
            try {
                final Class<? extends Converter<?>> customConverter = configuredProperty.getCustomConverter();
                valueString = stringConverter.serialize(value, customConverter);
            } catch (Exception e) {
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
            final ComponentDescriptor<?> descriptor = Descriptors.ofComponent(customJavaClass);
            return descriptor;
        } catch (Exception e) {
            throw new NoSuchComponentException(CustomJob.class, className);
        }
    }

    public CustomJavaComponentJob getCustomJavaComponentJob() {
        // there's a 2 second read cache time - enough to only need to read once
        // for executing a job under normal circumstances
        if (_cachedReadTime == -1 || System.currentTimeMillis() - _cachedReadTime > 2000) {
            _cachedCustomJavaJob = _file
                    .readFile(new Func<InputStream, CustomJavaComponentJob>() {
                        @Override
                        public CustomJavaComponentJob eval(InputStream in) {
                            final JaxbCustomJavaComponentJobAdaptor adaptor = new JaxbCustomJavaComponentJobAdaptor();
                            CustomJavaComponentJob result = adaptor.unmarshal(in);
                            _cachedReadTime = System.currentTimeMillis();
                            return result;
                        }
                    });
        }
        return _cachedCustomJavaJob;
    }

    @Override
    public void toXml(final OutputStream out) {
        _file.readFile(new Action<InputStream>() {
            @Override
            public void run(InputStream in) throws Exception {
                FileHelper.copy(in, out);
            }
        });
    }

    public BeanConfiguration getBeanConfiguration(CustomJob customJavaJob) {
        final ComponentDescriptor<?> descriptor = getDescriptor();
        final Map<ConfiguredPropertyDescriptor, Object> propertyMap = new HashMap<ConfiguredPropertyDescriptor, Object>();
        final PropertiesType propertiesType = getCustomJavaComponentJob().getProperties();
        final StringConverter stringConverter = new StringConverter(_injectionManager);
        
        // build initial map based on default values from the customJob instance
        if (customJavaJob != null) {
            final Set<ConfiguredPropertyDescriptor> configuredProperties = descriptor.getConfiguredProperties();
            for (ConfiguredPropertyDescriptor property : configuredProperties) {
                Object value = property.getValue(customJavaJob);
                propertyMap.put(property, value);
            }
        }
        
        // then build/override map based upon specified <property> elements in the XML file
        if (propertiesType != null) {
            final List<Property> propertyTypes = propertiesType.getProperty();
            for (Property propertyType : propertyTypes) {
                final String name = propertyType.getName();
                final String value = propertyType.getValue();
                setProperty(descriptor, propertyMap, name, value, stringConverter);
            }
        }
        
        return new ImmutableBeanConfiguration(propertyMap);
    }

    private void setProperty(ComponentDescriptor<?> descriptor, Map<ConfiguredPropertyDescriptor, Object> propertyMap,
            String name, String valueString, StringConverter stringConverter) {
        final ConfiguredPropertyDescriptor configuredProperty = descriptor.getConfiguredProperty(name);
        if (configuredProperty == null) {
            throw new ComponentConfigurationException("No such configured property in class '"
                    + getDescriptor().getComponentClass().getName() + "': " + name);
        }
        final Class<? extends Converter<?>> customConverter = configuredProperty.getCustomConverter();
        final Object value = stringConverter.deserialize(valueString, configuredProperty.getType(), customConverter);
        propertyMap.put(configuredProperty, value);
    }

    @Override
    public CustomJobEngine getJobEngine() {
        return _engine;
    }

}
