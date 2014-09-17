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
package org.eobjects.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.widgets.SourceColumnComboBox;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.jdesktop.swingx.VerticalLayout;

public class MultiSourceColumnComboBoxPanel extends DCPanel {

    private static final long serialVersionUID = 6598553122965748098L;
    private Datastore _datastore;
    private Table _table;
    private final DCPanel _sourceComboBoxPanel;
    private final DCPanel _buttonPanel;
    private final List<SourceColumnComboBox> _sourceColumnComboBoxes;

    public MultiSourceColumnComboBoxPanel() {
        _sourceComboBoxPanel = new DCPanel();
        _buttonPanel = new DCPanel();
        _sourceComboBoxPanel.setBorder(new EmptyBorder(0, 4, 0, 0));
        _sourceComboBoxPanel.setLayout(new VerticalLayout(2));
        _sourceColumnComboBoxes = new ArrayList<SourceColumnComboBox>();

        final JButton addButton = WidgetFactory.createSmallButton(IconUtils.ACTION_ADD);
        final JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE);

        _buttonPanel.setBorder(new EmptyBorder(0, 4, 0, 0));
        _buttonPanel.setLayout(new VerticalLayout(2));

        _buttonPanel.add(addButton);
        _buttonPanel.add(removeButton);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createSourceColumnComboBox(null);
            }

        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int componentCount = _sourceComboBoxPanel.getComponentCount();
                if (componentCount > 0) {
                    _sourceComboBoxPanel.remove(componentCount - 1);
                    _sourceComboBoxPanel.updateUI();
                }
            }
        });

        // add a single uninitialized combo box to begin with
        createSourceColumnComboBox(null);
    }

    private void createSourceColumnComboBox(Column column) {
        SourceColumnComboBox sourceColumnComboBox = (_table == null) ? new SourceColumnComboBox(_datastore)
                : new SourceColumnComboBox(_datastore, _table);
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
        DCPanel parentPanel = new DCPanel();
        parentPanel.setLayout(new BorderLayout());
        parentPanel.add(_sourceComboBoxPanel, BorderLayout.CENTER);
        parentPanel.add(_buttonPanel, BorderLayout.EAST);
        return parentPanel;
    }

    public void setModel(Datastore datastore) {
        _datastore = datastore;
        for (SourceColumnComboBox comboBox : _sourceColumnComboBoxes) {
            comboBox.setModel(datastore);
        }
    }

    /**
     * Returns the column names selected as String[].
     */
    public String[] getColumnNames() {
        List<Column> columns = getColumns();
        return convertToStringArry(columns);
    }

    private String[] convertToStringArry(List<Column> columns) {
        List<String> columnNamesAsString = new ArrayList<String>();
        for (Column column : columns) {
            columnNamesAsString.add(column.getQualifiedLabel());
        }
        return columnNamesAsString.toArray(new String[0]);
    }

    /**
     * Returns the columns selected as Column[].
     */
    public List<Column> getColumns() {
        List<Column> columns = new ArrayList<Column>();
        Component[] components = _sourceComboBoxPanel.getComponents();
        for (Component component : components) {
            if (component instanceof SourceColumnComboBox) {
                SourceColumnComboBox sourceColumnComboBox = (SourceColumnComboBox) component;
                columns.add(sourceColumnComboBox.getSelectedItem());
            }
        }
        return columns;
    }

    public void setColumns(Collection<Column> columns) {
        Iterator<Column> it = columns.iterator();
        Component[] components = _sourceComboBoxPanel.getComponents();
        for (Component component : components) {
            if (component instanceof SourceColumnComboBox) {
                if (!it.hasNext()) {
                    return;
                }

                Column column = it.next();
                SourceColumnComboBox sourceColumnComboBox = (SourceColumnComboBox) component;
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
    public void updateSourceComboBoxes(Datastore datastore, Table table) {
        _datastore = datastore;
        _table = table;
        for (SourceColumnComboBox sourceColComboBox : _sourceColumnComboBoxes) {
            sourceColComboBox.setModel(datastore, table);
        }
    }

}
