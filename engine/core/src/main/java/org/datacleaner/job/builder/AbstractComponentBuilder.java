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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.EqualsBuilder;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Component;
import org.datacleaner.api.HasOutputDataStreams;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.api.Renderable;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.InjectionManager;
import org.datacleaner.connection.OutputDataStreamDatastore;
import org.datacleaner.descriptors.AnalyzerDescriptor;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentConfiguration;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.ComponentValidationException;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.HasComponentRequirement;
import org.datacleaner.job.HasFilterOutcomes;
import org.datacleaner.job.ImmutableComponentConfiguration;
import org.datacleaner.job.OutputDataStreamJob;
import org.datacleaner.job.SimpleComponentRequirement;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.util.CollectionUtils2;
import org.datacleaner.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract {@link ComponentBuilder} for components of a {@link AnalysisJob}.
 * 
 * @param <D>
 *            the component descriptor type (for instance
 *            {@link AnalyzerDescriptor})
 * @param <E>
 *            the actual component type (for instance {@link Analyzer})
 * @param <B>
 *            the concrete {@link ComponentBuilder} (for instance
 *            {@link AnalyzerComponentBuilder})
 */
@SuppressWarnings("unchecked")
public abstract class AbstractComponentBuilder<D extends ComponentDescriptor<E>, E extends Component, B extends ComponentBuilder>
        implements ComponentBuilder, Renderable {

    private static final Logger logger = LoggerFactory.getLogger(AbstractComponentBuilder.class);

    private final List<ComponentRemovalListener<ComponentBuilder>> _removalListeners;
    private final List<OutputDataStream> _outputDataStreams = new ArrayList<OutputDataStream>();
    private final Map<OutputDataStream, AnalysisJobBuilder> _outputDataStreamJobs = new HashMap<>();
    private final D _descriptor;
    private final E _configurableBean;
    private final Map<String, String> _metadataProperties;

    private AnalysisJobBuilder _analysisJobBuilder;
    private ComponentRequirement _componentRequirement;
    private String _name;

    public AbstractComponentBuilder(AnalysisJobBuilder analysisJobBuilder, D descriptor, Class<?> builderClass) {
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
        _removalListeners = new ArrayList<>(1);
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

    @Override
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
        final ImmutableComponentConfiguration beanConfiguration = new ImmutableComponentConfiguration(
                configuredPropeties);
        setConfiguredProperties(beanConfiguration);
    }

    @Override
    public void setConfiguredProperties(ComponentConfiguration configuration) {
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
    public final boolean isConfigured(boolean throwException) throws ComponentValidationException,
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
            LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(_analysisJobBuilder.getConfiguration(), null, false);
            lifeCycleHelper.validate(getDescriptor(), getComponentInstance());
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

    @Override
    public void setName(String name) {
        _name = name;
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
     * requirement of the bean changes
     */
    public void onRequirementChanged() {
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

    /**
     * Removes/clears all input columns
     */
    @Override
    public void clearInputColumns() {
        Set<ConfiguredPropertyDescriptor> configuredProperties = getDescriptor().getConfiguredPropertiesForInput();
        for (ConfiguredPropertyDescriptor configuredProperty : configuredProperties) {
            if (configuredProperty.isArray()) {
                setConfiguredProperty(configuredProperty, new InputColumn[0]);
            } else {
                setConfiguredProperty(configuredProperty, null);
            }
        }
    }

    /**
     * 
     * @param inputColumn
     * @throws IllegalArgumentException
     *             if the input column data type family doesn't match the types
     *             accepted by this transformer.
     */
    @Override
    public B addInputColumn(InputColumn<?> inputColumn) throws IllegalArgumentException {
        ConfiguredPropertyDescriptor propertyDescriptor = getDefaultConfiguredPropertyForInput();
        return addInputColumn(inputColumn, propertyDescriptor);
    }

    @Override
    public ConfiguredPropertyDescriptor getDefaultConfiguredPropertyForInput() throws UnsupportedOperationException {
        Collection<ConfiguredPropertyDescriptor> inputProperties = getDescriptor().getConfiguredPropertiesForInput(
                false);

        if (inputProperties.isEmpty()) {
            // if there are no required input columns, try optional input
            // columns
            inputProperties = getDescriptor().getConfiguredPropertiesForInput(true);
        }

        if (inputProperties.size() == 1) {
            ConfiguredPropertyDescriptor propertyDescriptor = inputProperties.iterator().next();
            return propertyDescriptor;
        } else {
            throw new UnsupportedOperationException("There are " + inputProperties.size()
                    + " named input columns in \"" + getDescriptor().getDisplayName()
                    + "\", please specify which one to configure");
        }
    }

    // this is the main "addInputColumn" method that the other similar methods
    // delegate to
    @Override
    public B addInputColumn(InputColumn<?> inputColumn, ConfiguredPropertyDescriptor propertyDescriptor)
            throws IllegalArgumentException {
        if (propertyDescriptor == null || !propertyDescriptor.isInputColumn()) {
            throw new IllegalArgumentException("Property is not of InputColumn type: " + propertyDescriptor);
        }

        final Class<?> expectedDataType = propertyDescriptor.getTypeArgument(0);
        if (expectedDataType != null && expectedDataType != Object.class) {
            // check input column type parameter compatibility
            final Class<?> actualDataType = inputColumn.getDataType();
            if (!ReflectionUtils.is(actualDataType, expectedDataType, false)) {
                throw new IllegalArgumentException("Unsupported InputColumn type: " + actualDataType + ", expected: "
                        + expectedDataType);
            }
        }

        Object inputColumns = getConfiguredProperty(propertyDescriptor);
        if (inputColumns == null) {
            if (propertyDescriptor.isArray()) {
                inputColumns = new InputColumn[] { inputColumn };
            } else {
                inputColumns = inputColumn;
            }
        } else {
            inputColumns = CollectionUtils2.array(InputColumn.class, inputColumns, inputColumn);
        }
        setConfiguredProperty(propertyDescriptor, inputColumns);
        return (B) this;
    }

    // this is the main "addInputColumns" method that the other similar methods
    // delegate to
    @Override
    public B addInputColumns(Collection<? extends InputColumn<?>> inputColumns,
            ConfiguredPropertyDescriptor propertyDescriptor) {
        if (propertyDescriptor == null || !propertyDescriptor.isInputColumn()) {
            throw new IllegalArgumentException("Property is not of InputColumn type: " + propertyDescriptor);
        }

        final Class<?> expectedDataType = propertyDescriptor.getTypeArgument(0);
        if (expectedDataType != null && expectedDataType != Object.class) {
            // check input column type parameter compatibility
            for (InputColumn<?> inputColumn : inputColumns) {
                final Class<?> actualDataType = inputColumn.getDataType();
                if (!ReflectionUtils.is(actualDataType, expectedDataType, false)) {
                    throw new IllegalArgumentException("Unsupported InputColumn type: " + actualDataType
                            + ", expected: " + expectedDataType);
                }
            }
        }

        Object newInputColumns = getConfiguredProperty(propertyDescriptor);
        if (newInputColumns == null) {
            if (propertyDescriptor.isArray()) {
                InputColumn<?>[] asArray = inputColumns.toArray(new InputColumn[inputColumns.size()]);
                newInputColumns = asArray;
            } else {
                if (inputColumns == null || inputColumns.isEmpty()) {
                    newInputColumns = null;
                } else if (inputColumns.size() > 1) {
                    throw new IllegalArgumentException(
                            "Property type is a single InputColumn, but a collection of more than one element was given");
                } else {
                    newInputColumns = inputColumns.iterator().next();
                }
            }
        } else {
            InputColumn<?>[] asArray = inputColumns.toArray(new InputColumn[inputColumns.size()]);
            newInputColumns = CollectionUtils2.array(InputColumn.class, newInputColumns, asArray);
        }
        setConfiguredProperty(propertyDescriptor, newInputColumns);
        return (B) this;
    }

    @Override
    public B addInputColumns(Collection<? extends InputColumn<?>> inputColumns) {
        ConfiguredPropertyDescriptor propertyDescriptor = getDefaultConfiguredPropertyForInput();
        addInputColumns(inputColumns, propertyDescriptor);
        return (B) this;
    }

    @Override
    public B addInputColumns(InputColumn<?>... inputColumns) {
        List<InputColumn<?>> list = Arrays.asList(inputColumns);
        ConfiguredPropertyDescriptor propertyDescriptor = getDefaultConfiguredPropertyForInput();
        addInputColumns(list, propertyDescriptor);
        return (B) this;
    }

    @Override
    public B removeInputColumn(InputColumn<?> inputColumn) {
        Set<ConfiguredPropertyDescriptor> propertyDescriptors = getDescriptor().getConfiguredPropertiesForInput();
        if (propertyDescriptors.size() == 1) {
            ConfiguredPropertyDescriptor propertyDescriptor = propertyDescriptors.iterator().next();
            return removeInputColumn(inputColumn, propertyDescriptor);
        } else {
            throw new UnsupportedOperationException("There are " + propertyDescriptors.size()
                    + " named input columns, please specify which one to configure");
        }
    }

    @Override
    public B removeInputColumn(InputColumn<?> inputColumn, ConfiguredPropertyDescriptor propertyDescriptor) {
        Object inputColumns = getConfiguredProperty(propertyDescriptor);
        if (inputColumns != null) {
            if (inputColumns == inputColumn) {
                inputColumns = null;
            } else {
                if (inputColumns.getClass().isArray()) {
                    inputColumns = CollectionUtils.arrayRemove(inputColumns, inputColumn);
                }
            }
            setConfiguredProperty(propertyDescriptor, inputColumns);
            propertyDescriptor.setValue(getComponentInstance(), inputColumns);
        }
        return (B) this;
    }

    public void setRequirement(FilterComponentBuilder<?, ?> filterComponentBuilder, String category) {
        final FilterOutcome filterOutcome = filterComponentBuilder.getFilterOutcome(category);
        if (filterOutcome == null) {
            throw new IllegalArgumentException("No such category found in available outcomes: " + category);
        }
        setRequirement(filterOutcome);
    }

    public void setRequirement(FilterComponentBuilder<?, ?> filterJobBuilder, Enum<?> category) {
        EnumSet<?> categories = filterJobBuilder.getDescriptor().getOutcomeCategories();
        if (!categories.contains(category)) {
            throw new IllegalArgumentException("No such category found in available outcomes: " + category);
        }
        setRequirement(filterJobBuilder.getFilterOutcome(category));
    }

    public void setRequirement(FilterOutcome outcome) throws IllegalArgumentException {
        if (!validateRequirementCandidate(outcome)) {
            throw new IllegalArgumentException("Cyclic dependency detected when setting requirement: " + outcome);
        }

        if (outcome == null) {
            setComponentRequirement(null);
        } else if (outcome instanceof FilterOutcome) {
            setComponentRequirement(new SimpleComponentRequirement((FilterOutcome) outcome));
        } else {
            throw new IllegalArgumentException("Unsupported outcome type (use ComponentRequirement instead): "
                    + outcome);
        }
    }

    @Override
    public void setComponentRequirement(final ComponentRequirement requirement) {
        if (!EqualsBuilder.equals(_componentRequirement, requirement)) {
            _componentRequirement = requirement;
            onRequirementChanged();
        }
    }

    public boolean validateRequirementSource(HasFilterOutcomes outcomeSource) {
        if (outcomeSource == null) {
            return true;
        }

        final Collection<FilterOutcome> outcomes = outcomeSource.getFilterOutcomes();
        if (outcomes == null || outcomes.isEmpty()) {
            return true;
        }

        final FilterOutcome firstOutcome = outcomes.iterator().next();
        return validateRequirementCandidate(firstOutcome);
    }

    public boolean validateRequirementCandidate(final ComponentRequirement requirement) {
        if (requirement instanceof SimpleComponentRequirement) {
            final SimpleComponentRequirement simpleComponentRequirement = (SimpleComponentRequirement) requirement;
            final FilterOutcome outcome = simpleComponentRequirement.getOutcome();
            return validateRequirementCandidate(outcome);
        }
        return true;
    }

    public boolean validateRequirementCandidate(final FilterOutcome requirement) {
        if (requirement == null) {
            return true;
        }
        final HasFilterOutcomes source = requirement.getSource();
        if (source == this) {
            return false;
        }
        if (source instanceof HasComponentRequirement) {
            final ComponentRequirement componentRequirement = ((HasComponentRequirement) source)
                    .getComponentRequirement();
            if (componentRequirement != null) {
                final Collection<FilterOutcome> requirements = componentRequirement.getProcessingDependencies();
                for (FilterOutcome transitiveRequirement : requirements) {
                    boolean transitiveValidation = validateRequirementCandidate(transitiveRequirement);
                    if (!transitiveValidation) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public List<InputColumn<?>> getInputColumns() {
        List<InputColumn<?>> result = new LinkedList<InputColumn<?>>();
        Set<ConfiguredPropertyDescriptor> configuredPropertiesForInput = getDescriptor()
                .getConfiguredPropertiesForInput();
        for (ConfiguredPropertyDescriptor configuredProperty : configuredPropertiesForInput) {
            Object inputColumns = getConfiguredProperty(configuredProperty);
            if (inputColumns != null) {
                if (inputColumns.getClass().isArray()) {
                    int length = Array.getLength(inputColumns);
                    for (int i = 0; i < length; i++) {
                        InputColumn<?> column = (InputColumn<?>) Array.get(inputColumns, i);
                        if (column == null) {
                            logger.warn("Element no. {} in array (size {}) is null! Value read from {}", new Object[] {
                                    i, length, configuredProperty });
                        } else {
                            result.add(column);
                        }
                    }
                } else {
                    result.add((InputColumn<?>) inputColumns);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public ComponentRequirement getComponentRequirement() {
        return _componentRequirement;
    }

    @Override
    public InputColumn<?>[] getInput() {
        List<InputColumn<?>> inputColumns = getInputColumns();
        return inputColumns.toArray(new InputColumn[inputColumns.size()]);
    }

    /**
     * Notification method invoked when this {@link ComponentBuilder} is
     * removed.
     */
    protected final void onRemoved() {
        onRemovedInternal();
        for (ComponentRemovalListener<ComponentBuilder> removalListener : _removalListeners) {
            removalListener.onRemove(this);
        }
    }

    protected abstract void onRemovedInternal();

    @Override
    public void addRemovalListener(ComponentRemovalListener<ComponentBuilder> componentRemovalListener) {
        _removalListeners.add(componentRemovalListener);
    }

    @Override
    public boolean removeRemovalListener(ComponentRemovalListener<ComponentBuilder> componentRemovalListener) {
        return _removalListeners.remove(componentRemovalListener);
    }

    protected Component getComponentInstanceForQuestioning() {
        if (!isConfigured()) {
            // as long as the component is not configured we cannot proceed
            return null;
        }

        final Component component = getComponentInstance();
        final D descriptor = getDescriptor();

        final DataCleanerConfiguration configuration = getAnalysisJobBuilder().getConfiguration();
        final InjectionManager injectionManager = configuration.getEnvironment().getInjectionManagerFactory()
                .getInjectionManager(configuration);

        final LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(injectionManager, null, false);

        // mimic the configuration of a real component instance
        final ComponentConfiguration beanConfiguration = new ImmutableComponentConfiguration(getConfiguredProperties());
        lifeCycleHelper.assignConfiguredProperties(descriptor, component, beanConfiguration);
        lifeCycleHelper.assignProvidedProperties(descriptor, component);

        // only validate, don't initialize
        lifeCycleHelper.validate(descriptor, component);

        return component;
    }

    @Override
    public AnalysisJobBuilder getOutputDataStreamJobBuilder(OutputDataStream outputDataStream) {
        AnalysisJobBuilder analysisJobBuilder = _outputDataStreamJobs.get(outputDataStream);
        if (analysisJobBuilder == null) {
            assert _outputDataStreams.contains(outputDataStream);

            final Table table = outputDataStream.getTable();

            analysisJobBuilder = new AnalysisJobBuilder(_analysisJobBuilder.getConfiguration());
            analysisJobBuilder.setDatastore(new OutputDataStreamDatastore(outputDataStream));
            analysisJobBuilder.addSourceColumns(table.getColumns());

            _outputDataStreamJobs.put(outputDataStream, analysisJobBuilder);
        }
        return analysisJobBuilder;
    }

    @Override
    public OutputDataStream getOutputDataStream(Table dataStreamTable) {
        if (dataStreamTable == null) {
            return null;
        }
        final List<OutputDataStream> streams = getOutputDataStreams();
        for (final OutputDataStream outputDataStream : streams) {
            if (dataStreamTable.equals(outputDataStream.getTable())) {
                return outputDataStream;
            }
        }
        return null;
    }

    @Override
    public OutputDataStream getOutputDataStream(final String name) {
        if (name == null) {
            return null;
        }
        final List<OutputDataStream> streams = getOutputDataStreams();
        for (final OutputDataStream outputDataStream : streams) {
            if (name.equals(outputDataStream.getName())) {
                return outputDataStream;
            }
        }
        return null;
    }

    @Override
    public List<OutputDataStream> getOutputDataStreams() {
        final Component component = getComponentInstanceForQuestioning();
        if (component == null) {
            // as long as the component is not configured, just return an
            // empty list
            return Collections.emptyList();
        }

        if (component instanceof HasOutputDataStreams) {
            final OutputDataStream[] outputDataStreams = ((HasOutputDataStreams) component).getOutputDataStreams();
            final List<OutputDataStream> newList = Arrays.asList(outputDataStreams);
            if (!_outputDataStreams.equals(newList)) {
                // This can be improved - maybe the list only slightly changes
                // in which case we would want to only change the elements that
                // are different. See
                // TransformerComponentBuilder.getOutputColumns() as
                // inspiration.
                _outputDataStreams.clear();
                _outputDataStreamJobs.clear();
                _outputDataStreams.addAll(newList);
            }
            return Collections.unmodifiableList(_outputDataStreams);
        }

        // component isn't capable of having output data streams
        return Collections.emptyList();
    }

    @Override
    public boolean isOutputDataStreamConsumed(OutputDataStream outputDataStream) {
        final AnalysisJobBuilder analysisJobBuilder = _outputDataStreamJobs.get(outputDataStream);
        if (analysisJobBuilder == null) {
            return false;
        }
        return analysisJobBuilder.getComponentCount() > 0;
    }

    @Override
    public OutputDataStreamJob[] getOutputDataStreamJobs() {
        final List<OutputDataStream> outputDataStreams = getOutputDataStreams();
        if (outputDataStreams == null || outputDataStreams.isEmpty()) {
            return new OutputDataStreamJob[0];
        }
        final List<OutputDataStreamJob> result = new ArrayList<>();
        for (OutputDataStream outputDataStream : outputDataStreams) {
            if (isOutputDataStreamConsumed(outputDataStream)) {
                result.add(new LazyOutputDataStreamJob(outputDataStream,
                        getOutputDataStreamJobBuilder(outputDataStream)));
            }
        }
        return result.toArray(new OutputDataStreamJob[result.size()]);
    }

    @Override
    public void setAnalysisJobBuilder(final AnalysisJobBuilder analysisJobBuilder) {
        _analysisJobBuilder = analysisJobBuilder;
    }
}
