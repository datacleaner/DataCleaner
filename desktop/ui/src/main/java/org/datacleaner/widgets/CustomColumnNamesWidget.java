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
package org.datacleaner.widgets;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.jdesktop.swingx.VerticalLayout;

public class CustomColumnNamesWidget {
    private final DCPanel _innerPanel;
    private final DCPanel _outerPanel;
    private final List<JButton> _buttons;

    public CustomColumnNamesWidget(List<String> columnNames) {
        _innerPanel = new DCPanel();
        _innerPanel.setLayout(new VerticalLayout(2));

        final JButton addButton = WidgetFactory.createSmallButton(IconUtils.ACTION_ADD_DARK);
        addButton.addActionListener(e -> addColumnName("", true));

        final JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE_DARK);
        removeButton.addActionListener(e -> {
            if (_innerPanel.getComponentCount() > 0) {
                removeColumnName();
                _innerPanel.updateUI();
            }
        });

        final DCPanel buttonPanel = new DCPanel();
        buttonPanel.setBorder(new EmptyBorder(0, 4, 0, 0));
        buttonPanel.setLayout(new VerticalLayout(2));
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);

        _buttons = Arrays.asList(addButton, removeButton);

        if (columnNames != null) {
            columnNames.forEach(columnName -> addColumnName(columnName, false));
        }

        _outerPanel = new DCPanel();
        _outerPanel.setLayout(new BorderLayout());

        _outerPanel.add(_innerPanel, BorderLayout.CENTER);
        _outerPanel.add(buttonPanel, BorderLayout.EAST);
    }

    private void addColumnName(String columnName, boolean updateUI) {
        final JTextField columnNameField = WidgetFactory.createTextField();
        if (columnName != null) {
            columnNameField.setText(columnName);
        }

        _innerPanel.add(columnNameField);
        if (updateUI) {
            _innerPanel.updateUI();
        }
    }

    private void removeColumnName() {
        final int componentCount = _innerPanel.getComponentCount();
        if (componentCount > 0) {
            _innerPanel.remove(componentCount - 1);
        }
    }

    public List<String> getColumnNames() {
        return getColumnNameFields().stream().filter(field -> field.getText().length() > 0).map(JTextField::getText)
                .collect(Collectors.toList());
    }

    public List<JTextField> getColumnNameFields() {
        return Stream.of(_innerPanel.getComponents()).map(component -> (JTextField) component).collect(Collectors
                .toList());
    }

    public List<JButton> getButtons() {
        return _buttons;
    }

    public DCPanel getPanel() {
        return _outerPanel;
    }
}
