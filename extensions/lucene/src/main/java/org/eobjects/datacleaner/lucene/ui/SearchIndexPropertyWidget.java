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

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.lucene.SearchIndex;
import org.eobjects.datacleaner.lucene.SearchIndexCatalog;
import org.eobjects.datacleaner.widgets.DCComboBox;
import org.eobjects.datacleaner.widgets.properties.AbstractPropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;

/**
 * A {@link PropertyWidget} for selecting a {@link SearchIndex} in a combobox.
 */
public class SearchIndexPropertyWidget extends AbstractPropertyWidget<SearchIndex> {

    private final DCComboBox<String> _comboBox;
    private final SearchIndexCatalog _catalog;

    public SearchIndexPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
            ConfiguredPropertyDescriptor propertyDescriptor, SearchIndexCatalog catalog) {
        super(beanJobBuilder, propertyDescriptor);

        _catalog = catalog;

        final String[] names = catalog.getSearchIndexNames();
        _comboBox = new DCComboBox<String>(names);

        final SearchIndex currentValue = getCurrentValue();
        if (currentValue != null) {
            _comboBox.setSelectedItem(currentValue.getName());
        }

        add(_comboBox);
    }

    @Override
    public SearchIndex getValue() {
        String name = _comboBox.getSelectedItem();
        return _catalog.getSearchIndex(name);
    }

    @Override
    protected void setValue(SearchIndex value) {
        _comboBox.setSelectedItem(value.getName());
    }

}
