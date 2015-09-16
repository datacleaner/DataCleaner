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
package org.datacleaner.panels.coalesce;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.datacleaner.api.InputColumn;
import org.datacleaner.components.fuse.CoalesceUnit;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCComboBox;
import org.datacleaner.widgets.SchemaStructureComboBoxListRenderer;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Panel that presents and edits a single {@link CoalesceUnit}.
 */
public class CoalesceUnitPanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private final MultipleCoalesceUnitPropertyWidget _parent;
    private final DCComboBox<InputColumn<?>> _comboBox;
    private final Map<InputColumn<?>, DCPanel> _inputColumnPanels;
    private final List<InputColumn<?>> _inputColumns;
    private final DCPanel _columnListPanel;
    private final DCPanel _outerPanel;

    public CoalesceUnitPanel(MultipleCoalesceUnitPropertyWidget parent, CoalesceUnit unit) {
        _parent = parent;
        _inputColumns = new ArrayList<InputColumn<?>>();
        _inputColumnPanels = new IdentityHashMap<InputColumn<?>, DCPanel>();

        _comboBox = new DCComboBox<InputColumn<?>>();
        SchemaStructureComboBoxListRenderer renderer = new SchemaStructureComboBoxListRenderer();
        renderer.setNullText("- Add input column -");
        _comboBox.setRenderer(renderer);
        _comboBox.addListener(new DCComboBox.Listener<InputColumn<?>>() {
            @Override
            public void onItemSelected(InputColumn<?> item) {
                if (item == null) {
                    return;
                }
                addInputColumn(item);
            }
        });

        _columnListPanel = new DCPanel();
        _columnListPanel.setLayout(new VerticalLayout(2));
        _columnListPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

        final DCPanel comboBoxPanel = DCPanel.around(_comboBox);
        comboBoxPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

        _outerPanel = new DCPanel();
        _outerPanel.setLayout(new BorderLayout());
        _outerPanel.add(_columnListPanel, BorderLayout.CENTER);
        _outerPanel.add(comboBoxPanel, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(WidgetUtils.decorateWithShadow(_outerPanel), BorderLayout.CENTER);

        List<InputColumn<?>> availableInputColumns = _parent.getAvailableInputColumns();
        setAvailableInputColumns(availableInputColumns);

        if (unit != null) {
            InputColumn<?>[] inputColumns = unit.getInputColumns(availableInputColumns
                    .toArray(new InputColumn[availableInputColumns.size()]));
            for (InputColumn<?> inputColumn : inputColumns) {
                addInputColumn(inputColumn);
            }
        }
    }

    /**
     * Called by the parent widget to update the list of available columns
     * 
     * @param inputColumns
     */
    public void setAvailableInputColumns(Collection<InputColumn<?>> inputColumns) {
        final InputColumn<?>[] items = new InputColumn<?>[inputColumns.size() + 1];
        int index = 1;
        for (InputColumn<?> inputColumn : inputColumns) {
            items[index] = inputColumn;
            index++;
        }
        final DefaultComboBoxModel<InputColumn<?>> model = new DefaultComboBoxModel<InputColumn<?>>(items);
        _comboBox.setModel(model);
    }

    public void addInputColumn(InputColumn<?> item) {
        final DCPanel panel = createInputColumnPanel(item);
        _columnListPanel.add(panel);

        _inputColumns.add(item);
        _parent.onInputColumnPicked(item);
    }

    public void removeInputColumn(InputColumn<?> item) {
        DCPanel panel = _inputColumnPanels.get(item);
        if (panel == null) {
            return;
        }
        _columnListPanel.remove(panel);

        _inputColumns.remove(item);
        _parent.onInputColumnReleased(item);
    }

    private DCPanel createInputColumnPanel(final InputColumn<?> item) {
        DCPanel panel = new DCPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel(item.getName(), IconUtils.getColumnIcon(item, IconUtils.ICON_SIZE_SMALL), JLabel.LEFT),
                BorderLayout.CENTER);
        JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE);
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeInputColumn(item);
            }
        });
        panel.add(removeButton, BorderLayout.EAST);

        _inputColumnPanels.put(item, panel);
        return panel;
    }

    public CoalesceUnitPanel(MultipleCoalesceUnitPropertyWidget parent) {
        this(parent, null);
    }

    public boolean isSet() {
        return !_inputColumns.isEmpty();
    }

    public CoalesceUnit getCoalesceUnit() {
        if (_inputColumns.isEmpty()) {
            return null;
        }
        return new CoalesceUnit(_inputColumns);
    }

}
