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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.lucene.SearchIndex;
import org.eobjects.datacleaner.lucene.SearchIndexCatalog;
import org.eobjects.datacleaner.lucene.SearchIndexListener;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.widgets.DCCheckBox;
import org.eobjects.datacleaner.widgets.properties.AbstractPropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.jdesktop.swingx.VerticalLayout;

/**
 * A {@link PropertyWidget} for selecting an array of {@link SearchIndex} in a
 * list of checkboxes.
 */
public class MultipleSearchIndicesPropertyWidget extends AbstractPropertyWidget<SearchIndex[]> implements
        SearchIndexListener, DCCheckBox.Listener<SearchIndex> {

    private final Map<String, DCCheckBox<SearchIndex>> _checkBoxes;
    private final SearchIndexCatalog _catalog;
    private DCPanel _panel;

    public MultipleSearchIndicesPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
            ConfiguredPropertyDescriptor propertyDescriptor, SearchIndexCatalog catalog) {
        super(beanJobBuilder, propertyDescriptor);

        _catalog = catalog;
        _panel = new DCPanel();
        _checkBoxes = new LinkedHashMap<String, DCCheckBox<SearchIndex>>();

        String[] searchIndexNames = catalog.getSearchIndexNames();
        for (String name : searchIndexNames) {
            SearchIndex searchIndex = catalog.getSearchIndex(name);
            onAdd(searchIndex);
        }
        catalog.addListener(this);

        final SearchIndex[] currentValue = getCurrentValue();
        setValue(currentValue);

        _panel.setLayout(new VerticalLayout());
        add(_panel);
    }

    @Override
    public SearchIndex[] getValue() {
        final List<SearchIndex> value = new ArrayList<SearchIndex>();
        final Collection<DCCheckBox<SearchIndex>> checkBoxes = _checkBoxes.values();
        for (DCCheckBox<SearchIndex> checkBox : checkBoxes) {
            if (checkBox.isSelected()) {
                value.add(checkBox.getValue());
            }
        }

        if (value.isEmpty()) {
            return null;
        }

        return value.toArray(new SearchIndex[value.size()]);
    }

    @Override
    protected void setValue(SearchIndex[] value) {
        if (value == null) {
            value = new SearchIndex[0];
        }

        final Set<Entry<String, DCCheckBox<SearchIndex>>> entrySet = _checkBoxes.entrySet();
        for (Entry<String, DCCheckBox<SearchIndex>> entry : entrySet) {
            String name = entry.getKey();
            DCCheckBox<SearchIndex> checkBox = entry.getValue();
            boolean selected = false;
            for (SearchIndex searchIndex : value) {
                if (name.equals(searchIndex.getName())) {
                    selected = true;
                    break;
                }
            }

            checkBox.setSelected(selected);
        }
    }

    @Override
    protected void onPanelAdd() {
        super.onPanelAdd();
        _catalog.addListener(this);
    }

    protected void onPanelRemove() {
        super.onPanelRemove();
        _catalog.removeListener(this);
    };

    @Override
    public void onAdd(SearchIndex searchIndex) {
        String name = searchIndex.getName();
        DCCheckBox<SearchIndex> checkBox = new DCCheckBox<SearchIndex>(name, false);
        checkBox.addListener(this);
        checkBox.setValue(searchIndex);
        _checkBoxes.put(name, checkBox);
        _panel.add(checkBox);
    }

    @Override
    public void onRemove(SearchIndex searchIndex) {
        String name = searchIndex.getName();
        DCCheckBox<SearchIndex> checkBox = _checkBoxes.remove(name);
        if (checkBox != null) {
            _panel.remove(checkBox);
        }
    }

    @Override
    public void onItemSelected(SearchIndex item, boolean selected) {
        fireValueChanged();
    }
}
