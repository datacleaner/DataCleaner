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
package org.datacleaner.panels.tablelookup;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.EqualsBuilder;
import org.apache.metamodel.util.MutableRef;
import org.datacleaner.components.tablelookup.TableLookupTransformer;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.widgets.SourceColumnComboBox;
import org.datacleaner.widgets.properties.AbstractPropertyWidget;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Property widget for the {@link TableLookupTransformer}'s output columns
 * property, which is defined as a String array, but should be shown as a list
 * of {@link SourceColumnComboBox}es.
 *
 * @author Kasper Sørensen
 */
public class TableLookupOutputColumnsPropertyWidget extends AbstractPropertyWidget<String[]> {

    private final List<SourceColumnComboBox> _comboBoxes;
    private final MutableRef<Table> _tableRef;
    private final DCPanel _comboBoxPanel;

    public TableLookupOutputColumnsPropertyWidget(final ComponentBuilder componentBuilder,
            final ConfiguredPropertyDescriptor propertyDescriptor) {
        super(componentBuilder, propertyDescriptor);
        _comboBoxes = new ArrayList<>();

        _tableRef = new MutableRef<>();

        _comboBoxPanel = new DCPanel();
        _comboBoxPanel.setLayout(new VerticalLayout(2));

        final JButton addButton = WidgetFactory.createSmallButton(IconUtils.ACTION_ADD_DARK);
        addButton.addActionListener(e -> {
            addComboBox(null, true);
            fireValueChanged();
        });

        final JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE_DARK);
        removeButton.addActionListener(e -> {
            final int componentCount = _comboBoxPanel.getComponentCount();
            if (componentCount > 0) {
                removeComboBox();
                _comboBoxPanel.updateUI();
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

        outerPanel.add(_comboBoxPanel, BorderLayout.CENTER);
        outerPanel.add(buttonPanel, BorderLayout.EAST);

        add(outerPanel);

        final String[] currentValue = getCurrentValue();
        setValue(currentValue);
    }

    protected void addComboBox(final String value, final boolean updateUI) {
        final SourceColumnComboBox comboBox = new SourceColumnComboBox();
        final Column column;
        final Table table = _tableRef.get();
        if (value == null) {
            column = null;
        } else if (table == null) {
            column = new MutableColumn(value);
        } else {
            column = table.getColumnByName(value);
        }
        comboBox.setModel(table);

        comboBox.setEditable(true);
        comboBox.setSelectedItem(column);
        comboBox.setEditable(false);
        comboBox.addColumnSelectedListener(item -> fireValueChanged());

        _comboBoxes.add(comboBox);
        _comboBoxPanel.add(comboBox);

        if (updateUI) {
            _comboBoxPanel.updateUI();
        }
    }

    public void setTable(final Table table) {
        _tableRef.set(table);
        for (final SourceColumnComboBox comboBox : _comboBoxes) {
            comboBox.setModel(table);
        }
    }

    @Override
    public String[] getValue() {
        final List<String> result = new ArrayList<>();
        for (final SourceColumnComboBox comboBox : _comboBoxes) {
            final Column column = comboBox.getSelectedItem();
            if (column != null) {
                result.add(column.getName());
            }
        }
        return result.toArray(new String[result.size()]);
    }

    @Override
    protected void setValue(String[] values) {
        if (values == null || values.length == 0) {
            values = new String[1];
        }
        final String[] previousValues = getValue();
        if (!EqualsBuilder.equals(values, previousValues)) {
            for (int i = 0; i < Math.min(previousValues.length, values.length); i++) {
                // modify combo boxes
                if (!EqualsBuilder.equals(previousValues[i], values[i])) {
                    final SourceColumnComboBox comboBox = _comboBoxes.get(i);
                    comboBox.setEditable(true);
                    comboBox.setSelectedItem(values[i]);
                    comboBox.setEditable(false);
                }
            }

            while (_comboBoxes.size() < values.length) {
                // add combo boxes if there are too few
                final String nextValue = values[_comboBoxes.size()];
                addComboBox(nextValue, false);
            }

            while (_comboBoxes.size() > values.length) {
                // remove text boxes if there are too many
                removeComboBox();
            }
            _comboBoxPanel.updateUI();
        }
    }

    private void removeComboBox() {
        final int comboBoxIndex = _comboBoxes.size() - 1;
        _comboBoxes.remove(comboBoxIndex);
        _comboBoxPanel.remove(comboBoxIndex);
    }
}
