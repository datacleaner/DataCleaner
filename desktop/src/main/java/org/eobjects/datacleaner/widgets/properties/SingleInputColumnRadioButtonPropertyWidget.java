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

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.event.DocumentEvent;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.SourceColumnChangeListener;
import org.eobjects.analyzer.job.builder.TransformerChangeListener;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.util.LabelUtils;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.actions.AddExpressionBasedColumnActionListener;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.JXRadioGroup;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

/**
 * {@link PropertyWidget} for single {@link InputColumn}s. Displays the
 * selection as a series of radiobuttons. Used for required input columns.
 * 
 * @author Kasper Sørensen
 */
public class SingleInputColumnRadioButtonPropertyWidget extends AbstractPropertyWidget<InputColumn<?>> implements
        SourceColumnChangeListener, TransformerChangeListener, MutableInputColumn.Listener {

    private final JXRadioGroup<JRadioButton> _radioGroup = new JXRadioGroup<JRadioButton>();
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final Class<?> _dataType;
    private final ConfiguredPropertyDescriptor _propertyDescriptor;
    private final AbstractBeanJobBuilder<?, ?, ?> _beanJobBuilder;
    private final DCPanel _buttonPanel;
    private volatile JRadioButton[] _radioButtons;
    private volatile List<InputColumn<?>> _inputColumns;
    private final JXTextField _searchDatastoreTextField;

    @Inject
    public SingleInputColumnRadioButtonPropertyWidget(AnalysisJobBuilder analysisJobBuilder,
            AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder, ConfiguredPropertyDescriptor propertyDescriptor) {
        super(beanJobBuilder, propertyDescriptor);
        _radioGroup.setLayoutAxis(BoxLayout.Y_AXIS);
        _radioGroup.setOpaque(false);

        _analysisJobBuilder = analysisJobBuilder;
        _analysisJobBuilder.getSourceColumnListeners().add(this);
        _analysisJobBuilder.getTransformerChangeListeners().add(this);
        _beanJobBuilder = beanJobBuilder;
        _propertyDescriptor = propertyDescriptor;
        _dataType = propertyDescriptor.getTypeArgument(0);

        _searchDatastoreTextField = WidgetFactory.createTextField("Search/filter columns");
        _searchDatastoreTextField.setBorder(WidgetUtils.BORDER_THIN);
        _searchDatastoreTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                String text = _searchDatastoreTextField.getText();
                if (StringUtils.isNullOrEmpty(text)) {
                    // when there is no search query, set all datastores visible
                    for (JRadioButton rb : _radioButtons) {
                        rb.setVisible(true);
                    }
                } else {
                    // do a case insensitive search
                    text = text.trim().toLowerCase();
                    for (JRadioButton rb : _radioButtons) {
                        String name = rb.getText().toLowerCase();
                        rb.setVisible(name.indexOf(text) != -1);
                    }
                }
            }
        });

        _buttonPanel = new DCPanel();
        _buttonPanel.setLayout(new VerticalLayout(2));

        if (_dataType == String.class || _dataType == Object.class) {
            final JButton expressionColumnButton = WidgetFactory
                    .createSmallButton(IconUtils.BUTTON_EXPRESSION_COLUMN_IMAGEPATH);
            expressionColumnButton.setToolTipText("Create expression/value based column");
            expressionColumnButton.addActionListener(AddExpressionBasedColumnActionListener.forSingleColumn(this));
            expressionColumnButton.setFocusable(false);
            _buttonPanel.add(expressionColumnButton);
        }

        updateComponents();

        DCPanel outerPanel = new DCPanel();
        outerPanel.setLayout(new BorderLayout());
        outerPanel.add(_searchDatastoreTextField, BorderLayout.NORTH);
        outerPanel.add(_radioGroup, BorderLayout.CENTER);
        outerPanel.add(_buttonPanel, BorderLayout.EAST);
        add(outerPanel);
    }

    private void updateComponents() {
        InputColumn<?> currentValue = getCurrentValue();
        updateComponents(currentValue);
    }

    private void updateComponents(InputColumn<?> value) {
        _inputColumns = _analysisJobBuilder.getAvailableInputColumns(getBeanJobBuilder(), _dataType);

        if (value != null) {
            if (!_inputColumns.contains(value)) {
                _inputColumns.add(value);
            }
        }

        _searchDatastoreTextField.setVisible(_inputColumns.size() > 5);

        if (_propertyDescriptor.isRequired()) {
            _radioButtons = new JRadioButton[_inputColumns.size()];
        } else {
            _radioButtons = new JRadioButton[_inputColumns.size() + 1];
        }
        if (_inputColumns.isEmpty()) {
            _radioButtons = new JRadioButton[1];
            JRadioButton radioButton = new JRadioButton("<html><font color=\"gray\">- no <i>"
                    + LabelUtils.getDataTypeLabel(_dataType) + "</i> columns available -</font></html>");
            radioButton.setOpaque(false);
            radioButton.setEnabled(false);
            _radioButtons[0] = radioButton;
        } else {
            for (int i = 0; i < _inputColumns.size(); i++) {
                InputColumn<?> inputColumn = _inputColumns.get(i);
                JRadioButton radioButton = new JRadioButton(inputColumn.getName());
                radioButton.setOpaque(false);
                if (value == inputColumn) {
                    radioButton.setSelected(true);
                }
                if (inputColumn instanceof MutableInputColumn) {
                    MutableInputColumn<?> mutableInputColumn = (MutableInputColumn<?>) inputColumn;
                    mutableInputColumn.addListener(this);
                    if (mutableInputColumn.isHidden()) {
                        radioButton.setVisible(false);
                    }
                }
                _radioButtons[i] = radioButton;
            }

            if (!_propertyDescriptor.isRequired()) {
                JRadioButton radioButton = new JRadioButton("(none)");
                radioButton.setOpaque(false);
                if (value == null) {
                    radioButton.setSelected(true);
                }
                _radioButtons[_radioButtons.length - 1] = radioButton;
            }
        }

        for (int i = 0; i < _radioButtons.length; i++) {
            JRadioButton rb = _radioButtons[i];

            rb.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    fireValueChanged();
                }
            });
        }

        _radioGroup.setValues(_radioButtons);
    }

    private JRadioButton getRadioButton(InputColumn<?> column) {
        int i = 0;
        for (InputColumn<?> inputColumn : _inputColumns) {
            if (column.equals(inputColumn)) {
                return _radioButtons[i];
            }
            i++;
        }
        return null;
    }

    @Override
    public void onAdd(InputColumn<?> sourceColumn) {
        if (isColumnApplicable(sourceColumn)) {
            updateComponents();
            updateUI();
        }
    }

    private boolean isColumnApplicable(InputColumn<?> column) {
        return _dataType == Object.class || ReflectionUtils.is(column.getDataType(), _dataType);
    }

    @Override
    public void onRemove(InputColumn<?> sourceColumn) {
        handleRemovedColumn(sourceColumn);
    }

    private void handleRemovedColumn(InputColumn<?> column) {
        if (isColumnApplicable(column)) {
            InputColumn<?> currentValue = (InputColumn<?>) _beanJobBuilder.getConfiguredProperty(_propertyDescriptor);
            if (currentValue != null) {
                if (currentValue.equals(column)) {
                    _beanJobBuilder.setConfiguredProperty(_propertyDescriptor, null);
                }
            }
            updateComponents();
            updateUI();

            if (column instanceof MutableInputColumn) {
                ((MutableInputColumn<?>) column).removeListener(this);
            }
        }
    }

    @Override
    public void onAdd(TransformerJobBuilder<?> transformerJobBuilder) {
    }

    @Override
    public void onOutputChanged(TransformerJobBuilder<?> transformerJobBuilder,
            List<MutableInputColumn<?>> outputColumns) {
        updateComponents();
        updateUI();
    }

    @Override
    public void onRemove(TransformerJobBuilder<?> transformerJobBuilder) {
        List<MutableInputColumn<?>> outputColumns = transformerJobBuilder.getOutputColumns();
        for (MutableInputColumn<?> column : outputColumns) {
            handleRemovedColumn(column);
        }
    }

    @Override
    public void onPanelRemove() {
        super.onPanelRemove();
        _analysisJobBuilder.getSourceColumnListeners().remove(this);
        _analysisJobBuilder.getTransformerChangeListeners().remove(this);

        for (InputColumn<?> column : _inputColumns) {
            if (column instanceof MutableInputColumn) {
                ((MutableInputColumn<?>) column).removeListener(this);
            }
        }
    }

    @Override
    public InputColumn<?> getValue() {
        for (int i = 0; i < _inputColumns.size(); i++) {
            JRadioButton radio = _radioButtons[i];
            if (radio.isSelected()) {
                return _inputColumns.get(i);
            }
        }
        return null;
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
    protected void setValue(InputColumn<?> value) {
        updateComponents(value);
        updateUI();
    }

    @Override
    public void onNameChanged(MutableInputColumn<?> inputColumn, String oldName, String newName) {
        JRadioButton radioButton = getRadioButton(inputColumn);
        if (radioButton == null) {
            return;
        }
        radioButton.setText(newName);
    }

    @Override
    public void onVisibilityChanged(MutableInputColumn<?> inputColumn, boolean hidden) {
        JRadioButton radioButton = getRadioButton(inputColumn);
        if (radioButton == null) {
            return;
        }
        if (radioButton.isSelected()) {
            // don't hide columns that are selected.
            return;
        }
        radioButton.setVisible(!hidden);
    }
}
