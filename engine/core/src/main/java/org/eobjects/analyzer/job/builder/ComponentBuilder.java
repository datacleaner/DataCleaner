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

import java.util.Collection;
import java.util.Map;

import org.apache.metamodel.util.HasName;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ComponentDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.ComponentRequirement;
import org.eobjects.analyzer.job.HasComponentRequirement;
import org.eobjects.analyzer.job.InputColumnSinkJob;
import org.eobjects.analyzer.metadata.HasMetadataProperties;

/**
 * Represents a builder object for components in a {@link AnalysisJob}.
 */
public interface ComponentBuilder extends HasMetadataProperties, InputColumnSinkJob, HasComponentRequirement, HasName {

    /**
     * Determines if the underlying component is fully configured or not. This
     * is equivalent to invoking {@link #isConfigured(boolean))} with a 'false'
     * argument.
     * 
     * @return
     */
    public boolean isConfigured();

    /**
     * Gets the underlying component object/instance that is being built.
     * 
     * @return the underlying component that is being built, or null if it was
     *         not possible to build one given the current builder
     *         configuration.
     */
    public Object getComponentInstance();

    /**
     * Determines if the underlying component is fully configured or not,
     * optionally throwing an exception that will indicate if anything is not
     * correct from a configuration point of view.
     * 
     * @param throwException
     * @return
     * @throws IllegalStateException
     * @throws UnconfiguredConfiguredPropertyException
     */
    public boolean isConfigured(boolean throwException) throws IllegalStateException,
            UnconfiguredConfiguredPropertyException;

    /**
     * Sets a metadata property
     * 
     * @param key
     * @param value
     */
    public void setMetadataProperty(String key, String value);

    /**
     * Sets all the provided metadata properties, removing any existing metadata
     * properties.
     * 
     * @param metadataProperties
     */
    public void setMetadataProperties(Map<String, String> metadataProperties);

    /**
     * Removes a metadata property
     * 
     * @param key
     */
    public void removeMetadataProperty(String key);

    /**
     * Gets the {@link ComponentDescriptor} of the underlying component
     * 
     * @return
     */
    public ComponentDescriptor<?> getDescriptor();

    /**
     * Determines if a particular configured property is configured or not.
     * 
     * @param configuredProperty
     * @param throwException
     * @return
     * @throws UnconfiguredConfiguredPropertyException
     */
    public boolean isConfigured(ConfiguredPropertyDescriptor configuredProperty, boolean throwException)
            throws UnconfiguredConfiguredPropertyException;

    /**
     * Sets a configured property value by it's name.
     * 
     * @param propertyName
     * @param value
     * @return this component builder
     * @throws IllegalArgumentException
     */
    public ComponentBuilder setConfiguredProperty(String propertyName, Object value) throws IllegalArgumentException;

    /**
     * Sets a configured property value.
     * 
     * @param property
     * @param value
     * @return this component builder
     * @throws IllegalArgumentException
     */
    public ComponentBuilder setConfiguredProperty(ConfiguredPropertyDescriptor property, Object value)
            throws IllegalArgumentException;

    /**
     * Gets a map of all configured properties and their values in the
     * underlying component.
     * 
     * @return
     */
    public Map<ConfiguredPropertyDescriptor, Object> getConfiguredProperties();

    /**
     * Sets the configured properties onto the underlying component.
     * 
     * @param configuredPropeties
     */
    public void setConfiguredProperties(Map<ConfiguredPropertyDescriptor, Object> configuredPropeties);

    /**
     * Gets the value of a particular configured property in the underlying
     * component.
     * 
     * @param propertyDescriptor
     * @return
     */
    public Object getConfiguredProperty(ConfiguredPropertyDescriptor propertyDescriptor);

    public void setComponentRequirement(ComponentRequirement requirement);

    /**
     * Gets a {@link ConfiguredPropertyDescriptor} for the "default input" of
     * the underlying component, if such a default is available.
     * 
     * In cases when there is no single default input property, an exception
     * will be thrown.
     * 
     * @return
     * @throws UnsupportedOperationException
     */
    public ConfiguredPropertyDescriptor getDefaultConfiguredPropertyForInput() throws UnsupportedOperationException;

    public ComponentBuilder addInputColumn(InputColumn<?> inputColumn, ConfiguredPropertyDescriptor propertyDescriptor)
            throws IllegalArgumentException;

    public ComponentBuilder addInputColumn(InputColumn<?> inputColumn) throws IllegalArgumentException;

    public ComponentBuilder addInputColumns(Collection<? extends InputColumn<?>> inputColumns,
            ConfiguredPropertyDescriptor propertyDescriptor) throws IllegalArgumentException;

    public ComponentBuilder addInputColumns(Collection<? extends InputColumn<?>> inputColumns)
            throws IllegalArgumentException;

    public ComponentBuilder addInputColumns(InputColumn<?>... inputColumns) throws IllegalArgumentException;

    public ComponentBuilder removeInputColumn(InputColumn<?> inputColumn);

    public ComponentBuilder removeInputColumn(InputColumn<?> inputColumn,
            ConfiguredPropertyDescriptor propertyDescriptor);

    public void clearInputColumns();
}
