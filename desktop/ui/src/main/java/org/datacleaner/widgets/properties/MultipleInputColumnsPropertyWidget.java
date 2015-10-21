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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.border.CompoundBorder;
import javax.swing.event.DocumentEvent;

import org.apache.commons.lang.ArrayUtils;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.HasNameMapper;
import org.datacleaner.actions.AddExpressionBasedColumnActionListener;
import org.datacleaner.actions.ReorderColumnsActionListener;
import org.datacleaner.api.ExpressionBasedInputColumn;
import org.datacleaner.api.InputColumn;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.SourceColumnChangeListener;
import org.datacleaner.job.builder.TransformerChangeListener;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCCheckBox;
import org.datacleaner.widgets.DCCheckBox.Listener;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Property widget for multiple input columns. Displays these as checkboxes.
 */
public class MultipleInputColumnsPropertyWidget extends AbstractPropertyWidget<InputColumn<?>[]> implements
        SourceColumnChangeListener, TransformerChangeListener, MutableInputColumn.Listener,
        ReorderColumnsActionListener.ReorderColumnsCallback {

    private static final Logger logger = LoggerFactory.getLogger(MultipleInputColumnsPropertyWidget.class);

    private final Listener<InputColumn<?>> checkBoxListener = new Listener<InputColumn<?>>() {
        @Override
        public void onItemSelected(InputColumn<?> item, boolean selected) {
            if (isBatchUpdating()) {
                return;
            }
            fireValueChanged();
        }
    };

    private final ActionListener selectAllActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            selectAll();
        }
    };

    private final ActionListener selectNoneActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            selectNone();
        }
    };

    private final Class<?> _dataType;
    private final Map<InputColumn<?>, DCCheckBox<InputColumn<?>>> _checkBoxes;
    private final Map<DCCheckBox<InputColumn<?>>, JComponent> _checkBoxDecorations;
    private final DCPanel _buttonPanel;
    private final JXTextField _searchDatastoreTextField;
    private final DCCheckBox<InputColumn<?>> _notAvailableCheckBox;

    private volatile boolean _firstUpdate;

    @Inject
    public MultipleInputColumnsPropertyWidget(ComponentBuilder componentBuilder,
            ConfiguredPropertyDescriptor propertyDescriptor) {
        super(componentBuilder, propertyDescriptor);
        // setBorder(WidgetUtils.BORDER_LIST_ITEM);
        _checkBoxes = new LinkedHashMap<>();
        _checkBoxDecorations = new IdentityHashMap<>();
        _firstUpdate = true;
        _dataType = propertyDescriptor.getTypeArgument(0);
        getAnalysisJobBuilder().addSourceColumnChangeListener(this);
        getAnalysisJobBuilder().addTransformerChangeListener(this);

        setLayout(new VerticalLayout(2));

        _searchDatastoreTextField = WidgetFactory.createTextField("Search/filter columns");
        _searchDatastoreTextField.setBorder(
                new CompoundBorder(WidgetUtils.BORDER_CHECKBOX_LIST_INDENTATION, WidgetUtils.BORDER_THIN));
        _searchDatastoreTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                String text = _searchDatastoreTextField.getText();
                if (StringUtils.isNullOrEmpty(text)) {
                    // when there is no search query, set all datastores
                    // visible
                    for (JCheckBox cb : _checkBoxes.values()) {
                        cb.setVisible(true);
                    }
                } else {
                    // do a case insensitive search
                    text = text.trim().toLowerCase();
                    for (JCheckBox cb : _checkBoxes.values()) {
                        String name = cb.getText().toLowerCase();
                        cb.setVisible(name.contains(text));
                    }
                }
            }
        });

        if (_dataType == null || _dataType == Object.class) {
            _notAvailableCheckBox = new DCCheckBox<>(
                    "<html><font color=\"gray\">- no columns available -</font></html>", false);
        } else {
            _notAvailableCheckBox = new DCCheckBox<>("<html><font color=\"gray\">- no <i>"
                    + LabelUtils.getDataTypeLabel(_dataType) + "</i> columns available -</font></html>", false);
        }
        _notAvailableCheckBox.setEnabled(false);

        _buttonPanel = new DCPanel();
        _buttonPanel.setLayout(new HorizontalLayout(2));
        _buttonPanel.setBorder(WidgetUtils.BORDER_CHECKBOX_LIST_INDENTATION);

        JButton selectAllButton = WidgetFactory.createDefaultButton("Select all");
        selectAllButton.setFont(WidgetUtils.FONT_SMALL);
        selectAllButton.addActionListener(selectAllActionListener);
        _buttonPanel.add(selectAllButton);

        JButton selectNoneButton = WidgetFactory.createDefaultButton("Select none");
        selectNoneButton.setFont(WidgetUtils.FONT_SMALL);
        selectNoneButton.addActionListener(selectNoneActionListener);
        _buttonPanel.add(selectNoneButton);

        if (propertyDescriptor.isArray()) {
            if (_dataType == String.class || _dataType == Object.class) {
                final JButton expressionColumnButton = WidgetFactory
                        .createSmallButton(IconUtils.MODEL_COLUMN_EXPRESSION);
                expressionColumnButton.setToolTipText("Create expression/value based column");
                expressionColumnButton.addActionListener(AddExpressionBasedColumnActionListener
                        .forMultipleColumns(this));
                _buttonPanel.add(expressionColumnButton);
            }

            final JButton reorderColumnsButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REORDER_COLUMNS);
            reorderColumnsButton.setToolTipText("Reorder columns");
            reorderColumnsButton.addActionListener(new ReorderColumnsActionListener(this));
            _buttonPanel.add(reorderColumnsButton);
        }

        add(_buttonPanel);
        add(_searchDatastoreTextField);
    }

    @Override
    public void initialize(final InputColumn<?>[] value) {
        updateComponents(value);
        _firstUpdate = false;
        if (value != null && value.length > 0) {

            // update selections in checkboxes
            batchUpdateWidget(new Runnable() {
                @Override
                public void run() {
                    reorderValue(value);
                    for (DCCheckBox<InputColumn<?>> cb : _checkBoxes.values()) {
                        if (ArrayUtils.contains(value, cb.getValue())) {
                            cb.setSelected(true);
                        } else {
                            cb.setSelected(false);
                        }
                    }
                    onValuesBatchSelected(Arrays.asList(value));
                }
            });
        }
    }

    protected boolean isAllInputColumnsSelectedIfNoValueExist() {
        return false;
    }

    private void updateComponents() {
        final InputColumn<?>[] currentValue = getCurrentValue();
        updateComponents(currentValue);
    }

    private void updateComponents(final InputColumn<?>[] value) {
        // fetch available input columns
        final List<InputColumn<?>> availableColumns = getAnalysisJobBuilder().getAvailableInputColumns(
                getComponentBuilder(), _dataType);

        final Set<InputColumn<?>> inputColumnsToBeRemoved = new HashSet<>();
        inputColumnsToBeRemoved.addAll(_checkBoxes.keySet());

        if (value != null) {
            // retain selected expression based input columns
            for (InputColumn<?> col : value) {
                if (col instanceof ExpressionBasedInputColumn) {
                    inputColumnsToBeRemoved.remove(col);
                    availableColumns.add(col);
                }
            }
        }

        for (InputColumn<?> col : availableColumns) {
            inputColumnsToBeRemoved.remove(col);
            final DCCheckBox<InputColumn<?>> checkBox = _checkBoxes.get(col);
            if (checkBox == null) {
                addAvailableInputColumn(col, isEnabled(col, value));
            } else {
                // handle updated names from transformed columns.
                checkBox.setText(col.getName());
            }
        }

        for (InputColumn<?> col : inputColumnsToBeRemoved) {
            removeAvailableInputColumn(col);
        }

        updateVisibility();
    }

    private void updateVisibility() {
        _searchDatastoreTextField.setVisible(_checkBoxes.size() > 16);
        if (_checkBoxes.isEmpty()) {
            add(_notAvailableCheckBox);
        } else {
            remove(_notAvailableCheckBox);
        }
    }

    /**
     * Method that allows decorating a checkbox for an input column. Subclasses
     * can eg. wrap the checkbox in a panel, in order to make additional widgets
     * available.
     *
     * @param checkBox
     * @return a {@link JComponent} to add to the widget's parent.
     */
    protected JComponent decorateCheckBox(DCCheckBox<InputColumn<?>> checkBox) {
        return checkBox;
    }

    private boolean isEnabled(InputColumn<?> inputColumn, InputColumn<?>[] currentValue) {
        if (_firstUpdate) {
            if (currentValue == null || currentValue.length == 0) {
                // set all to true if this is the only required inputcolumn
                // property
                ComponentDescriptor<?> componentDescriptor = getPropertyDescriptor().getComponentDescriptor();
                if (componentDescriptor.getConfiguredPropertiesForInput(false).size() == 1) {
                    return isAllInputColumnsSelectedIfNoValueExist();
                }
                return false;
            }
        }
        for (InputColumn<?> col : currentValue) {
            if (inputColumn == col) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSet() {
        for (JCheckBox checkBox : _checkBoxes.values()) {
            if (checkBox.isSelected()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public InputColumn<?>[] getValue() {
        final List<InputColumn<?>> result = getSelectedInputColumns();

        if (logger.isDebugEnabled()) {
            final List<String> names = CollectionUtils.map(result, new HasNameMapper());
            logger.debug("getValue() returning: {}", names);
        }

        return result.toArray(new InputColumn<?>[result.size()]);
    }

    protected List<InputColumn<?>> getSelectedInputColumns() {
        final List<InputColumn<?>> result = new ArrayList<>();
        final Collection<DCCheckBox<InputColumn<?>>> checkBoxes = _checkBoxes.values();
        for (final DCCheckBox<InputColumn<?>> cb : checkBoxes) {
            if (cb.isSelected()) {
                final InputColumn<?> value = cb.getValue();
                if (value != null) {
                    result.add(value);
                }
            }
        }
        return result;
    }

    @Override
    public void onAdd(InputColumn<?> sourceColumn) {
        if (_dataType == Object.class || ReflectionUtils.is(sourceColumn.getDataType(), _dataType)) {
            addAvailableInputColumn(sourceColumn);
            updateVisibility();
        }
    }

    @Override
    public void onRemove(InputColumn<?> sourceColumn) {
        removeAvailableInputColumn(sourceColumn);
        updateVisibility();
    }

    @Override
    public void onAdd(TransformerComponentBuilder<?> transformerJobBuilder) {
    }

    @Override
    public void onRemove(TransformerComponentBuilder<?> transformerJobBuilder) {
    }

    @Override
    public void onPanelRemove() {
        super.onPanelRemove();
        getAnalysisJobBuilder().removeSourceColumnChangeListener(this);
        getAnalysisJobBuilder().removeTransformerChangeListener(this);
    }

    @Override
    public void onOutputChanged(TransformerComponentBuilder<?> transformerJobBuilder,
            List<MutableInputColumn<?>> outputColumns) {

        // Makes sure it makes sense to do this (rather destructive) update
        if (transformerJobBuilder == getComponentBuilder() || transformerJobBuilder.getAnalysisJobBuilder() != getAnalysisJobBuilder()) {
            return;
        }

        // we need to save the current value before we update the components
        // here. Otherwise any previous selections will be lost.
        final InputColumn<?>[] value = getValue();
        getComponentBuilder().setConfiguredProperty(getPropertyDescriptor(), value);

        updateComponents(value);
    }

    @Override
    public void onConfigurationChanged(TransformerComponentBuilder<?> transformerJobBuilder) {
        if (transformerJobBuilder == getComponentBuilder()) {
            return;
        }
        updateComponents();
        updateUI();
    }

    @Override
    public void onRequirementChanged(TransformerComponentBuilder<?> transformerJobBuilder) {
    }

    @Override
    protected void setValue(final InputColumn<?>[] values) {
        if (values == null) {
            logger.debug("setValue(null) - delegating to setValue([])");
            setValue(new InputColumn[0]);
            return;
        }

        if (logger.isDebugEnabled()) {
            final List<String> names = CollectionUtils.map(values, new HasNameMapper());
            logger.debug("setValue({})", names);
        }

        // if checkBoxes is empty it means that the value is being set before
        // initializing the widget. This can occur in subclasses and automatic
        // creating of checkboxes should be done.
        if (_checkBoxes.isEmpty()) {
            for (InputColumn<?> value : values) {
                addAvailableInputColumn(value, true);
            }
        }

        // add expression based input columns if needed.
        for (InputColumn<?> inputColumn : values) {
            if (inputColumn instanceof ExpressionBasedInputColumn && !_checkBoxes.containsKey(inputColumn)) {
                addAvailableInputColumn(inputColumn, true);
            }
        }

        // update selections in checkboxes
        batchUpdateWidget(new Runnable() {
            @Override
            public void run() {
                final List<InputColumn<?>> valueList = new ArrayList<>();
                for (DCCheckBox<InputColumn<?>> cb : _checkBoxes.values()) {
                    if (ArrayUtils.contains(values, cb.getValue())) {
                        cb.setSelected(true, true);
                        valueList.add(cb.getValue());
                    } else {
                        cb.setSelected(false, true);
                    }
                }
                onValuesBatchSelected(valueList);
            }
        });

        updateUI();
    }

    /**
     * Overrideable method for subclasses to get informed when values have been
     * selected in a batch update
     *
     * @param values
     */
    protected void onValuesBatchSelected(List<InputColumn<?>> values) {
    }

    protected void selectAll() {
        batchUpdateWidget(new Runnable() {
            @Override
            public void run() {
                final List<InputColumn<?>> valueList = new ArrayList<>();
                for (DCCheckBox<InputColumn<?>> cb : _checkBoxes.values()) {
                    if (cb.getValue() instanceof MutableInputColumn) {
                        final MutableInputColumn<?> mutableInputColumn = (MutableInputColumn<?>) cb.getValue();
                        if (mutableInputColumn.isHidden()) {
                            // skip hidden columns
                            continue;
                        }
                    }
                    if (cb.isEnabled()) {
                        cb.setSelected(true);
                        valueList.add(cb.getValue());
                    }
                }
                onValuesBatchSelected(valueList);
            }
        });
    }

    protected void selectNone() {
        batchUpdateWidget(new Runnable() {
            @Override
            public void run() {
                for (DCCheckBox<InputColumn<?>> cb : _checkBoxes.values()) {
                    cb.setSelected(false);
                }
                onValuesBatchSelected(Collections.<InputColumn<?>> emptyList());
            }
        });
    }

    private void addAvailableInputColumn(InputColumn<?> col) {
        addAvailableInputColumn(col, false);
    }

    private void addAvailableInputColumn(InputColumn<?> col, boolean selected) {
        JComponent decoration = getOrCreateCheckBoxDecoration(col, selected);
        add(decoration);
    }

    private void removeAvailableInputColumn(InputColumn<?> col) {
        boolean valueChanged = false;
        DCCheckBox<InputColumn<?>> checkBox = _checkBoxes.remove(col);
        if (checkBox != null) {
            if (checkBox.isSelected()) {
                valueChanged = true;
            }
            final JComponent decoration = _checkBoxDecorations.remove(checkBox);
            remove(decoration);
        }

        if (col instanceof MutableInputColumn) {
            ((MutableInputColumn<?>) col).removeListener(this);
        }

        if (valueChanged) {
            fireValueChanged();
        }
    }

    @Override
    public void reorderColumns(InputColumn<?>[] newValue) {
        reorderValue(newValue);
    }

    @Override
    public InputColumn<?>[] getColumns() {
        return getValue();
    }

    /**
     * Reorders the values
     *
     * @param sortedValue
     */
    public void reorderValue(final InputColumn<?>[] sortedValue) {
        // the offset represents the search textfield and the button panel
        final int offset = 2;

        // reorder the visual components
        for (int i = 0; i < sortedValue.length; i++) {
            InputColumn<?> inputColumn = sortedValue[i];
            JComponent decoration = getOrCreateCheckBoxDecoration(inputColumn, true);
            add(decoration, i + offset);
        }
        updateUI();

        // recreate the _checkBoxes map
        final TreeMap<InputColumn<?>, DCCheckBox<InputColumn<?>>> checkBoxesCopy = new TreeMap<>(_checkBoxes);
        _checkBoxes.clear();
        for (InputColumn<?> inputColumn : sortedValue) {
            final DCCheckBox<InputColumn<?>> checkBox = checkBoxesCopy.get(inputColumn);
            _checkBoxes.put(inputColumn, checkBox);
        }
        _checkBoxes.putAll(checkBoxesCopy);
        setValue(sortedValue);
    }

    private JComponent getOrCreateCheckBoxDecoration(InputColumn<?> inputColumn, boolean selected) {
        DCCheckBox<InputColumn<?>> checkBox = _checkBoxes.get(inputColumn);
        if (checkBox == null) {
            final String name = inputColumn.getName();
            checkBox = new DCCheckBox<>(name, selected);
            checkBox.addListener(checkBoxListener);
            checkBox.setValue(inputColumn);
            _checkBoxes.put(inputColumn, checkBox);
        }

        JComponent decoration = _checkBoxDecorations.get(checkBox);
        if (decoration == null) {
            decoration = decorateCheckBox(checkBox);
            _checkBoxDecorations.put(checkBox, decoration);

            if (inputColumn instanceof MutableInputColumn) {
                MutableInputColumn<?> mutableInputColumn = (MutableInputColumn<?>) inputColumn;
                mutableInputColumn.addListener(this);
                if (mutableInputColumn.isHidden()) {
                    decoration.setVisible(false);
                }
            }
        }
        return decoration;
    }

    public Map<InputColumn<?>, DCCheckBox<InputColumn<?>>> getCheckBoxes() {
        return Collections.unmodifiableMap(_checkBoxes);
    }

    public Map<DCCheckBox<InputColumn<?>>, JComponent> getCheckBoxDecorations() {
        return Collections.unmodifiableMap(_checkBoxDecorations);
    }

    public DCPanel getButtonPanel() {
        return _buttonPanel;
    }

    @Override
    public void onNameChanged(MutableInputColumn<?> inputColumn, String oldName, String newName) {
        DCCheckBox<InputColumn<?>> checkBox = getCheckBoxes().get(inputColumn);
        if (checkBox == null) {
            return;
        }
        checkBox.setText(newName);
    }

    @Override
    public void onVisibilityChanged(MutableInputColumn<?> inputColumn, boolean hidden) {
        final DCCheckBox<InputColumn<?>> checkBox = getCheckBoxes().get(inputColumn);
        if (checkBox == null) {
            return;
        }
        if (checkBox.isSelected()) {
            // don't hide columns that are selected
            return;
        }
        final JComponent decoration = getCheckBoxDecorations().get(checkBox);
        if (decoration == null) {
            return;
        }
        decoration.setVisible(!hidden);
    }
}
