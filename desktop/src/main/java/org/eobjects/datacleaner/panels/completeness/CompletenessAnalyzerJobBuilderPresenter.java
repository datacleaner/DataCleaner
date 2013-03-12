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
package org.eobjects.datacleaner.panels.completeness;

import org.eobjects.analyzer.beans.CompletenessAnalyzer;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.datacleaner.panels.AnalyzerJobBuilderPanel;
import org.eobjects.datacleaner.panels.AnalyzerJobBuilderPresenter;
import org.eobjects.datacleaner.widgets.properties.MultipleMappedEnumsPropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * Specialized {@link AnalyzerJobBuilderPresenter} for the
 * {@link CompletenessAnalyzer}.
 */
final class CompletenessAnalyzerJobBuilderPresenter extends AnalyzerJobBuilderPanel {

    private static final long serialVersionUID = 1L;

    private final ConfiguredPropertyDescriptor _inputColumnProperty;
    private final ConfiguredPropertyDescriptor _conditionEnumProperty;
    private final MultipleMappedEnumsPropertyWidget<Enum<?>> _inputColumnMappingPropertyWidget;

    public CompletenessAnalyzerJobBuilderPresenter(AnalyzerJobBuilder<?> analyzerJobBuilder,
            PropertyWidgetFactory propertyWidgetFactory) {
        super(analyzerJobBuilder, true, propertyWidgetFactory);

        _inputColumnProperty = analyzerJobBuilder.getDescriptor().getConfiguredProperty("Values");
        _conditionEnumProperty = analyzerJobBuilder.getDescriptor().getConfiguredProperty("Conditions");

        _inputColumnMappingPropertyWidget = new MultipleMappedEnumsPropertyWidget<Enum<?>>(analyzerJobBuilder,
                _inputColumnProperty, _conditionEnumProperty);
    }

    @Override
    protected PropertyWidget<?> createPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
            ConfiguredPropertyDescriptor propertyDescriptor) {
        if (propertyDescriptor == _inputColumnProperty) {
            return _inputColumnMappingPropertyWidget;
        } else if (propertyDescriptor == _conditionEnumProperty) {
            return _inputColumnMappingPropertyWidget.getMappedEnumsPropertyWidget();
        } else {
            return super.createPropertyWidget(beanJobBuilder, propertyDescriptor);
        }
    }
}
