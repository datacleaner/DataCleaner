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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;

import javax.swing.JComponent;

import org.datacleaner.api.InputColumn;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.metadata.ColumnMeaningCollection;
import org.datacleaner.metadata.HasColumnMeaning;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.widgets.DCCheckBox;
import org.datacleaner.widgets.DCGroupComboBox;

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
                final DCGroupComboBox comboBox = _mappedComboBoxes.get(inputColumn);

                if (comboBox == null || !comboBox.isVisible()) {
                    result.add(null);
                } else {
                    result.add((HasColumnMeaning) comboBox.getSelectedItem());
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
                final DCGroupComboBox comboBox = _mappedComboBoxes.get(inputColumn);

                if (meaning != null) {
                    comboBox.setVisible(true);
                    comboBox.setEditable(true);
                    comboBox.setSelectedItem(meaning);
                    comboBox.setEditable(false);
                }
            }
        }
    }

    private final Map<InputColumn<?>, DCGroupComboBox> _mappedComboBoxes;
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
    }

    public MappedComboBoxPropertyWidget getMappedComboBoxPropertyWidget() {
        return _mappedComboBoxPropertyWidget;
    }

    public Map<InputColumn<?>, DCGroupComboBox> getMappedComboBoxes() {
        return _mappedComboBoxes;
    }

    @Override
    protected boolean isAllInputColumnsSelectedIfNoValueExist() {
        return false;
    }

    protected DCGroupComboBox createComboBox(final InputColumn<?> inputColumn,
            final HasColumnMeaning mappedValue) {
        final DCGroupComboBox comboBox = new DCGroupComboBox();
        fillComboBox(comboBox);
        _mappedComboBoxes.put(inputColumn, comboBox);
        comboBox.setEditable(true);

        if (mappedValue == null) {
            comboBox.setSelectedItem(findMeaningByColumnName(inputColumn.getName()));
        } else {
            comboBox.setSelectedItem(mappedValue);
        }

        comboBox.setEditable(false);
        comboBox.addListener(item -> _mappedComboBoxPropertyWidget.fireValueChanged());

        return comboBox;
    }

    private void fillComboBox(final DCGroupComboBox comboBox) {
        final Map<String, Set<HasColumnMeaning>> groupedMeanings = getGroupedColumnMeanings();

        for (final String group : groupedMeanings.keySet()) {
            if (groupedMeanings.size() > 1) {
                comboBox.addDelimiter(group);
            }

            for (final HasColumnMeaning meaning : groupedMeanings.get(group)) {
                comboBox.addItem(meaning);
            }
        }
    }

    private HashMap<String, Set<HasColumnMeaning>> getGroupedColumnMeanings() {
        final HashMap<String, Set<HasColumnMeaning>> groupedMeanings = new HashMap<>();
        final String nullCategory = "Meanings";

        for (final HasColumnMeaning meaning : _availableColumnMeanings.getColumnMeanings()) {
            final String categoryName = meaning.getGroup() == null ? nullCategory : meaning.getGroup();

            if (groupedMeanings.containsKey(categoryName)) {
                groupedMeanings.get(categoryName).add(meaning);
            } else {
                final Set<HasColumnMeaning> categorySet =
                        new TreeSet<>(Comparator.comparing(HasColumnMeaning::getName, String.CASE_INSENSITIVE_ORDER));
                categorySet.add(meaning);
                groupedMeanings.put(categoryName, categorySet);
            }
        }

        return groupedMeanings;
    }

    private HasColumnMeaning findMeaningByColumnName(final String columnName) {
        final HasColumnMeaning meaning = _availableColumnMeanings.find(columnName);

        if (meaning == null) {
            return _availableColumnMeanings.getDefault();
        }

        return meaning;
    }

    @Override
    protected JComponent decorateCheckBox(final DCCheckBox<InputColumn<?>> checkBox) {
        final DCGroupComboBox comboBox;
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
        _mappedComboBoxes.values().forEach(cb -> cb.setVisible(false));
        values.stream().map(_mappedComboBoxes::get).filter(Objects::nonNull).forEach(cb -> cb.setVisible(true));
    }

    @Override
    protected void onBatchFinished() {
        super.onBatchFinished();
        _mappedComboBoxPropertyWidget.fireValueChanged();
    }
}
