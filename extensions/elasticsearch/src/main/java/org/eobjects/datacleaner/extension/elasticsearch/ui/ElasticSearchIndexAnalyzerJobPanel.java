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
package org.eobjects.datacleaner.extension.elasticsearch.ui;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.datacleaner.panels.AnalyzerJobBuilderPanel;
import org.eobjects.datacleaner.widgets.properties.MultipleMappedStringsPropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;

public class ElasticSearchIndexAnalyzerJobPanel extends AnalyzerJobBuilderPanel {

    private static final long serialVersionUID = 1L;

    private final MultipleMappedStringsPropertyWidget _mappedWidget;
    private final ConfiguredPropertyDescriptor _inputColumnsProperty;
    private final ConfiguredPropertyDescriptor _mappedStringsProperty;

    public ElasticSearchIndexAnalyzerJobPanel(AnalyzerJobBuilder<?> analyzerJobBuilder,
            PropertyWidgetFactory propertyWidgetFactory) {
        super(analyzerJobBuilder, propertyWidgetFactory);

        _inputColumnsProperty = analyzerJobBuilder.getDescriptor().getConfiguredProperty("Values");
        _mappedStringsProperty = analyzerJobBuilder.getDescriptor().getConfiguredProperty("Fields");

        _mappedWidget = new MultipleMappedStringsPropertyWidget(analyzerJobBuilder, _inputColumnsProperty,
                _mappedStringsProperty);
    }

    @Override
    protected PropertyWidget<?> createPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
            ConfiguredPropertyDescriptor propertyDescriptor) {
        if (propertyDescriptor == _inputColumnsProperty) {
            return _mappedWidget;
        } else if (propertyDescriptor == _mappedStringsProperty) {
            return _mappedWidget.getMappedStringsPropertyWidget();
        }
        return super.createPropertyWidget(beanJobBuilder, propertyDescriptor);
    }
}
