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
package org.eobjects.datacleaner.lucene.ui;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.lucene.SearchIndex;
import org.eobjects.datacleaner.lucene.SearchIndexCatalog;
import org.eobjects.datacleaner.panels.AnalyzerJobBuilderPanel;
import org.eobjects.datacleaner.panels.AnalyzerJobBuilderPresenter;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.widgets.properties.MultipleMappedStringsPropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * {@link AnalyzerJobBuilderPresenter} for Lucene analyzers
 */
public class WriteSearchIndexAnalyzerJobBuilderPanel extends AnalyzerJobBuilderPanel {

    private static final long serialVersionUID = 1L;

    private final ConfiguredPropertyDescriptor _fieldNamesProperty;
    private final ConfiguredPropertyDescriptor _inputColumnsProperty;
    private final MultipleMappedStringsPropertyWidget _mappedFieldsPropertyWidget;
    private final SearchIndexCatalog _catalog;
    private final WindowContext _windowContext;
    private final UserPreferences _userPreferences;

    public WriteSearchIndexAnalyzerJobBuilderPanel(AnalyzerJobBuilder<?> analyzerJobBuilder,
            PropertyWidgetFactory propertyWidgetFactory, SearchIndexCatalog catalog, WindowContext windowContext, UserPreferences userPreferences) {
        super(Images.WATERMARK_IMAGE, 95, 95, analyzerJobBuilder, true, propertyWidgetFactory);

        _catalog = catalog;
        _windowContext = windowContext;
        _userPreferences = userPreferences;

        _inputColumnsProperty = analyzerJobBuilder.getDescriptor()
                .getConfiguredPropertiesByType(InputColumn[].class, false).iterator().next();
        _fieldNamesProperty = analyzerJobBuilder.getDescriptor().getConfiguredPropertiesByType(String[].class, false)
                .iterator().next();

        _mappedFieldsPropertyWidget = new MultipleMappedStringsPropertyWidget(analyzerJobBuilder,
                _inputColumnsProperty, _fieldNamesProperty) {
            @Override
            protected String getDefaultMappedString(InputColumn<?> inputColumn) {
                return inputColumn.getName();
            }
        };
    }

    @Override
    protected PropertyWidget<?> createPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
            ConfiguredPropertyDescriptor propertyDescriptor) {
        if (propertyDescriptor == _inputColumnsProperty) {
            return _mappedFieldsPropertyWidget;
        } else if (propertyDescriptor == _fieldNamesProperty) {
            return _mappedFieldsPropertyWidget.getMappedStringsPropertyWidget();
        } else if (propertyDescriptor.getBaseType() == SearchIndex.class) {
            return new SingleSearchIndexPropertyWidget(beanJobBuilder, propertyDescriptor, _catalog, _windowContext, _userPreferences);
        }
        return super.createPropertyWidget(beanJobBuilder, propertyDescriptor);
    }
}
