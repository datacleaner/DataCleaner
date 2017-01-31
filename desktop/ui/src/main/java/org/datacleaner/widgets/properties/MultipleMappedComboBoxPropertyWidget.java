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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.JComponent;
import javax.swing.ListCellRenderer;

import org.datacleaner.api.InputColumn;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.metadata.ColumnMeaningCollection;
import org.datacleaner.metadata.HasColumnMeaning;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.widgets.DCCheckBox;
import org.datacleaner.widgets.DCComboBox;
import org.datacleaner.widgets.EnumComboBoxListRenderer;

/**
 * A specialized property widget for multiple input columns that are mapped to string values.
 * This widget looks like the {@link MultipleInputColumnsPropertyWidget}, but is enhanced with combo boxes.
 */
public class MultipleMappedComboBoxPropertyWidget extends MultipleInputColumnsPropertyWidget {

    public class MappedComboBoxPropertyWidget extends MinimalPropertyWidget<Object[]> {

        private MultipleMappedComboBoxPropertyWidget _multipleMappedComboBoxPropertyWidget;

        public MappedComboBoxPropertyWidget(
                final MultipleMappedComboBoxPropertyWidget multipleMappedComboBoxPropertyWidget,
                final ComponentBuilder componentBuilder, final ConfiguredPropertyDescriptor propertyDescriptor) {
            super(componentBuilder, propertyDescriptor);
            _multipleMappedComboBoxPropertyWidget = multipleMappedComboBoxPropertyWidget;
        }

        @Override
        public JComponent getWidget() {
            return null; // do not return a visual widget
        }

        @Override
        public boolean isSet() {
            return _multipleMappedComboBoxPropertyWidget.isSet();
        }

        @Override
        public Object[] getValue() {
            final InputColumn<?>[] inputColumns = MultipleMappedComboBoxPropertyWidget.this.getValue();
            final List<HasColumnMeaning> result = new ArrayList<>();

            for (final InputColumn<?> inputColumn : inputColumns) {
                final DCComboBox<HasColumnMeaning> comboBox = _mappedComboBoxes.get(inputColumn);

                if (comboBox == null || !comboBox.isVisible()) {
                    result.add(null);
                } else {
                    result.add(comboBox.getSelectedItem());
                }
            }

            return result.toArray(new HasColumnMeaning[result.size()]);
        }

        @Override
        protected void setValue(final Object[] value) {
            final List<InputColumn<?>> inputColumns =
                    MultipleMappedComboBoxPropertyWidget.this.getSelectedInputColumns();

            for (int i = 0; i < inputColumns.size(); i++) {
                final HasColumnMeaning meaning;

                if (value != null && i < value.length) {
                    meaning = (HasColumnMeaning) value[i];
                } else {
                    meaning = null;
                }

                final InputColumn<?> inputColumn = inputColumns.get(i);
                final DCComboBox<HasColumnMeaning> comboBox = _mappedComboBoxes.get(inputColumn);

                if (meaning != null) {
                    comboBox.setVisible(true);
                    comboBox.setEditable(true);
                    comboBox.setSelectedItem(meaning);
                    comboBox.setEditable(false);
                }
            }
        }
    }

    private final Map<InputColumn<?>, DCComboBox<HasColumnMeaning>> _mappedComboBoxes;
    private final MappedComboBoxPropertyWidget _mappedComboBoxPropertyWidget;
    private final ConfiguredPropertyDescriptor _mappedColumnsProperty;
    private final ColumnMeaningCollection _availableColumnMeanings;

