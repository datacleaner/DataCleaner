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
import java.util.List;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;

import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.widgets.CharTextField;
import org.jdesktop.swingx.VerticalLayout;

public class MultipleCharPropertyWidget extends AbstractPropertyWidget<char[]> {

    private final DCPanel _textFieldPanel;

    @Inject
    public MultipleCharPropertyWidget(final ConfiguredPropertyDescriptor propertyDescriptor,
            final ComponentBuilder componentBuilder) {
        super(componentBuilder, propertyDescriptor);

        _textFieldPanel = new DCPanel();
        _textFieldPanel.setLayout(new VerticalLayout(2));

        char[] currentValue = getCurrentValue();
        if (currentValue == null) {
            currentValue = new char[1];
        }
        updateComponents(currentValue);

        final JButton addButton = WidgetFactory.createSmallButton(IconUtils.ACTION_ADD_DARK);
        addButton.addActionListener(e -> {
            addCharField();
            fireValueChanged();
        });

        final JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE_DARK);
        removeButton.addActionListener(e -> {
            final int componentCount = _textFieldPanel.getComponentCount();
            if (componentCount > 0) {
                _textFieldPanel.remove(componentCount - 1);
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

    public void updateComponents(final char[] values) {
        _textFieldPanel.removeAll();
        if (values != null) {
            for (final char value : values) {
                addTextComponent(value);
            }
        }
    }

    private CharTextField addCharField() {
        final CharTextField textField = new CharTextField();
        textField.addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(final DocumentEvent event) {
                fireValueChanged();
            }
        });
        _textFieldPanel.add(textField);
        _textFieldPanel.updateUI();
        return textField;
    }

    private void addTextComponent(final char value) {
        addCharField().setValue(value);
    }

    @Override
    public char[] getValue() {
        final Component[] components = _textFieldPanel.getComponents();
        final List<Character> list = new ArrayList<>();
        for (int i = 0; i < components.length; i++) {
            final DCPanel panel = (DCPanel) components[i];
            final JTextComponent textComponent = (JTextComponent) panel.getComponent(0);
            final String text = textComponent.getText();
            if (text != null && text.length() == 1) {
                list.add(text.charAt(0));
            }
        }
        final char[] result = new char[list.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    @Override
    protected void setValue(final char[] value) {
        updateComponents(value);
    }

}
