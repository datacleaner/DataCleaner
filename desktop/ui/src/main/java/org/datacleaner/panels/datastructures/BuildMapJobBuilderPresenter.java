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
package org.datacleaner.panels.datastructures;

import java.util.HashMap;
import java.util.Map;

import org.datacleaner.api.InputColumn;
import org.datacleaner.beans.datastructures.BuildMapTransformer;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.panels.TransformerComponentBuilderPanel;
import org.datacleaner.panels.TransformerComponentBuilderPresenter;
import org.datacleaner.widgets.properties.MultipleMappedStringsPropertyWidget;
import org.datacleaner.widgets.properties.PropertyWidget;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * Spectialized {@link TransformerComponentBuilderPresenter} for the
 * {@link BuildMapTransformer}.
 */
final class BuildMapJobBuilderPresenter extends TransformerComponentBuilderPanel {

    private static final long serialVersionUID = 1L;

    private final Map<ConfiguredPropertyDescriptor, PropertyWidget<?>> _overriddenPropertyWidgets;

    public BuildMapJobBuilderPresenter(TransformerComponentBuilder<BuildMapTransformer> tjb,
            WindowContext windowContext, PropertyWidgetFactory propertyWidgetFactory,
            DataCleanerConfiguration configuration) {
        super(tjb, windowContext, propertyWidgetFactory, configuration);

        _overriddenPropertyWidgets = new HashMap<ConfiguredPropertyDescriptor, PropertyWidget<?>>();

        final TransformerDescriptor<BuildMapTransformer> descriptor = tjb.getDescriptor();
        final ConfiguredPropertyDescriptor valuesProperty = descriptor.getConfiguredProperty("Values");
        final ConfiguredPropertyDescriptor keysProperty = descriptor.getConfiguredProperty("Keys");

        MultipleMappedStringsPropertyWidget propertyWidget = new MultipleMappedStringsPropertyWidget(tjb,
                valuesProperty, keysProperty) {
            @Override
            protected String getDefaultMappedString(InputColumn<?> inputColumn) {
                return inputColumn.getName();
            }
        };
        _overriddenPropertyWidgets.put(valuesProperty, propertyWidget);
        _overriddenPropertyWidgets.put(keysProperty, propertyWidget.getMappedStringsPropertyWidget());
    }

    @Override
    protected PropertyWidget<?> createPropertyWidget(ComponentBuilder componentBuilder,
            ConfiguredPropertyDescriptor propertyDescriptor) {
        if (_overriddenPropertyWidgets.containsKey(propertyDescriptor)) {
            return _overriddenPropertyWidgets.get(propertyDescriptor);
        }
        return super.createPropertyWidget(componentBuilder, propertyDescriptor);
    }
}
