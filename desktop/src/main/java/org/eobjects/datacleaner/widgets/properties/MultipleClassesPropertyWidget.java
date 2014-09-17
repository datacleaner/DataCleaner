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
package org.eobjects.datacleaner.widgets.properties;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.widgets.DCComboBox;
import org.eobjects.datacleaner.widgets.DCComboBox.Listener;
import org.apache.metamodel.util.EqualsBuilder;
import org.jdesktop.swingx.VerticalLayout;

/**
 * {@link PropertyWidget} for Class arrays. Displays class options as a set of
 * comboboxes and plus/minus buttons to grow/shrink the array.
 */
public class MultipleClassesPropertyWidget extends AbstractPropertyWidget<Class<?>[]> {

    private final DCPanel _outerPanel;
    private final Map<JComponent, DCComboBox<Class<?>>> _comboBoxDecorations;

    @Inject
    public MultipleClassesPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
            AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
        super(beanJobBuilder, propertyDescriptor);

        _comboBoxDecorations = new IdentityHashMap<JComponent, DCComboBox<Class<?>>>();

        _outerPanel = new DCPanel();
        _outerPanel.setLayout(new VerticalLayout(2));

        final JButton addButton = WidgetFactory.createSmallButton(IconUtils.ACTION_ADD);
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addComboBox(String.class, true);
                fireValueChanged();
            }
        });

        final JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE);
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int componentCount = _outerPanel.getComponentCount();
                if (componentCount > 0) {
                    removeComboBox();
                    _outerPanel.updateUI();
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

        outerPanel.add(_outerPanel, BorderLayout.CENTER);
        outerPanel.add(buttonPanel, BorderLayout.EAST);

        add(outerPanel);
    }

    @Override
    public void initialize(Class<?>[] value) {
        updateComponents(value);
    }

    public void updateComponents(Class<?>[] values) {
        if (values == null) {
            values = new Class[2];
        }
        final Class<?>[] previousValues = getValue();
        if (!EqualsBuilder.equals(values, previousValues)) {
            for (int i = 0; i < Math.min(previousValues.length, values.length); i++) {
                // modify comboboxes
                if (!EqualsBuilder.equals(previousValues[i], values[i])) {
                    Component decoration = _outerPanel.getComponent(i);
                    DCComboBox<Class<?>> component = _comboBoxDecorations.get(decoration);
                    component.setSelectedItem(values[i]);
                }
            }

            while (_outerPanel.getComponentCount() < values.length) {
                // add comboboxes if there are too few
                Class<?> nextValue = values[_outerPanel.getComponentCount()];
                addComboBox(nextValue, false);
            }

            while (_outerPanel.getComponentCount() > values.length) {
                removeComboBox();
            }
            _outerPanel.updateUI();
        }
    }

    private void removeComboBox() {
        int componentCount = _outerPanel.getComponentCount();
        if (componentCount == 0) {
            return;
        }
        int index = componentCount - 1;
        Component decoration = _outerPanel.getComponent(index);
        _comboBoxDecorations.remove(decoration);
        _outerPanel.remove(index);
    }

    private void addComboBox(Class<?> value, boolean updateUI) {
        DCComboBox<Class<?>> comboBox = SingleClassPropertyWidget.createClassComboBox(true);
        if (value != null) {
            comboBox.setSelectedItem(value);
        }
        comboBox.addListener(new Listener<Class<?>>() {
            @Override
            public void onItemSelected(Class<?> item) {
                fireValueChanged();
            }
        });

        final int index = _outerPanel.getComponentCount();
        final JComponent decoration = decorateComboBox(comboBox, index);
        _comboBoxDecorations.put(decoration, comboBox);

        _outerPanel.add(decoration);
        if (updateUI) {
            _outerPanel.updateUI();
        }
    }

    protected JComponent decorateComboBox(DCComboBox<Class<?>> comboBox, int index) {
        return comboBox;
    }

    @Override
    public Class<?>[] getValue() {
        Component[] components = _outerPanel.getComponents();
        List<Class<?>> result = new ArrayList<Class<?>>();
        for (int i = 0; i < components.length; i++) {
            final Component decoration = components[i];
            final DCComboBox<Class<?>> comboBox = _comboBoxDecorations.get(decoration);
            final Class<?> cls = comboBox.getSelectedItem();
            result.add(cls);
        }
        return result.toArray(new Class<?>[result.size()]);
    }

    @Override
    public boolean isSet() {
        Class<?>[] value = getValue();
        if (value.length == 0) {
            return false;
        }

        for (int i = 0; i < value.length; i++) {
            if (value[i] == null) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void setValue(Class<?>[] value) {
        updateComponents(value);
    }

}
