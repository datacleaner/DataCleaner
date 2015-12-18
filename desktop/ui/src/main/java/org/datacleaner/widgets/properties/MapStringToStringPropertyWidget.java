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
package org.datacleaner.widgets.properties;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;

import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

/**
 * {@link PropertyWidget} for {@link Map}s of string-to-string. Displays each
 * entry as a set of text boxes and plus/minus buttons to add/remove entries.
 */
public class MapStringToStringPropertyWidget extends AbstractPropertyWidget<Map<String, String>> {
    
    private final DCPanel _textFieldPanel;
    private final List<MapEntryStringStringPanel> _entryPanels;

    @Inject
    public MapStringToStringPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
            ComponentBuilder componentBuilder) {
        super(componentBuilder, propertyDescriptor);
        _entryPanels = new LinkedList<>();
        _textFieldPanel = new DCPanel();
        _textFieldPanel.setLayout(new VerticalLayout(2));

        final JButton addButton = WidgetFactory.createSmallButton(IconUtils.ACTION_ADD);
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addEntryPanel("", "", true);
                fireValueChanged();
            }
        });

        final JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE);
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final int componentCount = _textFieldPanel.getComponentCount();
                if (componentCount > 0) {
                    removeEntryPanel();
                    _textFieldPanel.updateUI();
                    fireValueChanged();
                }
            }
        });

        final DCPanel buttonPanel = new DCPanel();
        buttonPanel.setBorder(new EmptyBorder(0, 4, 0, 0));
        buttonPanel.setLayout(new VerticalLayout(2));
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);

        final DCPanel outerPanel = new DCPanel();
        outerPanel.setLayout(new BorderLayout());

        outerPanel.add(_textFieldPanel, BorderLayout.CENTER);
        outerPanel.add(buttonPanel, BorderLayout.EAST);

        add(outerPanel);
    }

    protected void addEntryPanel(String key, String value, boolean updateUI) {
        final MapEntryStringStringPanel entryPanel = new MapEntryStringStringPanel(key  , value);
        entryPanel.addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent e) {
                fireValueChanged();
            }
        });

        _entryPanels.add(entryPanel);
        _textFieldPanel.add(entryPanel);
        
        if (updateUI) {
            _textFieldPanel.updateUI();
        }
    }

    @Override
    public void initialize(Map<String, String> value) {
        updateComponents(value);
    }

    /**
     * Creates the initial map type to use. Subclasses can override this if they
     * want to enforce a specific implementation of {@link Map}.
     * 
     * By default a {@link LinkedHashMap} will be used since it has consistent
     * ordering of entries and thus provides the a consistent user experience
     * for most cases.
     * 
     * @return
     */
    public Map<String, String> createEmptyMap() {
        return new LinkedHashMap<>();
    }

    public void updateComponents(final Map<String, String> value) {
        if (value == null) {
            updateComponents(createEmptyMap());
            return;
        }
        batchUpdateWidget(new Runnable() {
            @Override
            public void run() {
                while (_entryPanels.size() > value.size()) {
                    // remove entry panels to make size equal
                    removeEntryPanel();
                }
                
                while (_entryPanels.size() < value.size()) {
                    // remove entry panels to make size equal
                    addEntryPanel("","", false);
                }
                
                // update all the panels
                int i = 0;
                final Set<Entry<String, String>> entries = value.entrySet();
                for (Entry<String, String> entry : entries) {
                    final MapEntryStringStringPanel entryPanel = _entryPanels.get(i);
                    entryPanel.setEntry(entry);
                    i++;
                }
            }
        });
        _textFieldPanel.updateUI();
    }

    private void removeEntryPanel() {
        final int componentCount = _textFieldPanel.getComponentCount();
        if (componentCount == 0) {
            return;
        }
        final int index = componentCount - 1;
        _entryPanels.remove(index);
        _textFieldPanel.remove(index);
    }

    protected JComponent decorateTextField(JXTextField textField, int index) {
        return textField;
    }

    @Override
    public Map<String, String> getValue() {
        final Map<String, String> result = createEmptyMap();
        for (MapEntryStringStringPanel panel : _entryPanels) {
            if (panel.isSet()) {
                result.put(panel.getEntryKey(), panel.getEntryValue());
            }
        }
        return result;
    }

    @Override
    public boolean isSet() {
        Map<String, String> value = getValue();
        if (value == null || value.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    protected void setValue(Map<String, String> value) {
        updateComponents(value);
    }

}
