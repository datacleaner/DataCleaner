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

import java.util.List;
import java.util.Objects;

import org.datacleaner.api.InputColumn;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.util.ReflectionUtils;

public class ComponentBuilderTransformerChangeListener implements TransformerChangeListener {
    private final ComponentBuilder _componentBuilder;
    private final ConfiguredPropertyDescriptor _propertyDescriptor;

    public ComponentBuilderTransformerChangeListener(final ComponentBuilder componentBuilder,
            final ConfiguredPropertyDescriptor propertyDescriptor) {
        _componentBuilder = componentBuilder;
        _propertyDescriptor = propertyDescriptor;
    }

    @Override
    public void onAdd(final TransformerComponentBuilder<?> builder) {
        // Do nothing.
    }

    @Override
    public void onConfigurationChanged(final TransformerComponentBuilder<?> builder) {
        synchronizeInputColumns(builder);
    }

    @Override
    public void onRequirementChanged(final TransformerComponentBuilder<?> builder) {
        // Do nothing.
    }

    @Override
    public void onRemove(final TransformerComponentBuilder<?> componentBuilder) {
        synchronizeInputColumns(componentBuilder);
    }

    @Override
    public void onOutputChanged(final TransformerComponentBuilder<?> transformerJobBuilder,
            final List<MutableInputColumn<?>> outputColumns) {
        synchronizeInputColumns(transformerJobBuilder);
    }

    private void synchronizeInputColumns(final TransformerComponentBuilder<?> changedBuilder) {
        if (!changedBuilder.equals(_componentBuilder)) {
            final List<InputColumn<?>> availableColumns = _componentBuilder.getAnalysisJobBuilder()
                    .getAvailableInputColumns(_componentBuilder, _propertyDescriptor.getTypeArgument(0));

            for (final ConfiguredPropertyDescriptor propertyDescriptor : _componentBuilder.getDescriptor()
                    .getConfiguredPropertiesForInput()) {
                final Object configuredProperty = _componentBuilder.getConfiguredProperty(propertyDescriptor);

                if (configuredProperty != null) {
                    if (ReflectionUtils.isInputColumn(configuredProperty.getClass())) {
                        if (ReflectionUtils.isArray(configuredProperty)) {
                            for (final InputColumn<?> column : (InputColumn<?>[]) configuredProperty) {
                                if (!availableColumns.contains(column)) {
                                    _componentBuilder.removeInputColumn(column, propertyDescriptor);
                                }
                            }
                        } else {
                            if (!availableColumns.contains(configuredProperty)) {
                                _componentBuilder
                                        .removeInputColumn((InputColumn<?>) configuredProperty, propertyDescriptor);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean equals(final Object object) {
        return (super.equals(object) || (object != null && object instanceof ComponentBuilderTransformerChangeListener
                && ((ComponentBuilderTransformerChangeListener) object)._componentBuilder == _componentBuilder
                && ((ComponentBuilderTransformerChangeListener) object)._propertyDescriptor == _propertyDescriptor));
    }

    @Override
    public int hashCode() {
        return Objects.hash(_componentBuilder, _propertyDescriptor);
    }
}
