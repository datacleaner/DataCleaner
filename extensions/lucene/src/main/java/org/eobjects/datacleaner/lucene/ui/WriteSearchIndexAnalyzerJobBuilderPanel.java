/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.lucene.ui;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.lucene.SearchIndex;
import org.eobjects.datacleaner.lucene.SearchIndexCatalog;
import org.eobjects.datacleaner.panels.AnalyzerJobBuilderPanel;
import org.eobjects.datacleaner.widgets.properties.MultipleMappedStringsPropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;

public class WriteSearchIndexAnalyzerJobBuilderPanel extends AnalyzerJobBuilderPanel {

    private static final long serialVersionUID = 1L;

    private final ConfiguredPropertyDescriptor _fieldNamesProperty;
    private final ConfiguredPropertyDescriptor _inputColumnsProperty;
    private final MultipleMappedStringsPropertyWidget _mappedFieldsPropertyWidget;
    private final SearchIndexCatalog _catalog;
    private final WindowContext _windowContext;

    public WriteSearchIndexAnalyzerJobBuilderPanel(AnalyzerJobBuilder<?> analyzerJobBuilder,
            PropertyWidgetFactory propertyWidgetFactory, SearchIndexCatalog catalog, WindowContext windowContext) {
        super(analyzerJobBuilder, propertyWidgetFactory);

        _catalog = catalog;
        _windowContext = windowContext;

        _inputColumnsProperty = analyzerJobBuilder.getDescriptor()
                .getConfiguredPropertiesByType(InputColumn[].class, false).iterator().next();
        _fieldNamesProperty = analyzerJobBuilder.getDescriptor().getConfiguredPropertiesByType(String[].class, false)
                .iterator().next();

        _mappedFieldsPropertyWidget = new MultipleMappedStringsPropertyWidget(analyzerJobBuilder,
                _inputColumnsProperty, _fieldNamesProperty);
    }

    @Override
    protected PropertyWidget<?> createPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
            ConfiguredPropertyDescriptor propertyDescriptor) {
        if (propertyDescriptor == _inputColumnsProperty) {
            return _mappedFieldsPropertyWidget;
        } else if (propertyDescriptor == _fieldNamesProperty) {
            return _mappedFieldsPropertyWidget.getMappedStringsPropertyWidget();
        } else if (propertyDescriptor.getBaseType() == SearchIndex.class) {
            return new SearchIndexPropertyWidget(beanJobBuilder, propertyDescriptor, _catalog, _windowContext);
        }
        return super.createPropertyWidget(beanJobBuilder, propertyDescriptor);
    }
}
