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
package org.datacleaner.panels.fuse;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.Func;
import org.datacleaner.actions.ReorderColumnsActionListener;
import org.datacleaner.api.InputColumn;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCCheckBox;
import org.datacleaner.widgets.DCLabel;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class StreamColumnListPanel extends DCPanel implements ReorderColumnsActionListener.ReorderColumnsCallback {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(StreamColumnListPanel.class);

    public static interface Listener {
        public void onValueChanged(StreamColumnListPanel panel);
    }

    private final Table _table;
    private final List<Listener> _listeners;
    private final DCPanel _selectedCheckboxesPanel;
    private final DCPanel _availableCheckboxesPanel;

    private DCPanel _buttonPanelForSelected;

    public StreamColumnListPanel(AnalysisJobBuilder rootAnalysisJobBuilder, Table table, Listener listener) {
        super();
        _table = table;
        _listeners = new ArrayList<>();
        _listeners.add(listener);

        setLayout(new VerticalLayout(0));

        _selectedCheckboxesPanel = new DCPanel();
        _selectedCheckboxesPanel.setLayout(new VerticalLayout(0));
        _selectedCheckboxesPanel.setBorder(WidgetUtils.BORDER_LIST_ITEM);
        _selectedCheckboxesPanel.setVisible(false);

        _availableCheckboxesPanel = new DCPanel();
        _availableCheckboxesPanel.setLayout(new VerticalLayout(0));

        final DCLabel streamHeader = DCLabel.dark(table.getName());
        streamHeader.setFont(WidgetUtils.FONT_SMALL.deriveFont(Font.BOLD));
        streamHeader.setBorder(WidgetUtils.BORDER_CHECKBOX_LIST_INDENTATION);
        add(streamHeader);

        final JButton reorderColumnsButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REORDER_COLUMNS);
        reorderColumnsButton.setToolTipText("Reorder columns");
        reorderColumnsButton.addActionListener(new ReorderColumnsActionListener(this));

        final JButton selectAllButton = WidgetFactory.createDefaultButton("Select all");
        selectAllButton.setFont(WidgetUtils.FONT_SMALL);
        selectAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (DCCheckBox<InputColumn<?>> checkBox : getAvailableInputColumnCheckBoxes()) {
                    checkBox.setSelected(true);
                }
                fireValueChanged();
            }
        });

        final JButton selectNoneButton = WidgetFactory.createDefaultButton("Select none");
        selectNoneButton.setFont(WidgetUtils.FONT_SMALL);
        selectNoneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (DCCheckBox<InputColumn<?>> checkBox : getSelectedInputColumnCheckBoxes()) {
                    checkBox.setSelected(false);
                }
                fireValueChanged();
            }
        });

        _buttonPanelForSelected = createButtonPanel();
        _buttonPanelForSelected.setVisible(false);
        _buttonPanelForSelected.add(reorderColumnsButton);
        _buttonPanelForSelected.add(selectNoneButton);

        // only show the "selected" button panel when there are any selections
        addListener(new Listener() {
            @Override
            public void onValueChanged(StreamColumnListPanel panel) {
                refresh();
            }
        });

        final DCPanel buttonPanelForAvailable = createButtonPanel();
        buttonPanelForAvailable.add(selectAllButton);

        add(_buttonPanelForSelected);
        add(_selectedCheckboxesPanel);

        add(buttonPanelForAvailable);
        add(_availableCheckboxesPanel);
    }

    public void addListener(Listener listener) {
        _listeners.add(listener);
    }

    private void fireValueChanged() {
        for (Listener listener : _listeners) {
            listener.onValueChanged(this);
        }
    }

    private DCPanel createButtonPanel() {
        final DCPanel buttonPanel = new DCPanel();
        buttonPanel.setLayout(new HorizontalLayout(2));
        buttonPanel.setBorder(new CompoundBorder(new EmptyBorder(4, 0, 0, 0),
                WidgetUtils.BORDER_CHECKBOX_LIST_INDENTATION));
        return buttonPanel;
    }

    protected List<DCCheckBox<InputColumn<?>>> getSelectedInputColumnCheckBoxes() {
        return getInputColumnCheckBoxes(_selectedCheckboxesPanel);
    }

    protected List<DCCheckBox<InputColumn<?>>> getAvailableInputColumnCheckBoxes() {
        return getInputColumnCheckBoxes(_availableCheckboxesPanel);
    }

    private List<DCCheckBox<InputColumn<?>>> getInputColumnCheckBoxes(DCPanel panel) {
        final Component[] components = panel.getComponents();
        return CollectionUtils.map(components, new Func<Component, DCCheckBox<InputColumn<?>>>() {
            @SuppressWarnings("unchecked")
            @Override
            public DCCheckBox<InputColumn<?>> eval(Component component) {
                return (DCCheckBox<InputColumn<?>>) component;
            }
        });
    }

    public void addInputColumn(InputColumn<?> inputColumn, boolean coalesced) {
        final DCCheckBox<InputColumn<?>> checkBox = new DCCheckBox<>(inputColumn.getName(), coalesced);
        checkBox.addListener(new DCCheckBox.Listener<InputColumn<?>>() {
            @Override
            public void onItemSelected(InputColumn<?> item, boolean selected) {
                if (selected) {
                    _availableCheckboxesPanel.remove(checkBox);
                    _selectedCheckboxesPanel.add(checkBox);
                } else {
                    _selectedCheckboxesPanel.remove(checkBox);
                    _availableCheckboxesPanel.add(checkBox);
                }
                fireValueChanged();
            }
        });
        checkBox.setValue(inputColumn);

        if (coalesced) {
            _selectedCheckboxesPanel.add(checkBox);
        } else {
            _availableCheckboxesPanel.add(checkBox);
        }
    }

    public List<InputColumn<?>> getAllInputColumns() {
        final List<InputColumn<?>> result = new ArrayList<>();
        for (DCCheckBox<InputColumn<?>> checkBox : getSelectedInputColumnCheckBoxes()) {
            result.add(checkBox.getValue());
        }
        for (DCCheckBox<InputColumn<?>> checkBox : getAvailableInputColumnCheckBoxes()) {
            result.add(checkBox.getValue());
        }
        return result;
    }

    public List<InputColumn<?>> getCoalescedInputColumns() {
        final List<InputColumn<?>> result = new ArrayList<>();
        for (DCCheckBox<InputColumn<?>> checkBox : getSelectedInputColumnCheckBoxes()) {
            result.add(checkBox.getValue());
        }
        return result;
    }

    public Table getTable() {
        return _table;
    }

    @Override
    public InputColumn<?>[] getColumns() {
        return getCoalescedInputColumns().toArray(new InputColumn[0]);
    }

    @Override
    public void reorderColumns(InputColumn<?>[] newValue) {
        final List<DCCheckBox<InputColumn<?>>> selectedInputColumnCheckBoxes = getSelectedInputColumnCheckBoxes();

        _selectedCheckboxesPanel.removeAll();
        for (InputColumn<?> inputColumn : newValue) {
            // find the corresponding checkbox and add it
            boolean found = false;
            for (int i = 0; i < selectedInputColumnCheckBoxes.size(); i++) {
                final DCCheckBox<InputColumn<?>> checkBox = selectedInputColumnCheckBoxes.get(i);
                if (checkBox.getValue().equals(inputColumn)) {
                    found = true;
                    _selectedCheckboxesPanel.add(checkBox);
                    break;
                }
            }
            if (!found) {
                logger.warn("Failed to find reordered column: {}", inputColumn);
            }
        }

        fireValueChanged();
    }

    public boolean hasSelectedInputColumns() {
        return _selectedCheckboxesPanel.getComponentCount() > 0;
    }

    public void refresh() {
        final boolean hasSelectedInputColumns = hasSelectedInputColumns();
        _buttonPanelForSelected.setVisible(hasSelectedInputColumns);
        _selectedCheckboxesPanel.setVisible(hasSelectedInputColumns);
    }
}
