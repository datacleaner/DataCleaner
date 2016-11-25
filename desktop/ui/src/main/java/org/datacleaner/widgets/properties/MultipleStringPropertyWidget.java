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
import java.awt.Component;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;

import org.apache.metamodel.util.EqualsBuilder;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

/**
 * {@link PropertyWidget} for String arrays. Displays string arrays as a set of
 * text boxes and plus/minus buttons to grow/shrink the array.
 */
public class MultipleStringPropertyWidget extends AbstractPropertyWidget<String[]> {

    private final DCPanel _textFieldPanel;
    private final Map<JComponent, JXTextField> _textFieldDecorations;

    @Inject
    public MultipleStringPropertyWidget(final ConfiguredPropertyDescriptor propertyDescriptor,
            final ComponentBuilder componentBuilder) {
        super(componentBuilder, propertyDescriptor);

        _textFieldDecorations = new IdentityHashMap<>();

        _textFieldPanel = new DCPanel();
        _textFieldPanel.setLayout(new VerticalLayout(2));

        final JButton addButton = WidgetFactory.createSmallButton(IconUtils.ACTION_ADD_DARK);
        addButton.addActionListener(e -> {
            addTextField("", true);
            fireValueChanged();
        });

        final JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE_DARK);
        removeButton.addActionListener(e -> {
            final int componentCount = _textFieldPanel.getComponentCount();
            if (componentCount > 0) {
                removeTextField();
                _textFieldPanel.updateUI();
                fireValueChanged();
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

    @Override
    public void initialize(final String[] value) {
        updateComponents(value);
    }

    public void updateComponents(String[] values) {
        if (values == null) {
            values = new String[2];
        }
        final String[] previousValues = getValue();
        if (!EqualsBuilder.equals(values, previousValues)) {
            for (int i = 0; i < Math.min(previousValues.length, values.length); i++) {
                // modify text boxes
                if (!EqualsBuilder.equals(previousValues[i], values[i])) {
                    final Component decoration = _textFieldPanel.getComponent(i);
                    final JXTextField component = _textFieldDecorations.get(decoration);
                    component.setText(values[i]);
                }
            }

            while (_textFieldPanel.getComponentCount() < values.length) {
                // add text boxes if there are too few
                final String nextValue = values[_textFieldPanel.getComponentCount()];
                addTextField(nextValue, false);
            }

            while (_textFieldPanel.getComponentCount() > values.length) {
                removeTextField();
            }
            _textFieldPanel.updateUI();
        }
    }

    private void removeTextField() {
        final int componentCount = _textFieldPanel.getComponentCount();
        if (componentCount == 0) {
            return;
        }
        final int index = componentCount - 1;
        final Component decoration = _textFieldPanel.getComponent(index);
        _textFieldDecorations.remove(decoration);
        _textFieldPanel.remove(index);
    }

    private void addTextField(final String value, final boolean updateUI) {
        final JXTextField textField = WidgetFactory.createTextField();
        if (value != null) {
            textField.setText(value);
        }
        textField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(final DocumentEvent e) {
                fireValueChanged();
            }
        });

        final int index = _textFieldPanel.getComponentCount();
        final JComponent decoration = decorateTextField(textField, index);
        _textFieldDecorations.put(decoration, textField);

        _textFieldPanel.add(decoration);
        if (updateUI) {
            _textFieldPanel.updateUI();
        }
    }

    protected JComponent decorateTextField(final JXTextField textField, final int index) {
        return textField;
    }

    @Override
    public String[] getValue() {
        final Component[] components = _textFieldPanel.getComponents();
        final List<String> result = new ArrayList<>();
        for (int i = 0; i < components.length; i++) {
            final Component decoration = components[i];
            final JXTextField textField = _textFieldDecorations.get(decoration);
            final String text = textField.getText();
            if (isEmptyStringValid() || text.length() != 0) {
                result.add(text);
            }
        }
        return result.toArray(new String[result.size()]);
    }

    @Override
    protected void setValue(final String[] value) {
        updateComponents(value);
    }

    @Override
    public boolean isSet() {
        final String[] value = getValue();
        if (value.length == 0) {
            return false;
        }

        if (!isEmptyStringValid()) {
            for (int i = 0; i < value.length; i++) {
                if (value[i].length() == 0) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Method to be overridden by subclasses in case empty strings inside the
     * arrays are not to be tolerated.
     *
     * @return
     */
    protected boolean isEmptyStringValid() {
        return true;
    }

}
