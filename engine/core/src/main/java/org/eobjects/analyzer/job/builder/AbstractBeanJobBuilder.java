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
package org.eobjects.analyzer.job.builder;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.metamodel.util.EqualsBuilder;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.BeanConfiguration;
import org.eobjects.analyzer.job.ImmutableBeanConfiguration;
import org.eobjects.analyzer.lifecycle.LifeCycleHelper;
import org.eobjects.analyzer.result.renderer.Renderable;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract {@link ComponentBuilder} for components of a {@link AnalysisJob}.
 * 
 * @param <D>
 *            the component descriptor type (eg. AnalyzerBeanDescriptor)
 * @param <E>
 *            the actual component type (eg. Analyzer)
 * @param <B>
 *            the concrete {@link ComponentBuilder} (eg. AnalyzerJobBuilder)
 */
@SuppressWarnings("unchecked")
public abstract class AbstractBeanJobBuilder<D extends BeanDescriptor<E>, E, B extends ComponentBuilder> implements
        ComponentBuilder, Renderable {

    private static final Logger logger = LoggerFactory.getLogger(AbstractBeanJobBuilder.class);

    private final D _descriptor;
    private final E _configurableBean;
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final Map<String, String> _metadataProperties;
    private String _name;

    public AbstractBeanJobBuilder(AnalysisJobBuilder analysisJobBuilder, D descriptor, Class<?> builderClass) {
        if (analysisJobBuilder == null) {
            throw new IllegalArgumentException("analysisJobBuilder cannot be null");
        }
        if (descriptor == null) {
            throw new IllegalArgumentException("descriptor cannot be null");
        }
        if (builderClass == null) {
            throw new IllegalArgumentException("builderClass cannot be null");
        }
        _analysisJobBuilder = analysisJobBuilder;
        _descriptor = descriptor;
        if (!ReflectionUtils.is(getClass(), builderClass)) {
            throw new IllegalArgumentException("Builder class does not correspond to actual class of builder");
        }

        _configurableBean = ReflectionUtils.newInstance(_descriptor.getComponentClass());
        _metadataProperties = new LinkedHashMap<>();
    }

    /**
     * Gets metadata properties as a map.
     * 
     * @return
     */
    @Override
    public final Map<String, String> getMetadataProperties() {
        return _metadataProperties;
    }

    /**
     * Gets a metadata property
     * 
     * @param key
     * @return
     */
    @Override
    public final String getMetadataProperty(String key) {
        return _metadataProperties.get(key);
    }

    /**
     * Sets a metadata property
     * 
     * @param key
     * @param value
     */
    @Override
    public final void setMetadataProperty(String key, String value) {
        _metadataProperties.put(key, value);
    }

    @Override
    public void setMetadataProperties(Map<String, String> metadataProperties) {
        _metadataProperties.clear();
        if (metadataProperties != null) {
            _metadataProperties.putAll(metadataProperties);
        }
    }

    /**
     * Removes/clears a metadata property
     * 
     * @param key
     */
    @Override
    public final void removeMetadataProperty(String key) {
        _metadataProperties.remove(key);
    }

    public final AnalysisJobBuilder getAnalysisJobBuilder() {
        return _analysisJobBuilder;
    }

    @Override
    public final D getDescriptor() {
        return _descriptor;
    }

    @Override
    public final E getComponentInstance() {
        return _configurableBean;
    }

    /**
     * @deprecated use {@link #getComponentInstance()} instead.
     */
    @Deprecated
    public final E getConfigurableBean() {
        return getComponentInstance();
    }

    @Override
    public void setConfiguredProperties(Map<ConfiguredPropertyDescriptor, Object> configuredPropeties) {
        final ImmutableBeanConfiguration beanConfiguration = new ImmutableBeanConfiguration(configuredPropeties);
        setConfiguredProperties(beanConfiguration);
    }

    /**
     * Sets the configured properties of this component based on a
     * {@link BeanConfiguration}.
     * 
     * @param configuration
     */
    public void setConfiguredProperties(BeanConfiguration configuration) {
        boolean changed = false;
        final Set<ConfiguredPropertyDescriptor> properties = getDescriptor().getConfiguredProperties();
        for (ConfiguredPropertyDescriptor property : properties) {
            final Object value = configuration.getProperty(property);
            final boolean changedValue = setConfiguredPropertyIfChanged(property, value);
            if (changedValue) {
                changed = true;
            }
        }
        if (changed) {
            onConfigurationChanged();
        }
    }

    @Override
    public final boolean isConfigured(boolean throwException) throws IllegalStateException,
            UnconfiguredConfiguredPropertyException {
        for (ConfiguredPropertyDescriptor configuredProperty : _descriptor.getConfiguredProperties()) {
            if (!isConfigured(configuredProperty, throwException)) {
                if (throwException) {
                    throw new UnconfiguredConfiguredPropertyException(this, configuredProperty);
                } else {
                    return false;
                }
            }
        }

        try {
            LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(null, null, false);
            lifeCycleHelper.validate(getDescriptor(), getConfigurableBean());
        } catch (RuntimeException e) {
            if (throwException) {
                throw e;
            } else {
                return false;
            }
        }

        return true;
    }

    @Override
    public String getName() {
        return _name;
    }

    public B setName(String name) {
        _name = name;
        return (B) this;
    }

    @Override
    public boolean isConfigured() {
        return isConfigured(false);
    }

    @Override
    public boolean isConfigured(ConfiguredPropertyDescriptor configuredProperty, boolean throwException)
            throws UnconfiguredConfiguredPropertyException {
        if (configuredProperty.isRequired()) {
            Map<ConfiguredPropertyDescriptor, Object> configuredProperties = getConfiguredProperties();
            Object value = configuredProperties.get(configuredProperty);
            if (configuredProperty.isArray() && value != null) {
                if (Array.getLength(value) == 0) {
                    value = null;
                }
            }
            if (value == null) {
                if (throwException) {
                    throw new UnconfiguredConfiguredPropertyException(this, configuredProperty);
                } else {
                    logger.debug("Configured property is not set: " + configuredProperty);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public B setConfiguredProperty(String configuredName, Object value) {
        ConfiguredPropertyDescriptor configuredProperty = _descriptor.getConfiguredProperty(configuredName);
        if (configuredProperty == null) {
            throw new IllegalArgumentException("No such configured property: " + configuredName);
        }
        return setConfiguredProperty(configuredProperty, value);
    }

    @Override
    public B setConfiguredProperty(ConfiguredPropertyDescriptor configuredProperty, Object value) {
        final boolean changed = setConfiguredPropertyIfChanged(configuredProperty, value);
        if (changed) {
            onConfigurationChanged();
        }
        return (B) this;
    }

    /**
     * Sets a configured property if it has changed.
     * 
     * Note that this method is for internal use. It does not invoke
     * {@link #onConfigurationChanged()} even if changes happen. The reason for
     * this is to allow code reuse and avoid chatty use of the notification
     * method.
     * 
     * @param configuredProperty
     * @param value
     * @return true if the value was changed or false if it was not
     */
    protected boolean setConfiguredPropertyIfChanged(final ConfiguredPropertyDescriptor configuredProperty,
            final Object value) {
        if (configuredProperty == null) {
            throw new IllegalArgumentException("configuredProperty cannot be null");
        }

        final Object currentValue = configuredProperty.getValue(_configurableBean);
        if (EqualsBuilder.equals(currentValue, value)) {
            // no change
            return false;
        }

        if (value != null) {
            boolean correctType = true;
            if (configuredProperty.isArray()) {
                if (value.getClass().isArray()) {
                    int length = Array.getLength(value);
                    for (int i = 0; i < length; i++) {
                        Object valuePart = Array.get(value, i);
                        if (valuePart == null) {
                            logger.warn("Element no. {} in array (size {}) is null! Value passed to {}", new Object[] {
                                    i, length, configuredProperty });
                        } else {
                            if (!ReflectionUtils.is(valuePart.getClass(), configuredProperty.getBaseType())) {
                                correctType = false;
                            }
                        }
                    }
                } else {
                    if (!ReflectionUtils.is(value.getClass(), configuredProperty.getBaseType())) {
                        correctType = false;
                    }
                }
            } else {
                if (!ReflectionUtils.is(value.getClass(), configuredProperty.getBaseType())) {
                    correctType = false;
                }
            }
            if (!correctType) {
                throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName() + ", expected: "
                        + configuredProperty.getBaseType().getName());
            }
        }

        configuredProperty.setValue(_configurableBean, value);
        return true;
    }

    @Override
    public Map<ConfiguredPropertyDescriptor, Object> getConfiguredProperties() {
        Map<ConfiguredPropertyDescriptor, Object> map = new HashMap<ConfiguredPropertyDescriptor, Object>();
        Set<ConfiguredPropertyDescriptor> configuredProperties = getDescriptor().getConfiguredProperties();
        for (ConfiguredPropertyDescriptor propertyDescriptor : configuredProperties) {
            Object value = getConfiguredProperty(propertyDescriptor);
            if (value != null) {
                map.put(propertyDescriptor, value);
            }
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * method that can be used by sub-classes to add callback logic when the
     * configuration of the bean changes
     */
    public void onConfigurationChanged() {
    }

    @Override
    public Object getConfiguredProperty(ConfiguredPropertyDescriptor propertyDescriptor) {
        return propertyDescriptor.getValue(getConfigurableBean());
    }
}
