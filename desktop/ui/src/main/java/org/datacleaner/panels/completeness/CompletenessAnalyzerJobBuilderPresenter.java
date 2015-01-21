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
package org.datacleaner.panels.completeness;

import org.datacleaner.beans.CompletenessAnalyzer;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.panels.AnalyzerComponentBuilderPanel;
import org.datacleaner.panels.AnalyzerComponentBuilderPresenter;
import org.datacleaner.widgets.properties.MultipleMappedEnumsPropertyWidget;
import org.datacleaner.widgets.properties.PropertyWidget;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * Specialized {@link AnalyzerComponentBuilderPresenter} for the
 * {@link CompletenessAnalyzer}.
 */
final class CompletenessAnalyzerComponentBuilderPresenter extends AnalyzerComponentBuilderPanel {

    private static final long serialVersionUID = 1L;

    private final ConfiguredPropertyDescriptor _inputColumnProperty;
    private final ConfiguredPropertyDescriptor _conditionEnumProperty;
    private final MultipleMappedEnumsPropertyWidget<Enum<?>> _inputColumnMappingPropertyWidget;

    public CompletenessAnalyzerComponentBuilderPresenter(AnalyzerComponentBuilder<?> analyzerJobBuilder,
            PropertyWidgetFactory propertyWidgetFactory) {
        super(analyzerJobBuilder, propertyWidgetFactory);

        _inputColumnProperty = analyzerJobBuilder.getDescriptor().getConfiguredProperty("Values");
        _conditionEnumProperty = analyzerJobBuilder.getDescriptor().getConfiguredProperty("Conditions");

        _inputColumnMappingPropertyWidget = new MultipleMappedEnumsPropertyWidget<Enum<?>>(analyzerJobBuilder,
                _inputColumnProperty, _conditionEnumProperty);
    }

    @Override
    protected PropertyWidget<?> createPropertyWidget(ComponentBuilder componentBuilder,
            ConfiguredPropertyDescriptor propertyDescriptor) {
        if (propertyDescriptor == _inputColumnProperty) {
            return _inputColumnMappingPropertyWidget;
        } else if (propertyDescriptor == _conditionEnumProperty) {
            return _inputColumnMappingPropertyWidget.getMappedEnumsPropertyWidget();
        } else {
            return super.createPropertyWidget(componentBuilder, propertyDescriptor);
        }
    }
}
