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

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.MutableTable;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.EqualsBuilder;
import org.apache.metamodel.util.HasNameMapper;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Component;
import org.datacleaner.api.HasDistributionAdvice;
import org.datacleaner.api.HasOutputDataStreams;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.MappedProperty;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.api.Renderable;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.InjectionManager;
import org.datacleaner.connection.OutputDataStreamDatastore;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.data.TransformedInputColumn;
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
    private final List<OutputDataStream> _outputDataStreams = new ArrayList<>();
    private final Map<OutputDataStream, AnalysisJobBuilder> _outputDataStreamJobs = new HashMap<>();
    private final D _descriptor;
    private final E _configurableBean;
    private final Map<String, String> _metadataProperties;

    private AnalysisJobBuilder _analysisJobBuilder;
    private ComponentRequirement _componentRequirement;
    private String _name;

    public AbstractComponentBuilder(final AnalysisJobBuilder analysisJobBuilder, final D descriptor,
            final Class<?> builderClass) {
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

        _configurableBean = _descriptor.newInstance();
        _metadataProperties = new LinkedHashMap<>();
        _removalListeners = new ArrayList<>(1);
    }

    private static String getKey(final Object object) {
        if (object instanceof InputColumn<?>) {
            final InputColumn<?> inputColumn = (InputColumn<?>) object;

            if (inputColumn.isVirtualColumn()) {
                return inputColumn.getName();
            }
        }

        return String.valueOf(object.hashCode());
    }

    private static <E> E[] getArray(final Class<E> clazz, final List<?> baseList) {
        final E[] result = (E[]) Array.newInstance(clazz, baseList.size());

        for (int i = 0; i < baseList.size(); i++) {
            Array.set(result, i, (E) baseList.get(i));
        }

        return (E[]) result;
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

    @Override
    public void setMetadataProperties(final Map<String, String> metadataProperties) {
        _metadataProperties.clear();

        if (metadataProperties != null) {
            _metadataProperties.putAll(metadataProperties);
        }
    }

    /**
     * Gets a metadata property
     *
     * @param key
     * @return
     */
    @Override
    public final String getMetadataProperty(final String key) {
        return _metadataProperties.get(key);
    }

    /**
     * Sets a metadata property
     *
     * @param key
     * @param value
     */
    @Override
    public final void setMetadataProperty(final String key, final String value) {
        _metadataProperties.put(key, value);
    }

    /**
     * Removes/clears a metadata property
     *
     * @param key
     */
    @Override
    public final void removeMetadataProperty(final String key) {
        _metadataProperties.remove(key);
    }

    @Override
    public final AnalysisJobBuilder getAnalysisJobBuilder() {
        return _analysisJobBuilder;
    }

    @Override
    public void setAnalysisJobBuilder(final AnalysisJobBuilder analysisJobBuilder) {
        _analysisJobBuilder = analysisJobBuilder;
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
    public void setConfiguredProperties(final Map<ConfiguredPropertyDescriptor, Object> configuredProperties) {
        boolean changed = false;
        for (final Map.Entry<ConfiguredPropertyDescriptor, Object> entry : configuredProperties.entrySet()) {
            changed = setConfiguredPropertyIfChanged(entry.getKey(), entry.getValue()) || changed;
        }

        if (changed) {
            onConfigurationChanged();
        }
    }

    @Override
    public final boolean isConfigured(final boolean throwException)
            throws ComponentValidationException, UnconfiguredConfiguredPropertyException {
        for (final ConfiguredPropertyDescriptor configuredProperty : _descriptor.getConfiguredProperties()) {
            if (!isConfigured(configuredProperty, throwException)) {
                if (throwException) {
                    throw new UnconfiguredConfiguredPropertyException(this, configuredProperty);
                } else {
                    return false;
                }
            }
        }

        try {
            final LifeCycleHelper lifeCycleHelper =
                    new LifeCycleHelper(_analysisJobBuilder.getConfiguration(), null, false);
            lifeCycleHelper.validate(getDescriptor(), getComponentInstance());
        } catch (final RuntimeException e) {
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
    public void setName(final String name) {
        _name = name;
    }

    @Override
    public boolean isConfigured() {
        return isConfigured(false);
    }

    @Override
    public boolean isDistributable() {
        if (getDescriptor().isDistributable()) {
            final Component component = getComponentInstanceForQuestioning();
            if (component instanceof HasDistributionAdvice) {
                return ((HasDistributionAdvice) component).isDistributable();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isConfigured(final ConfiguredPropertyDescriptor configuredProperty, final boolean throwException)
            throws UnconfiguredConfiguredPropertyException {
        if (configuredProperty.isRequired()) {
            final Map<ConfiguredPropertyDescriptor, Object> configuredProperties = getConfiguredProperties();
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
    public B setConfiguredProperty(final String configuredName, final Object value) {
        final ConfiguredPropertyDescriptor configuredProperty = _descriptor.getConfiguredProperty(configuredName);
        if (configuredProperty == null) {
            throw new IllegalArgumentException("No such configured property: " + configuredName);
        }
        return setConfiguredProperty(configuredProperty, value);
    }

    @Override
    public B setConfiguredProperty(final ConfiguredPropertyDescriptor configuredProperty, final Object value) {
        final boolean changed = setConfiguredPropertyIfChanged(configuredProperty, value);
        if (changed) {
            if (configuredProperty.isInputColumn()) {
                registerListenerIfLinkedToTransformer(configuredProperty, value);
            }

            onConfigurationChanged();
        }
        return (B) this;
    }

    protected void registerListenerIfLinkedToTransformer(final ConfiguredPropertyDescriptor configuredProperty,
            final Object value) {
        // Register change listener on all transformers providing values used for the input column.
        getTransformedInputColumns(value).forEach(
                transformedInputColumn -> getAnalysisJobBuilder().getTransformerComponentBuilders().stream()
                        .filter(transformer -> (isProvidingColumn(transformedInputColumn, transformer))).forEach(
                                transformer -> transformer.addChangeListener(
                                        new ComponentBuilderTransformerChangeListener(this, configuredProperty))));
    }

    protected boolean isProvidingColumn(final TransformedInputColumn<?> transformedInputColumn,
            final TransformerComponentBuilder<?> transformer) {
        for (final Object outputColumn : transformer.getOutputColumns()) {
            if (outputColumn.equals(transformedInputColumn)) {
                return true;
            }
        }
        return false;
    }

    private List<TransformedInputColumn<?>> getTransformedInputColumns(final Object value) {
        final List<TransformedInputColumn<?>> transformedInputColumns = new ArrayList<>();

        if (value != null) {
            if (value.getClass().isArray()) {
                for (int i = 0; i < Array.getLength(value); i++) {
                    final Object valuePart = Array.get(value, i);
                    if (valuePart != null && ReflectionUtils.is(valuePart.getClass(), TransformedInputColumn.class)) {
                        transformedInputColumns.add((TransformedInputColumn<?>) valuePart);
                    }
                }
            } else if (ReflectionUtils.is(value.getClass(), TransformedInputColumn.class)) {
                transformedInputColumns.add((TransformedInputColumn<?>) value);
            }
        }
        return transformedInputColumns;
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
                    final int length = Array.getLength(value);
                    for (int i = 0; i < length; i++) {
                        final Object valuePart = Array.get(value, i);
                        if (valuePart == null) {
                            logger.warn("Element no. {} in array (size {}) is null! Value passed to {}",
                                    new Object[] { i, length, configuredProperty });
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
                throw new IllegalArgumentException(
                        "Invalid value type: " + value.getClass().getName() + ", expected: " + configuredProperty
                                .getBaseType().getName());
            }
        }

        synchronizeDependentProperties(configuredProperty, value, currentValue);

        configuredProperty.setValue(_configurableBean, value);
        return true;
    }

    private void synchronizeDependentProperties(final ConfiguredPropertyDescriptor property, final Object newValue,
            final Object currentValue) {
        if (currentValue != null) {
            getDescriptor().getConfiguredPropertiesByAnnotation(MappedProperty.class).stream()
                    .filter(dependentProperty -> property.getName()
                            .equals(dependentProperty.getAnnotation(MappedProperty.class).value()))
                    .forEach(dependentProperty -> doSynchronizeProperties(newValue, currentValue, dependentProperty));
        }
    }

    private void doSynchronizeProperties(final Object newValue, final Object currentValue,
            final ConfiguredPropertyDescriptor property) {
        // In case the new value no longer contains everything in the original value,
        // the values in the dependent property referring to the removed values need
        // to be removed too.
        final Object dependentValue = property.getValue(_configurableBean);

        if (dependentValue != null) {
            // First build a list containing value and references tuples.
            final Map<String, Object> originalMappings = new HashMap<>();

            final List<Object> synchronizedDependents = new ArrayList<>();

            if (currentValue.getClass().isArray()) {
                for (int i = 0; i < Array.getLength(currentValue); i++) {
                    originalMappings.put(getKey(Array.get(currentValue, i)), Array.get(dependentValue, i));
                }

                for (int i = 0; i < Array.getLength(newValue); i++) {
                    synchronizedDependents.add(originalMappings.get(getKey(Array.get(newValue, i))));
                }

                property.setValue(_configurableBean, getArray(property.getBaseType(), synchronizedDependents));
            } else {
                if (newValue == null) {
                    property.setValue(_configurableBean, null);
                }
            }
        }
    }

    @Override
    public Map<ConfiguredPropertyDescriptor, Object> getConfiguredProperties() {
        final Map<ConfiguredPropertyDescriptor, Object> map = new HashMap<>();
        final Set<ConfiguredPropertyDescriptor> configuredProperties = getDescriptor().getConfiguredProperties();
        for (final ConfiguredPropertyDescriptor propertyDescriptor : configuredProperties) {
            final Object value = getConfiguredProperty(propertyDescriptor);
            if (value != null) {
                map.put(propertyDescriptor, value);
            }
        }
        return Collections.unmodifiableMap(map);
    }

    @Override
    public void setConfiguredProperties(final ComponentConfiguration configuration) {
        boolean changed = false;
        final Set<ConfiguredPropertyDescriptor> properties = getDescriptor().getConfiguredProperties();
        for (final ConfiguredPropertyDescriptor property : properties) {
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
    public Object getConfiguredProperty(final ConfiguredPropertyDescriptor propertyDescriptor) {
        return propertyDescriptor.getValue(getConfigurableBean());
    }

    /**
     * Removes/clears all input columns
     */
    @Override
    public void clearInputColumns() {
        final Set<ConfiguredPropertyDescriptor> configuredProperties =
                getDescriptor().getConfiguredPropertiesForInput();
        for (final ConfiguredPropertyDescriptor configuredProperty : configuredProperties) {
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
    public B addInputColumn(final InputColumn<?> inputColumn) throws IllegalArgumentException {
        final ConfiguredPropertyDescriptor propertyDescriptor = getDefaultConfiguredPropertyForInput();
        return addInputColumn(inputColumn, propertyDescriptor);
    }

    @Override
    public ConfiguredPropertyDescriptor getDefaultConfiguredPropertyForInput() throws UnsupportedOperationException {
        Collection<ConfiguredPropertyDescriptor> inputProperties =
                getDescriptor().getConfiguredPropertiesForInput(false);

        if (inputProperties.isEmpty()) {
            // if there are no required input columns, try optional input
            // columns
            inputProperties = getDescriptor().getConfiguredPropertiesForInput(true);
        }

        if (inputProperties.size() == 1) {
            return inputProperties.iterator().next();
        } else {
            throw new UnsupportedOperationException(
                    "There are " + inputProperties.size() + " named input columns in \"" + getDescriptor()
                            .getDisplayName() + "\", please specify which one to configure");
        }
    }

    // this is the main "addInputColumn" method that the other similar methods
    // delegate to
    @Override
    public B addInputColumn(final InputColumn<?> inputColumn, final ConfiguredPropertyDescriptor propertyDescriptor)
            throws IllegalArgumentException {
        if (propertyDescriptor == null || !propertyDescriptor.isInputColumn()) {
            throw new IllegalArgumentException("Property is not of InputColumn type: " + propertyDescriptor);
        }

        final Class<?> expectedDataType = propertyDescriptor.getTypeArgument(0);
        if (expectedDataType != null && expectedDataType != Object.class) {
            // check input column type parameter compatibility
            final Class<?> actualDataType = inputColumn.getDataType();
            if (!ReflectionUtils.is(actualDataType, expectedDataType, false)) {
                throw new IllegalArgumentException(
                        "Unsupported InputColumn type: " + actualDataType + ", expected: " + expectedDataType);
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
    public B addInputColumns(final Collection<? extends InputColumn<?>> inputColumns,
            final ConfiguredPropertyDescriptor propertyDescriptor) {
        if (propertyDescriptor == null || !propertyDescriptor.isInputColumn()) {
            throw new IllegalArgumentException("Property is not of InputColumn type: " + propertyDescriptor);
        }

        final Class<?> expectedDataType = propertyDescriptor.getTypeArgument(0);
        if (expectedDataType != null && expectedDataType != Object.class) {
            // check input column type parameter compatibility
            for (final InputColumn<?> inputColumn : inputColumns) {
                final Class<?> actualDataType = inputColumn.getDataType();
                if (!ReflectionUtils.is(actualDataType, expectedDataType, false)) {
                    throw new IllegalArgumentException(
                            "Unsupported InputColumn type: " + actualDataType + ", expected: " + expectedDataType);
                }
            }
        }

        Object newInputColumns = getConfiguredProperty(propertyDescriptor);
        if (newInputColumns == null) {
            if (propertyDescriptor.isArray()) {
                newInputColumns = inputColumns.toArray(new InputColumn[inputColumns.size()]);
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
            final InputColumn<?>[] asArray = inputColumns.toArray(new InputColumn[inputColumns.size()]);
            newInputColumns = CollectionUtils2.array(InputColumn.class, newInputColumns, asArray);
        }
        setConfiguredProperty(propertyDescriptor, newInputColumns);
        return (B) this;
    }

    @Override
    public B addInputColumns(final Collection<? extends InputColumn<?>> inputColumns) {
        final ConfiguredPropertyDescriptor propertyDescriptor = getDefaultConfiguredPropertyForInput();
        addInputColumns(inputColumns, propertyDescriptor);
        return (B) this;
    }

    @Override
    public B addInputColumns(final InputColumn<?>... inputColumns) {
        final List<InputColumn<?>> list = Arrays.asList(inputColumns);
        final ConfiguredPropertyDescriptor propertyDescriptor = getDefaultConfiguredPropertyForInput();
        addInputColumns(list, propertyDescriptor);
        return (B) this;
    }

    @Override
    public B removeInputColumn(final InputColumn<?> inputColumn) {
        final Set<ConfiguredPropertyDescriptor> propertyDescriptors = getDescriptor().getConfiguredPropertiesForInput();
        if (propertyDescriptors.size() == 1) {
            final ConfiguredPropertyDescriptor propertyDescriptor = propertyDescriptors.iterator().next();
            return removeInputColumn(inputColumn, propertyDescriptor);
        } else {
            throw new UnsupportedOperationException("There are " + propertyDescriptors.size()
                    + " named input columns, please specify which one to configure");
        }
    }

    @Override
    public B removeInputColumn(final InputColumn<?> inputColumn,
            final ConfiguredPropertyDescriptor propertyDescriptor) {
        Object inputColumns = getConfiguredProperty(propertyDescriptor);
        if (inputColumns != null) {
            if (inputColumns == inputColumn) {
                inputColumns = null;
            } else {
                if (inputColumns.getClass().isArray()) {
                    inputColumns = CollectionUtils.arrayRemove(inputColumns, inputColumn);

                    if (!propertyDescriptor.isArray() && Array.getLength(inputColumns) == 0) {
                        inputColumns = null;
                    }
                }
            }
            setConfiguredProperty(propertyDescriptor, inputColumns);
            propertyDescriptor.setValue(getComponentInstance(), inputColumns);
        }
        return (B) this;
    }

    public void setRequirement(final FilterComponentBuilder<?, ?> filterComponentBuilder, final String category) {
        if (filterComponentBuilder == this) {
            throw new IllegalArgumentException("Requirement source and sink cannot be the same");
        }
        final FilterOutcome filterOutcome = filterComponentBuilder.getFilterOutcome(category);
        if (filterOutcome == null) {
            throw new IllegalArgumentException("No such category found in available outcomes: " + category);
        }
        setRequirement(filterOutcome);
    }

    public void setRequirement(final FilterComponentBuilder<?, ?> filterComponentBuilder, final Enum<?> category) {
        if (filterComponentBuilder == this) {
            throw new IllegalArgumentException("Requirement source and sink cannot be the same");
        }
        final EnumSet<?> categories = filterComponentBuilder.getDescriptor().getOutcomeCategories();
        if (!categories.contains(category)) {
            throw new IllegalArgumentException("No such category found in available outcomes: " + category);
        }
        setRequirement(filterComponentBuilder.getFilterOutcome(category));
    }

    public void setRequirement(final FilterOutcome outcome) throws IllegalArgumentException {
        if (!validateRequirementCandidate(outcome)) {
            throw new IllegalArgumentException("Cyclic dependency detected when setting requirement: " + outcome);
        }

        if (outcome == null) {
            setComponentRequirement(null);
        } else if (outcome instanceof FilterOutcome) {
            setComponentRequirement(new SimpleComponentRequirement((FilterOutcome) outcome));
        } else {
            throw new IllegalArgumentException(
                    "Unsupported outcome type (use ComponentRequirement instead): " + outcome);
        }
    }

    public boolean validateRequirementSource(final HasFilterOutcomes outcomeSource) {
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
            final ComponentRequirement componentRequirement =
                    ((HasComponentRequirement) source).getComponentRequirement();
            if (componentRequirement != null) {
                final Collection<FilterOutcome> requirements = componentRequirement.getProcessingDependencies();
                for (final FilterOutcome transitiveRequirement : requirements) {
                    final boolean transitiveValidation = validateRequirementCandidate(transitiveRequirement);
                    if (!transitiveValidation) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public List<InputColumn<?>> getInputColumns() {
        final List<InputColumn<?>> result = new LinkedList<>();
        final Set<ConfiguredPropertyDescriptor> configuredPropertiesForInput =
                getDescriptor().getConfiguredPropertiesForInput();
        for (final ConfiguredPropertyDescriptor configuredProperty : configuredPropertiesForInput) {
            final Object inputColumns = getConfiguredProperty(configuredProperty);
            if (inputColumns != null) {
                if (inputColumns.getClass().isArray()) {
                    final int length = Array.getLength(inputColumns);
                    for (int i = 0; i < length; i++) {
                        final InputColumn<?> column = (InputColumn<?>) Array.get(inputColumns, i);
                        if (column == null) {
                            logger.warn("Element no. {} in array (size {}) is null! Value read from {}",
                                    new Object[] { i, length, configuredProperty });
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
    public void setComponentRequirement(final ComponentRequirement requirement) {
        if (!EqualsBuilder.equals(_componentRequirement, requirement)) {
            _componentRequirement = requirement;
            onRequirementChanged();
        }
    }

    @Override
    public InputColumn<?>[] getInput() {
        final List<InputColumn<?>> inputColumns = getInputColumns();
        return inputColumns.toArray(new InputColumn[inputColumns.size()]);
    }

    /**
     * Notification method invoked when this {@link ComponentBuilder} is
     * removed.
     */
    protected final void onRemoved() {
        onRemovedInternal();
        for (final ComponentRemovalListener<ComponentBuilder> removalListener : _removalListeners) {
            removalListener.onRemove(this);
        }
    }

    protected abstract void onRemovedInternal();

    @Override
    public void addRemovalListener(final ComponentRemovalListener<ComponentBuilder> componentRemovalListener) {
        _removalListeners.add(componentRemovalListener);
    }

    @Override
    public boolean removeRemovalListener(final ComponentRemovalListener<ComponentBuilder> componentRemovalListener) {
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
        final InjectionManager injectionManager =
                configuration.getEnvironment().getInjectionManagerFactory().getInjectionManager(configuration);

        final LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(injectionManager, false);

        // mimic the configuration of a real component instance
        final ComponentConfiguration beanConfiguration =
                new ImmutableComponentConfiguration(getConfiguredPropertiesForQuestioning());
        lifeCycleHelper.assignConfiguredProperties(descriptor, component, beanConfiguration);
        lifeCycleHelper.assignProvidedProperties(descriptor, component);

        try {
            // only validate, don't initialize
            lifeCycleHelper.validate(descriptor, component);
        } catch (final RuntimeException e) {
            return null;
        }
        return component;
    }

    protected Map<ConfiguredPropertyDescriptor, Object> getConfiguredPropertiesForQuestioning() {
        return getConfiguredProperties();
    }

    @Override
    public AnalysisJobBuilder getOutputDataStreamJobBuilder(final String outputDataStreamName) {
        final OutputDataStream outputDataStream = getOutputDataStream(outputDataStreamName);
        if (outputDataStream == null) {
            throw new IllegalArgumentException("No such OutputDataStream: " + outputDataStreamName);
        }
        return getOutputDataStreamJobBuilder(outputDataStream);
    }

    @Override
    public AnalysisJobBuilder getOutputDataStreamJobBuilder(final OutputDataStream outputDataStream) {
        AnalysisJobBuilder analysisJobBuilder = _outputDataStreamJobs.get(outputDataStream);
        if (analysisJobBuilder == null) {
            assert _outputDataStreams.contains(outputDataStream);
            final Table table = outputDataStream.getTable();
            analysisJobBuilder = new AnalysisJobBuilder(_analysisJobBuilder.getConfiguration(), _analysisJobBuilder);
            analysisJobBuilder.setDatastore(new OutputDataStreamDatastore(outputDataStream));
            analysisJobBuilder.addSourceColumns(table.getColumns());

            _outputDataStreamJobs.put(outputDataStream, analysisJobBuilder);
        } else {
            final List<MetaModelInputColumn> sourceColumns = analysisJobBuilder.getSourceColumns();
            final List<String> sourceColumnsNames = new ArrayList<>(sourceColumns.size());
            for (int i = 0; i < sourceColumns.size(); i++) {
                sourceColumnsNames.add(sourceColumns.get(i).getName());
            }
            // If the one of the components has had changed output columns names it won't be visible
            // in the analysisJobBuilder's source columns represented by the outputStream. 
            // Therefore, we check if there are any changes in the name of the columns. see issue #1616(github).
            final Table table = outputDataStream.getTable();
            final List<String> outputStreamColumnNames = table.getColumnNames();
            if (!sourceColumnsNames.equals(outputStreamColumnNames)) {
                //avoid triggering listeners when the outputstream is consumed
                if (!isOutputDataStreamConsumed(outputDataStream)) {
                    for (int i = 0; i < sourceColumns.size(); i++) {
                        analysisJobBuilder.removeSourceColumn(sourceColumns.get(i));
                    }
                    //Add the new source columns
                    final List<Column> columns = table.getColumns();
                    analysisJobBuilder.addSourceColumns(columns);
                }
            }
        }
        return analysisJobBuilder;
    }

    @Override
    public OutputDataStream getOutputDataStream(final Table dataStreamTable) {
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
            final List<OutputDataStream> newStreams = Arrays.asList(outputDataStreams);
            if (!_outputDataStreams.equals(newStreams)) {
                final List<String> newNames = CollectionUtils.map(newStreams, new HasNameMapper());
                final List<String> existingNames = CollectionUtils.map(_outputDataStreams, new HasNameMapper());
                if (!newNames.equals(existingNames)) {
                    _outputDataStreams.clear();
                    _outputDataStreamJobs.clear();
                    _outputDataStreams.addAll(newStreams);
                } else {
                    // if the stream names are the same then it's better to see
                    // if we can incrementally update the existing streams
                    // instead of replacing it all
                    for (int i = 0; i < outputDataStreams.length; i++) {
                        final OutputDataStream existingStream = _outputDataStreams.get(i);
                        final Table table = existingStream.getTable();
                        final OutputDataStream newStream = newStreams.get(i);
                        if (table instanceof MutableTable) {
                            final MutableTable mutableTable = (MutableTable) table;
                            if (isOutputDataStreamConsumed(existingStream)) {
                                final AnalysisJobBuilder existingJobBuilder =
                                        getOutputDataStreamJobBuilder(existingStream);
                                // update the table
                                updateStream(mutableTable, existingJobBuilder, newStream);
                            } else {
                                updateStream(mutableTable, null, newStream);
                            }
                        } else {
                            _outputDataStreams.set(i, newStream);
                        }
                    }
                }
            }
            return new ArrayList<>(_outputDataStreams);
        }

        // component isn't capable of having output data streams
        return Collections.emptyList();
    }

    private void updateStream(final MutableTable existingTable, final AnalysisJobBuilder jobBuilder,
            final OutputDataStream newStream) {
        final List<Column> newColumnList = new ArrayList<>();
        final List<Column> addedColumns = new ArrayList<>();

        final Table newTable = newStream.getTable();

        int columnNumber = 0;
        for (final Column newColumn : newTable.getColumns()) {
            final Column existingColumn = existingTable.getColumnByName(newColumn.getName());
            final MutableColumn mutableColumn;

            if (existingColumn == null) {
                mutableColumn = (MutableColumn) newColumn;

                addedColumns.add(newColumn);
            } else {
                mutableColumn = (MutableColumn) existingColumn;

                // remove this so that it cannot be matched against in next
                // iterations
                existingTable.removeColumn(existingColumn);
            }

            // update the column to make sure everything is 100% matching
            mutableColumn.setTable(existingTable);
            mutableColumn.setColumnNumber(columnNumber);
            mutableColumn.setType(newColumn.getType());

            newColumnList.add(mutableColumn);

            columnNumber++;
        }

        if (jobBuilder != null) {
            // notify job builder of removed source columns
            final List<Column> currentColumns = new ArrayList<>(existingTable.getColumns());
            for (final Column column : currentColumns) {
                jobBuilder.removeSourceColumn(column);
            }
            // notify the job builder of added source columns
            for (final Column column : addedColumns) {
                jobBuilder.addSourceColumn(column);
            }
        }

        // update the table with the new set of columns
        existingTable.setColumns(newColumnList);
    }

    @Override
    public boolean isOutputDataStreamConsumed(final OutputDataStream outputDataStream) {
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
        for (final OutputDataStream outputDataStream : outputDataStreams) {
            if (isOutputDataStreamConsumed(outputDataStream)) {
                result.add(
                        new LazyOutputDataStreamJob(outputDataStream, getOutputDataStreamJobBuilder(outputDataStream)));
            }
        }
        return result.toArray(new OutputDataStreamJob[result.size()]);
    }
}
