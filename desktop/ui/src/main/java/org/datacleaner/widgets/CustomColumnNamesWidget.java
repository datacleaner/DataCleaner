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
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

public class CustomColumnNamesWidget {
    private final DCPanel _innerPanel;
    private final DCPanel _outerPanel;

    public CustomColumnNamesWidget(List<String> columnNames) {
        _innerPanel = new DCPanel();
        _innerPanel.setLayout(new VerticalLayout(2));

        final JButton addButton = WidgetFactory.createSmallButton(IconUtils.ACTION_ADD_DARK);
        addButton.addActionListener(e -> addColumnName("", true));

        final JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE_DARK);
        removeButton.addActionListener(e -> {
            int componentCount = _innerPanel.getComponentCount();
            if (componentCount > 0) {
                removeColumnName();
                _innerPanel.updateUI();
            }
        });

        final DCPanel buttonPanel = new DCPanel();
        buttonPanel.setBorder(new EmptyBorder(0, 4, 0, 0));
        buttonPanel.setLayout(new VerticalLayout(2));
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);

        if (columnNames != null) {
            columnNames.forEach(columnName -> addColumnName(columnName, false));
        }

        _outerPanel = new DCPanel();
        _outerPanel.setLayout(new BorderLayout());

        _outerPanel.add(_innerPanel, BorderLayout.CENTER);
        _outerPanel.add(buttonPanel, BorderLayout.EAST);
    }

    private void addColumnName(String columnName, boolean updateUI) {
        JXTextField columnNameField = WidgetFactory.createTextField();
        if (columnName != null) {
            columnNameField.setText(columnName);
        }

        _innerPanel.add(columnNameField);
        if (updateUI) {
            _innerPanel.updateUI();
        }
    }

    private void removeColumnName() {
        int componentCount = _innerPanel.getComponentCount();
        if (componentCount > 0) {
            _innerPanel.remove(componentCount - 1);
        }
    }

    public List<String> getColumnNames() {
        List<String> columnNames = new ArrayList<String>();
        for (Component component : _innerPanel.getComponents()) {
            final String columnName = ((JXTextField) component).getText();
            if (columnName.length() != 0) {
                columnNames.add(columnName);
            }
        }
        return columnNames;
    }

    public DCPanel getPanel() {
        return _outerPanel;
    }
}