    public MultipleMappedComboBoxPropertyWidget(final ComponentBuilder componentBuilder,
            final ConfiguredPropertyDescriptor inputColumnsProperty,
            final ConfiguredPropertyDescriptor mappedColumnsProperty,
            final ColumnMeaningCollection availableColumnMeanings) {
        super(componentBuilder, inputColumnsProperty);

        _mappedComboBoxes = new WeakHashMap<>();
        _mappedColumnsProperty = mappedColumnsProperty;
        _availableColumnMeanings = availableColumnMeanings;
        _mappedComboBoxPropertyWidget =
                new MappedComboBoxPropertyWidget(this, componentBuilder, _mappedColumnsProperty);

        final InputColumn<?>[] currentValue = getCurrentValue();
        final HasColumnMeaning[] currentMappedValues = (HasColumnMeaning[]) _mappedComboBoxPropertyWidget.getValue();

        if (currentValue != null && currentMappedValues != null) {
            final int minLength = Math.min(currentValue.length, currentMappedValues.length);

            for (int i = 0; i < minLength; i++) {
                final InputColumn<?> inputColumn = currentValue[i];
                final HasColumnMeaning mappedValue = currentMappedValues[i];
                createComboBox(inputColumn, mappedValue);
            }
        }

        if (currentValue != null) {
            // Ticket #945 - this must happen AFTER creating the combo boxes (above)
            setValue(currentValue);
        }
    }

    public MappedComboBoxPropertyWidget getMappedComboBoxPropertyWidget() {
        return _mappedComboBoxPropertyWidget;
    }

    @Override
    protected boolean isAllInputColumnsSelectedIfNoValueExist() {
        return false;
    }

    protected DCComboBox<HasColumnMeaning> createComboBox(final InputColumn<?> inputColumn,
            final HasColumnMeaning mappedValue) {
        final HasColumnMeaning[] meanings = _availableColumnMeanings.getSortedColumnMeanings()
                .toArray(new HasColumnMeaning[_availableColumnMeanings.getColumnMeanings().size()]);
        final DCComboBox<HasColumnMeaning> comboBox = new DCComboBox<>(meanings);
        comboBox.setRenderer(getComboBoxRenderer(inputColumn, _mappedComboBoxes, meanings));
        _mappedComboBoxes.put(inputColumn, comboBox);

        if (mappedValue != null) {
            comboBox.setEditable(true);
            comboBox.setSelectedItem(mappedValue);
            comboBox.setEditable(false);
        }

        comboBox.addListener(item -> _mappedComboBoxPropertyWidget.fireValueChanged());

        return comboBox;
    }

    protected ListCellRenderer<? super HasColumnMeaning> getComboBoxRenderer(final InputColumn<?> inputColumn,
            final Map<InputColumn<?>, DCComboBox<HasColumnMeaning>> mappedComboBoxes,
            final HasColumnMeaning[] constants) {
        return new EnumComboBoxListRenderer();
    }

    @Override
    protected JComponent decorateCheckBox(final DCCheckBox<InputColumn<?>> checkBox) {
        final DCComboBox<HasColumnMeaning> comboBox;
        final InputColumn<?> inputColumn = checkBox.getValue();
        if (_mappedComboBoxes.containsKey(inputColumn)) {
            comboBox = _mappedComboBoxes.get(inputColumn);
        } else {
            comboBox = createComboBox(inputColumn, null);
        }
        checkBox.addListener((item, selected) -> {
            if (isBatchUpdating()) {
                return;
            }
            comboBox.setVisible(selected);
            _mappedComboBoxPropertyWidget.fireValueChanged();
        });

        final boolean selected = checkBox.isSelected();
        comboBox.setVisible(selected);

        final DCPanel panel = new DCPanel();
        panel.setLayout(new BorderLayout());
        panel.add(checkBox, BorderLayout.CENTER);
        panel.add(comboBox, BorderLayout.EAST);
        return panel;
    }

    @Override
    protected void onValuesBatchSelected(final List<InputColumn<?>> values) {
        final Collection<DCComboBox<HasColumnMeaning>> allComboBoxes = _mappedComboBoxes.values();
        for (final DCComboBox<HasColumnMeaning> comboBox : allComboBoxes) {
            comboBox.setVisible(false);
        }
        for (final InputColumn<?> inputColumn : values) {
            final DCComboBox<HasColumnMeaning> comboBox = _mappedComboBoxes.get(inputColumn);
            if (comboBox != null) {
                comboBox.setVisible(true);
            }
        }
    }

    @Override
    protected void onBatchFinished() {
        super.onBatchFinished();
        _mappedComboBoxPropertyWidget.fireValueChanged();
    }
}
