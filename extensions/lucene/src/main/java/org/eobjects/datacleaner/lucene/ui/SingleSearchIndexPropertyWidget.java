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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.lucene.SearchIndex;
import org.eobjects.datacleaner.lucene.SearchIndexCatalog;
import org.eobjects.datacleaner.lucene.SearchIndexListener;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.widgets.DCComboBox;
import org.eobjects.datacleaner.widgets.DCListCellRenderer;
import org.eobjects.datacleaner.widgets.properties.AbstractPropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.jdesktop.swingx.HorizontalLayout;

/**
 * A {@link PropertyWidget} for selecting a {@link SearchIndex} in a combobox.
 */
public class SingleSearchIndexPropertyWidget extends AbstractPropertyWidget<SearchIndex> implements SearchIndexListener {

    public static interface Listener {
        public void onSearchIndexSelected(SearchIndex searchIndex);
    }

    private final DCComboBox<String> _comboBox;
    private final JButton _removeButton;
    private final JButton _createButton;
    private final JButton _editButton;
    private final SearchIndexCatalog _catalog;
    private final WindowContext _windowContext;
    private final UserPreferences _userPreferences;
    private final List<Listener> _listeners;

    public SingleSearchIndexPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
            ConfiguredPropertyDescriptor propertyDescriptor, SearchIndexCatalog catalog, WindowContext windowContext,
            UserPreferences userPreferences) {
        super(beanJobBuilder, propertyDescriptor);

        _listeners = new ArrayList<SingleSearchIndexPropertyWidget.Listener>();
        _catalog = catalog;
        _windowContext = windowContext;
        _userPreferences = userPreferences;

        final String[] names = catalog.getSearchIndexNames();
        _comboBox = new DCComboBox<String>(names);
        _comboBox.setRenderer(new DCListCellRenderer());

        final ImageIcon indexIcon = ImageManager.getInstance().getImageIcon("images/search_index.png",
                IconUtils.ICON_SIZE_MEDIUM, getClass().getClassLoader());
        _createButton = new JButton("Create", indexIcon);
        _createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final ConfigureSearchIndicesDialog dialog = new ConfigureSearchIndicesDialog(_windowContext, _catalog,
                        _userPreferences, _comboBox);
                dialog.open();
            }
        });

        _editButton = new JButton("Edit", indexIcon);
        _editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final SearchIndex searchIndex = getValue();
                if (searchIndex != null) {
                    final ConfigureSearchIndicesDialog dialog = new ConfigureSearchIndicesDialog(_windowContext,
                            _catalog, _userPreferences, _comboBox, searchIndex);
                    dialog.open();
                }
            }
        });

        _removeButton = WidgetFactory.createSmallButton("images/actions/remove.png");
        _removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                final String indexName = _comboBox.getSelectedItem();
                if (indexName == null) {
                    return;
                }
                final SearchIndex index = _catalog.getSearchIndex(indexName);
                if (index == null) {
                    return;
                }
                _catalog.removeSearchIndex(index);
            }
        });

        _comboBox.addListener(new DCComboBox.Listener<String>() {
            @Override
            public void onItemSelected(String item) {
                boolean itemSelected = item != null;
                _removeButton.setEnabled(itemSelected);
                _editButton.setEnabled(itemSelected);
                fireValueChanged();
                SearchIndex searchIndex = _catalog.getSearchIndex(item);
                notifyListeners(searchIndex);
            }
        });

        final SearchIndex currentValue = getCurrentValue();
        if (currentValue != null) {
            _comboBox.setSelectedItem(currentValue.getName());
        } else {
            _comboBox.setSelectedItem(null);
            _editButton.setEnabled(false);
            _removeButton.setEnabled(false);
        }

        final DCPanel panel = new DCPanel();
        panel.setLayout(new HorizontalLayout());
        panel.add(_comboBox);
        panel.add(Box.createHorizontalStrut(4));
        panel.add(_createButton);
        panel.add(Box.createHorizontalStrut(4));
        panel.add(_editButton);
        panel.add(Box.createHorizontalStrut(4));
        panel.add(_removeButton);
        add(panel);
    }
    
    public void addListener(Listener listener) {
        _listeners.add(listener);
    }
    
    public void removeListener(Listener listener) {
        _listeners.remove(listener);
    }

    private void notifyListeners(SearchIndex item) {
        for (Listener listener : _listeners) {
            listener.onSearchIndexSelected(item);
        }
    }

    @Override
    public SearchIndex getValue() {
        String name = _comboBox.getSelectedItem();
        return _catalog.getSearchIndex(name);
    }

    @Override
    protected void setValue(SearchIndex value) {
        if (value == null) {
            return;
        }
        _comboBox.setSelectedItem(value.getName());
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
        _comboBox.addItem(searchIndex.getName());
    }

    @Override
    public void onRemove(SearchIndex searchIndex) {
        _comboBox.removeItem(searchIndex.getName());
    }
}
