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
package org.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.datacleaner.connection.Datastore;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.widgets.SourceColumnComboBox;
import org.jdesktop.swingx.VerticalLayout;

public class MultiSourceColumnComboBoxPanel extends DCPanel {

    private static final long serialVersionUID = 6598553122965748098L;
    private final DCPanel _sourceComboBoxPanel;
    private final DCPanel _buttonPanel;
    private final List<SourceColumnComboBox> _sourceColumnComboBoxes;
    private Datastore _datastore;
    private Table _table;

    public MultiSourceColumnComboBoxPanel() {
        _sourceComboBoxPanel = new DCPanel();
        _buttonPanel = new DCPanel();
        _sourceComboBoxPanel.setBorder(new EmptyBorder(0, 4, 0, 0));
        _sourceComboBoxPanel.setLayout(new VerticalLayout(2));
        _sourceColumnComboBoxes = new ArrayList<>();

        final JButton addButton = WidgetFactory.createSmallButton(IconUtils.ACTION_ADD_DARK);
        final JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE_DARK);

        _buttonPanel.setBorder(new EmptyBorder(0, 4, 0, 0));
        _buttonPanel.setLayout(new VerticalLayout(2));

        _buttonPanel.add(addButton);
        _buttonPanel.add(removeButton);

        addButton.addActionListener(e -> createSourceColumnComboBox(null));

        removeButton.addActionListener(e -> {
            final int componentCount = _sourceComboBoxPanel.getComponentCount();
            if (componentCount > 0) {
                _sourceComboBoxPanel.remove(componentCount - 1);
                _sourceComboBoxPanel.updateUI();
            }
        });

        // add a single uninitialized combo box to begin with
        createSourceColumnComboBox(null);
    }

    private void createSourceColumnComboBox(final Column column) {
        final SourceColumnComboBox sourceColumnComboBox =
                (_table == null) ? new SourceColumnComboBox(_datastore) : new SourceColumnComboBox(_datastore, _table);
        sourceColumnComboBox.setSelectedItem(column);
        _sourceColumnComboBoxes.add(sourceColumnComboBox);
        _sourceComboBoxPanel.add(sourceColumnComboBox);
        _sourceComboBoxPanel.updateUI();
    }

    /**
     * Creates a panel containing ButtonPanel and SourceComboboxPanel
     *
     * @return DCPanel
     */
    public DCPanel createPanel() {
        final DCPanel parentPanel = new DCPanel();
        parentPanel.setLayout(new BorderLayout());
        parentPanel.add(_sourceComboBoxPanel, BorderLayout.CENTER);
        parentPanel.add(_buttonPanel, BorderLayout.EAST);
        return parentPanel;
    }

    public void setModel(final Datastore datastore) {
        _datastore = datastore;
        for (final SourceColumnComboBox comboBox : _sourceColumnComboBoxes) {
            comboBox.setModel(datastore);
        }
    }

    /**
     * Returns the column names selected as String[].
     */
    public String[] getColumnNames() {
        final List<Column> columns = getColumns();
        return convertToStringArry(columns);
    }

    private String[] convertToStringArry(final List<Column> columns) {
        final List<String> columnNamesAsString = new ArrayList<>();
        for (final Column column : columns) {
            columnNamesAsString.add(column.getQualifiedLabel());
        }
        return columnNamesAsString.toArray(new String[0]);
    }

    /**
     * Returns the columns selected as Column[].
     */
    public List<Column> getColumns() {
        final List<Column> columns = new ArrayList<>();
        final Component[] components = _sourceComboBoxPanel.getComponents();
        for (final Component component : components) {
            if (component instanceof SourceColumnComboBox) {
                final SourceColumnComboBox sourceColumnComboBox = (SourceColumnComboBox) component;
                columns.add(sourceColumnComboBox.getSelectedItem());
            }
        }
        return columns;
    }

    public void setColumns(final Collection<Column> columns) {
        final Iterator<Column> it = columns.iterator();
        final Component[] components = _sourceComboBoxPanel.getComponents();
        for (final Component component : components) {
            if (component instanceof SourceColumnComboBox) {
                if (!it.hasNext()) {
                    return;
                }

                final Column column = it.next();
                final SourceColumnComboBox sourceColumnComboBox = (SourceColumnComboBox) component;
                sourceColumnComboBox.setSelectedItem(column);
            }
        }

        while (it.hasNext()) {
            createSourceColumnComboBox(it.next());
        }
    }

    /**
     * updates the SourceColumnComboBoxes with the provided datastore and table
     */
    public void updateSourceComboBoxes(final Datastore datastore, final Table table) {
        _datastore = datastore;
        _table = table;
        for (final SourceColumnComboBox sourceColComboBox : _sourceColumnComboBoxes) {
            sourceColComboBox.setModel(datastore, table);
        }
    }

}
