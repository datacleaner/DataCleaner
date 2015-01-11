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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.EqualsBuilder;
import org.datacleaner.api.InputColumn;
import org.datacleaner.descriptors.BeanDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.HasComponentRequirement;
import org.datacleaner.job.HasFilterOutcomes;
import org.datacleaner.job.SimpleComponentRequirement;
import org.datacleaner.util.CollectionUtils2;
import org.datacleaner.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class AbstractBeanWithInputColumnsBuilder<D extends BeanDescriptor<E>, E, B extends ComponentBuilder> extends
        AbstractBeanJobBuilder<D, E, B> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractBeanWithInputColumnsBuilder.class);

    private ComponentRequirement _componentRequirement;

    public AbstractBeanWithInputColumnsBuilder(AnalysisJobBuilder analysisJobBuilder, D descriptor,
            Class<?> builderClass) {
        super(analysisJobBuilder, descriptor, builderClass);
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

    public void setRequirement(FilterJobBuilder<?, ?> filterJobBuilder, Enum<?> category) {
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

    /**
     * method that can be used by sub-classes to add callback logic when the
     * requirement of the bean changes
     */
    public void onRequirementChanged() {
    }

    public void setRequirement(FilterJobBuilder<?, ?> filterJobBuilder, String category) {
        EnumSet<?> categories = filterJobBuilder.getDescriptor().getOutcomeCategories();
        for (Enum<?> c : categories) {
            if (c.name().equals(category)) {
                setRequirement(filterJobBuilder.getFilterOutcome(c));
                return;
            }
        }
        throw new IllegalArgumentException("No such category found in available outcomes: " + category);
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
}
