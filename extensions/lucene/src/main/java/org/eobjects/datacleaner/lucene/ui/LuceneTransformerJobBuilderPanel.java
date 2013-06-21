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

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.lucene.LuceneTransformer;
import org.eobjects.datacleaner.lucene.SearchIndex;
import org.eobjects.datacleaner.lucene.SearchIndexCatalog;
import org.eobjects.datacleaner.panels.TransformerJobBuilderPanel;
import org.eobjects.datacleaner.panels.TransformerJobBuilderPresenter;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * {@link TransformerJobBuilderPresenter} for Lucene transformers
 */
public class LuceneTransformerJobBuilderPanel extends TransformerJobBuilderPanel {

    private static final long serialVersionUID = 1L;

    private final Map<ConfiguredPropertyDescriptor, PropertyWidget<?>> _propertyWidgets;

    public LuceneTransformerJobBuilderPanel(TransformerJobBuilder<LuceneTransformer<?>> tjb,
            PropertyWidgetFactory propertyWidgetFactory, SearchIndexCatalog catalog, WindowContext windowContext,
            AnalyzerBeansConfiguration configuration, UserPreferences userPreferences) {
        super(Images.WATERMARK_IMAGE, 95, 95, tjb, windowContext, propertyWidgetFactory, configuration);

        _propertyWidgets = new IdentityHashMap<ConfiguredPropertyDescriptor, PropertyWidget<?>>();

        final Set<ConfiguredPropertyDescriptor> searchIndexProperties = tjb.getDescriptor()
                .getConfiguredPropertiesByType(SearchIndex.class, true);
        for (ConfiguredPropertyDescriptor property : searchIndexProperties) {
            PropertyWidget<?> widget;
            if (property.isArray()) {
                widget = new MultipleSearchIndicesPropertyWidget(tjb, property, catalog);
            } else {
                widget = new SingleSearchIndexPropertyWidget(tjb, property, catalog, windowContext, userPreferences);
            }
            _propertyWidgets.put(property, widget);
        }
    }

    @Override
    protected PropertyWidget<?> createPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
            ConfiguredPropertyDescriptor propertyDescriptor) {
        PropertyWidget<?> widget = _propertyWidgets.get(propertyDescriptor);
        if (widget != null) {
            return widget;
        }
        return super.createPropertyWidget(beanJobBuilder, propertyDescriptor);
    }
}
