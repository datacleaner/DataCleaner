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
package org.datacleaner.panels.fuse;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.components.fuse.CoalesceMultipleFieldsTransformer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.panels.TransformerComponentBuilderPanel;
import org.datacleaner.panels.TransformerComponentBuilderPresenter;
import org.datacleaner.widgets.properties.PropertyWidget;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * Specialized {@link TransformerComponentBuilderPresenter} for the
 * {@link CoalesceMultipleFieldsTransformer}.
 */
final class CoalesceMultipleFieldsTransformerComponentBuilderPresenter extends TransformerComponentBuilderPanel {

    private static final long serialVersionUID = 1L;

    private final MultipleCoalesceUnitPropertyWidget _propertyWidget;
    private final ConfiguredPropertyDescriptor _inputProperty;
    private final ConfiguredPropertyDescriptor _unitsProperty;

    public CoalesceMultipleFieldsTransformerComponentBuilderPresenter(
            TransformerComponentBuilder<CoalesceMultipleFieldsTransformer> transformerJobBuilder,
            PropertyWidgetFactory propertyWidgetFactory, WindowContext windowContext,
            DataCleanerConfiguration configuration) {
        super(transformerJobBuilder, windowContext, propertyWidgetFactory, configuration);

        _inputProperty = transformerJobBuilder.getDescriptor().getConfiguredProperty("Input");
        _unitsProperty = transformerJobBuilder.getDescriptor().getConfiguredProperty("Units");

        _propertyWidget = new MultipleCoalesceUnitPropertyWidget(transformerJobBuilder, _inputProperty, _unitsProperty);
    }

    @Override
    protected PropertyWidget<?> createPropertyWidget(ComponentBuilder componentBuilder,
            ConfiguredPropertyDescriptor propertyDescriptor) {
        if (propertyDescriptor == _inputProperty) {
            return _propertyWidget;
        } else if (propertyDescriptor == _unitsProperty) {
            return _propertyWidget.getUnitPropertyWidget();
        }
        return super.createPropertyWidget(componentBuilder, propertyDescriptor);
    }
}
