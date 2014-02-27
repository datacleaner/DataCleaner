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
package org.eobjects.datacleaner.panels.coalesce;

import org.eobjects.analyzer.beans.coalesce.CoalesceMultipleFieldsTransformer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.TransformerJobBuilderPanel;
import org.eobjects.datacleaner.panels.TransformerJobBuilderPresenter;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * Specialized {@link TransformerJobBuilderPresenter} for the
 * {@link CoalesceMultipleFieldsTransformer}.
 */
final class CoalesceMultipleFieldsTransformerJobBuilderPresenter extends TransformerJobBuilderPanel {

    private static final long serialVersionUID = 1L;

    private final MultipleCoalesceUnitPropertyWidget _propertyWidget;
    private final ConfiguredPropertyDescriptor _inputProperty;
    private final ConfiguredPropertyDescriptor _unitsProperty;

    public CoalesceMultipleFieldsTransformerJobBuilderPresenter(
            TransformerJobBuilder<CoalesceMultipleFieldsTransformer> transformerJobBuilder,
            PropertyWidgetFactory propertyWidgetFactory, WindowContext windowContext,
            AnalyzerBeansConfiguration configuration) {
        super(transformerJobBuilder, windowContext, propertyWidgetFactory, configuration);

        _inputProperty = transformerJobBuilder.getDescriptor().getConfiguredProperty("Input");
        _unitsProperty = transformerJobBuilder.getDescriptor().getConfiguredProperty("Units");

        _propertyWidget = new MultipleCoalesceUnitPropertyWidget(transformerJobBuilder, _inputProperty, _unitsProperty);
    }

    @Override
    protected PropertyWidget<?> createPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
            ConfiguredPropertyDescriptor propertyDescriptor) {
        if (propertyDescriptor == _inputProperty) {
            return _propertyWidget;
        } else if (propertyDescriptor == _unitsProperty) {
            return _propertyWidget.getUnitPropertyWidget();
        }
        return super.createPropertyWidget(beanJobBuilder, propertyDescriptor);
    }
}
