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
package org.datacleaner.testtools.ui;

import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.AbstractBeanJobBuilder;
import org.datacleaner.job.builder.TransformerJobBuilder;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.panels.TransformerJobBuilderPanel;
import org.datacleaner.panels.TransformerJobBuilderPresenter;
import org.datacleaner.widgets.properties.MultipleMappedInputColumnsPropertyWidget;
import org.datacleaner.widgets.properties.PropertyWidget;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;


public class ColumnCompareTransformerPresenter extends TransformerJobBuilderPanel implements TransformerJobBuilderPresenter {

    private static final long serialVersionUID = 1L;

    private final ConfiguredPropertyDescriptor _testColumnsProperty;
    private final ConfiguredPropertyDescriptor _referenceColumnsProperty;

    private final MultipleMappedInputColumnsPropertyWidget
        _testColumnsPropertyWidget;

    public ColumnCompareTransformerPresenter(
            TransformerJobBuilder<?> transformerJobBuilder,
            WindowContext windowContext,
            PropertyWidgetFactory propertyWidgetFactory,
            AnalyzerBeansConfiguration configuration) {
        super(transformerJobBuilder, windowContext, propertyWidgetFactory,
                configuration);

        _testColumnsProperty = transformerJobBuilder.getDescriptor()
                .getConfiguredProperty("Test columns");
        _referenceColumnsProperty = transformerJobBuilder.getDescriptor()
                .getConfiguredProperty("Reference columns");

        _testColumnsPropertyWidget = new MultipleMappedInputColumnsPropertyWidget(
                transformerJobBuilder, _testColumnsProperty,
                _referenceColumnsProperty);
    }

    @Override
    protected PropertyWidget<?> createPropertyWidget(
            AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
            ConfiguredPropertyDescriptor propertyDescriptor) {
        if (propertyDescriptor == _testColumnsProperty) {
            return _testColumnsPropertyWidget;
        } else if (propertyDescriptor == _referenceColumnsProperty) {
            return _testColumnsPropertyWidget
                    .getMappedInputColumnsPropertyWidget();
        } else {
            return super.createPropertyWidget(beanJobBuilder,
                    propertyDescriptor);
        }
    }
}
