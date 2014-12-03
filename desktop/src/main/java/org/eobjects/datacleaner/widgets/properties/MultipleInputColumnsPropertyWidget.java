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
package org.eobjects.datacleaner.widgets.properties;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;

import org.apache.commons.lang.ArrayUtils;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.HasNameMapper;
import org.eobjects.analyzer.data.ExpressionBasedInputColumn;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ComponentDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.SourceColumnChangeListener;
import org.eobjects.analyzer.job.builder.TransformerChangeListener;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.util.LabelUtils;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.actions.AddExpressionBasedColumnActionListener;
import org.eobjects.datacleaner.actions.ReorderColumnsActionListener;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCCheckBox;
import org.eobjects.datacleaner.widgets.DCCheckBox.Listener;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Property widget for multiple input columns. Displays these as checkboxes.
 */
public class MultipleInputColumnsPropertyWidget extends AbstractPropertyWidget<InputColumn<?>[]> implements
        SourceColumnChangeListener, TransformerChangeListener, MutableInputColumn.Listener {

    private static final Logger logger = LoggerFactory.getLogger(MultipleInputColumnsPropertyWidget.class);

    // border for the button panel and search box to make them "indented"
    // similar to the check boxes.
    private static final EmptyBorder _indentBorder = new MatteBorder(1, 17, 0, 1, WidgetUtils.BG_COLOR_BRIGHT);

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
    public MultipleInputColumnsPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
            ConfiguredPropertyDescriptor propertyDescriptor) {
        super(beanJobBuilder, propertyDescriptor);
        setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        _checkBoxes = new LinkedHashMap<InputColumn<?>, DCCheckBox<InputColumn<?>>>();
        _checkBoxDecorations = new IdentityHashMap<DCCheckBox<InputColumn<?>>, JComponent>();
        _firstUpdate = true;
        _dataType = propertyDescriptor.getTypeArgument(0);
        getAnalysisJobBuilder().getSourceColumnListeners().add(this);
        getAnalysisJobBuilder().getTransformerChangeListeners().add(this);
        setLayout(new VerticalLayout(2));

        _searchDatastoreTextField = WidgetFactory.createTextField("Search/filter columns");
        _searchDatastoreTextField.setBorder(new CompoundBorder(_indentBorder, WidgetUtils.BORDER_THIN));
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
                        cb.setVisible(name.indexOf(text) != -1);
                    }
                }
            }
        });

        _notAvailableCheckBox = new DCCheckBox<InputColumn<?>>("<html><font color=\"gray\">- no <i>"
                + LabelUtils.getDataTypeLabel(_dataType) + "</i> columns available -</font></html>", false);
        _notAvailableCheckBox.setEnabled(false);

        _buttonPanel = new DCPanel();
        _buttonPanel.setLayout(new HorizontalLayout(2));
        _buttonPanel.setBorder(_indentBorder);

        JButton selectAllButton = new JButton("Select all");
        selectAllButton.setFont(WidgetUtils.FONT_SMALL);
        selectAllButton.addActionListener(selectAllActionListener);
        _buttonPanel.add(selectAllButton);

        JButton selectNoneButton = new JButton("Select none");
        selectNoneButton.setFont(WidgetUtils.FONT_SMALL);
        selectNoneButton.addActionListener(selectNoneActionListener);
        _buttonPanel.add(selectNoneButton);

        if (propertyDescriptor.isArray()) {
            if (_dataType == String.class || _dataType == Object.class) {
                final JButton expressionColumnButton = WidgetFactory
                        .createSmallButton(IconUtils.BUTTON_EXPRESSION_COLUMN_IMAGEPATH);
                expressionColumnButton.setToolTipText("Create expression/value based column");
                expressionColumnButton.addActionListener(AddExpressionBasedColumnActionListener
                        .forMultipleColumns(this));
                _buttonPanel.add(expressionColumnButton);
            }

            final JButton reorderColumnsButton = WidgetFactory
                    .createSmallButton(IconUtils.BUTTON_REORDER_COLUMN_IMAGEPATH);
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
                getBeanJobBuilder(), _dataType);

        final Set<InputColumn<?>> inputColumnsToBeRemoved = new HashSet<InputColumn<?>>();
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
        _searchDatastoreTextField.setVisible(_checkBoxes.size() > 5);
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
                if (componentDescriptor instanceof BeanDescriptor<?>) {
                    if (((BeanDescriptor<?>) componentDescriptor).getConfiguredPropertiesForInput(false).size() == 1) {
                        return isAllInputColumnsSelectedIfNoValueExist();
                    }
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
        final List<InputColumn<?>> result = new ArrayList<InputColumn<?>>();
        final Collection<DCCheckBox<InputColumn<?>>> checkBoxes = _checkBoxes.values();
        for (final DCCheckBox<InputColumn<?>> cb : checkBoxes) {
            if (cb.isSelected()) {
                final InputColumn<?> value = cb.getValue();
                if (value != null) {
                    result.add(value);
                }
            }
        }
        
        if (logger.isDebugEnabled()) {
            final List<String> names = CollectionUtils.map(result, new HasNameMapper());
            logger.debug("getValue() returning: {}", names);
        }
        
        return result.toArray(new InputColumn<?>[result.size()]);
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
    public void onAdd(TransformerJobBuilder<?> transformerJobBuilder) {
    }

    @Override
    public void onOutputChanged(TransformerJobBuilder<?> transformerJobBuilder,
            List<MutableInputColumn<?>> outputColumns) {
        // we need to save the current value before we update the components
        // here. Otherwise any previous selections will be lost.
        final InputColumn<?>[] value = getValue();
        getBeanJobBuilder().setConfiguredProperty(getPropertyDescriptor(), value);

        updateComponents(value);
    }

    @Override
    public void onRemove(TransformerJobBuilder<?> transformerJobBuilder) {
    }

    @Override
    public void onPanelRemove() {
        super.onPanelRemove();
        getAnalysisJobBuilder().getSourceColumnListeners().remove(this);
        getAnalysisJobBuilder().getTransformerChangeListeners().add(this);
    }

    @Override
    public void onConfigurationChanged(TransformerJobBuilder<?> transformerJobBuilder) {
        if (transformerJobBuilder == getBeanJobBuilder()) {
            return;
        }
        updateComponents();
        updateUI();
    }

    @Override
    public void onRequirementChanged(TransformerJobBuilder<?> transformerJobBuilder) {
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
                for (DCCheckBox<InputColumn<?>> cb : _checkBoxes.values()) {
                    if (ArrayUtils.contains(values, cb.getValue())) {
                        cb.setSelected(true, true);
                    } else {
                        cb.setSelected(false, true);
                    }
                }
            }
        });

        updateUI();
    }

    protected void selectAll() {
        batchUpdateWidget(new Runnable() {
            @Override
            public void run() {
                for (DCCheckBox<InputColumn<?>> cb : _checkBoxes.values()) {
                    if (cb.isEnabled()) {
                        cb.setSelected(true);
                    }
                }
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
        final TreeMap<InputColumn<?>, DCCheckBox<InputColumn<?>>> checkBoxesCopy = new TreeMap<InputColumn<?>, DCCheckBox<InputColumn<?>>>(
                _checkBoxes);
        _checkBoxes.clear();
        for (InputColumn<?> inputColumn : sortedValue) {
            final DCCheckBox<InputColumn<?>> checkBox = checkBoxesCopy.get(inputColumn);
            _checkBoxes.put(inputColumn, checkBox);
        }
        _checkBoxes.putAll(checkBoxesCopy);
    }

    private JComponent getOrCreateCheckBoxDecoration(InputColumn<?> inputColumn, boolean selected) {
        DCCheckBox<InputColumn<?>> checkBox = _checkBoxes.get(inputColumn);
        if (checkBox == null) {
            final String name = inputColumn.getName();
            checkBox = new DCCheckBox<InputColumn<?>>(name, selected);
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
