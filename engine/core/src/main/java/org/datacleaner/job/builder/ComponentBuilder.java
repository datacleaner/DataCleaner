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
package org.datacleaner.job.builder;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.HasName;
import org.datacleaner.api.Component;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.api.Renderable;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentConfiguration;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.ComponentValidationException;
import org.datacleaner.job.HasComponentRequirement;
import org.datacleaner.job.InputColumnSinkJob;
import org.datacleaner.job.OutputDataStreamJobSource;
import org.datacleaner.metadata.HasMetadataProperties;

/**
 * Represents a builder object for components in a {@link AnalysisJob}.
 */
public interface ComponentBuilder extends HasMetadataProperties, InputColumnSinkJob, OutputDataStreamJobSource,
        HasComponentRequirement, HasName, Renderable {

    /**
     * Determines if the underlying component is fully configured or not. This
     * is equivalent to invoking {@link #isConfigured(boolean))} with a 'false'
     * argument.
     * 
     * @return
     */
    public boolean isConfigured();

    /**
     * Sets the name of the {@link ComponentBuilder}. Names can be used to
     * identify the individual components of a job, in case there are multiple
     * components of the same type. See {@link #getName()}.
     * 
     * @param name
     */
    public void setName(String name);

    /**
     * Gets the underlying component object/instance that is being built.
     * 
     * @return the underlying component that is being built, or null if it was
     *         not possible to build one given the current builder
     *         configuration.
     */
    public Component getComponentInstance();

    /**
     * Determines if the underlying component is fully configured or not,
     * optionally throwing an exception that will indicate if anything is not
     * correct from a configuration point of view.
     * 
     * @param throwException
     * @return
     * @throws ComponentValidationException
     * @throws UnconfiguredConfiguredPropertyException
     */
    public boolean isConfigured(boolean throwException) throws ComponentValidationException,
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
     * Sets the configured properties of this component based on a
     * {@link ComponentConfiguration}.
     * 
     * @param componentConfiguration
     */
    public void setConfiguredProperties(ComponentConfiguration componentConfiguration);

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

    /**
     * Gets the {@link AnalysisJobBuilder} that this {@link ComponentBuilder} is
     * contained in - if any
     * 
     * @return a builder or null if none exist.
     */
    public AnalysisJobBuilder getAnalysisJobBuilder();

    /**
     * Adds a {@link ComponentRemovalListener} to this {@link ComponentBuilder}
     * instance.
     * 
     * @param componentRemovalListener
     */
    public void addRemovalListener(ComponentRemovalListener<ComponentBuilder> componentRemovalListener);

    /**
     * Removes a {@link ComponentRemovalListener} from this
     * {@link ComponentBuilder}.
     * 
     * @param componentRemovalListener
     * @return true if the listener was found and removed.
     */
    public boolean removeRemovalListener(ComponentRemovalListener<ComponentBuilder> componentRemovalListener);

    /**
     * Gets an {@link OutputDataStream} by name.
     * 
     * @param name
     * @return
     */
    public OutputDataStream getOutputDataStream(String name);

    /**
     * Gets an {@link OutputDataStream} by the reference to it's {@link Table}.
     * 
     * @param dataStreamTable
     */
    public OutputDataStream getOutputDataStream(Table dataStreamTable);

    /**
     * Gets the {@link OutputDataStream}s that are available for this component
     * 
     * @return
     */
    public List<OutputDataStream> getOutputDataStreams();

    /**
     * Determines if a particular {@link OutputDataStream} is currently being
     * consumed or not
     * 
     * @param outputDataStream
     * @return
     */
    public boolean isOutputDataStreamConsumed(OutputDataStream outputDataStream);

    /**
     * Gets (or creates if non-existing) a job builder for the consumption of a
     * particular {@link OutputDataStream}
     * 
     * @param outputDataStream
     * @return
     */
    public AnalysisJobBuilder getOutputDataStreamJobBuilder(OutputDataStream outputDataStream);

    /**
     * Gets (or creates if non-existing) a job builder for the consumption of a
     * particular {@link OutputDataStream} (referred to by name)
     * 
     * @param outputDataStreamName
     * @return
     */
    public AnalysisJobBuilder getOutputDataStreamJobBuilder(String outputDataStreamName);

    /**
     * Updates the {@link AnalysisJobBuilder} that this component belongs to.
     * 
     * @param analysisJobBuilder
     *            the new {@link }AnalysisJobBuilder}
     */
    public void setAnalysisJobBuilder(AnalysisJobBuilder analysisJobBuilder);
}
