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
package org.datacleaner.actions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.datacleaner.api.InputColumn;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.properties.MultipleInputColumnsPropertyWidget;
import org.datacleaner.widgets.table.DCTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReorderColumnsActionListener implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(ReorderColumnsActionListener.class);

    private final MultipleInputColumnsPropertyWidget _propertyWidget;

    public ReorderColumnsActionListener(MultipleInputColumnsPropertyWidget propertyWidget) {
        _propertyWidget = propertyWidget;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        InputColumn<?>[] currentValue = _propertyWidget.getValue();
        if (currentValue == null || currentValue.length == 0) {
            WidgetUtils.showErrorMessage("No columns selected", "Cannot reorder columns, when none is selected.");
            return;
        }
        if (currentValue.length < 2) {
            WidgetUtils.showErrorMessage("Nothing to reorder",
                    "You need to have at least two selected colummns to be able to reorder them.");
            return;
        }

        final List<InputColumn<?>> list = Arrays.asList(currentValue);
        logger.info("Selected columns before reordering: {}", list);

        final DCTable table = new DCTable();
        table.setRowHeight(22);

        updateTableModel(table, list);

        final Image image = ImageManager.get().getImage(IconUtils.ACTION_REORDER_COLUMNS);

        final DCPanel tablePanel = table.toPanel();
        tablePanel.setBorder(new CompoundBorder(WidgetUtils.BORDER_SHADOW, WidgetUtils.BORDER_THIN));

        final JDialog dialog = new JDialog();

        final JButton button = new JButton("Save order", new ImageIcon(image));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveReorderedValue(list);
                dialog.dispose();
            }
        });

        final DCPanel buttonPanel = new DCPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.add(button);

        final DCPanel panel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setLayout(new BorderLayout());
        panel.add(tablePanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setModal(true);
        dialog.setTitle("Reorder columns");
        dialog.setIconImage(image);
        dialog.getContentPane().add(panel);
        Dimension size = panel.getPreferredSize();
        size.width = Math.max(size.width, 300);
        size.height = Math.max(size.height, 400);
        dialog.setSize(size);
        WidgetUtils.centerOnScreen(dialog);
        dialog.setVisible(true);
    }

    private void updateTableModel(final DCTable table, final List<InputColumn<?>> list) {
        final String[] columnNames = new String[2];
        columnNames[0] = "Column name";
        columnNames[1] = "Move";

        final DefaultTableModel tableModel = new DefaultTableModel(columnNames, list.size());

        for (int i = 0; i < list.size(); i++) {
            final int index = i;

            final JButton moveDownButton = WidgetFactory.createSmallButton("/images/actions/move-down.png");
            moveDownButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    InputColumn<?> col1 = list.get(index);
                    InputColumn<?> col2 = list.get(index + 1);
                    list.set(index, col2);
                    list.set(index + 1, col1);
                    updateTableModel(table, list);
                }
            });

            final JButton moveUpButton = WidgetFactory.createSmallButton("/images/actions/move-up.png");
            moveUpButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    InputColumn<?> col1 = list.get(index);
                    InputColumn<?> col2 = list.get(index - 1);
                    list.set(index, col2);
                    list.set(index - 1, col1);
                    updateTableModel(table, list);
                }
            });

            final DCPanel buttonPanel = new DCPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
            if (i == list.size() - 1) {
                buttonPanel.add(Box.createHorizontalStrut(moveDownButton.getPreferredSize().width));
            } else {
                buttonPanel.add(moveDownButton);
            }
            buttonPanel.add(Box.createHorizontalStrut(6));
            if (i == 0) {
                buttonPanel.add(Box.createHorizontalStrut(moveUpButton.getPreferredSize().width));
            } else {
                buttonPanel.add(moveUpButton);
            }

            tableModel.setValueAt(list.get(i).getName(), i, 0);
            tableModel.setValueAt(buttonPanel, i, 1);
        }

        table.setModel(tableModel);
    }

    public void saveReorderedValue(List<InputColumn<?>> list) {
        logger.info("Saving reordered columns: {}", list);

        final InputColumn<?>[] newValue = list.toArray(new InputColumn[list.size()]);
        _propertyWidget.reorderValue(newValue);
    }
}