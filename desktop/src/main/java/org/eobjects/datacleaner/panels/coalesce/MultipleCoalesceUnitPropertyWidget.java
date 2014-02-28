/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.panels.coalesce;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.beans.coalesce.CoalesceUnit;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.SourceColumnChangeListener;
import org.eobjects.analyzer.job.builder.TransformerChangeListener;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.widgets.properties.AbstractPropertyWidget;
import org.eobjects.datacleaner.widgets.properties.MinimalPropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.metamodel.util.EqualsBuilder;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PropertyWidget} for two properties at one time: An array of
 * {@link InputColumn}s and an array of {@link CoalesceUnit}s.
 */
public class MultipleCoalesceUnitPropertyWidget extends AbstractPropertyWidget<InputColumn<?>[]> implements
        SourceColumnChangeListener, TransformerChangeListener, MutableInputColumn.Listener {

    private static final Logger logger = LoggerFactory.getLogger(MultipleCoalesceUnitPropertyWidget.class);

    private final ConfiguredPropertyDescriptor _unitProperty;
    private final MinimalPropertyWidget<CoalesceUnit[]> _unitPropertyWidget;
    private final DCPanel _unitContainerPanel;

    private final Set<InputColumn<?>> _pickedInputColumns;
    private final Set<InputColumn<?>> _hiddenInputColumns;

    public MultipleCoalesceUnitPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
            ConfiguredPropertyDescriptor inputProperty, ConfiguredPropertyDescriptor unitProperty) {
        super(beanJobBuilder, inputProperty);
        _unitProperty = unitProperty;
        _pickedInputColumns = new HashSet<InputColumn<?>>();
        _hiddenInputColumns = new HashSet<InputColumn<?>>();
        _unitContainerPanel = new DCPanel();
        _unitContainerPanel.setLayout(new VerticalLayout(2));

        getAnalysisJobBuilder().getSourceColumnListeners().add(this);
        getAnalysisJobBuilder().getTransformerChangeListeners().add(this);

        _unitPropertyWidget = createUnitPropertyWidget();

        final JButton addButton = WidgetFactory.createSmallButton(IconUtils.ACTION_ADD);
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addCoalesceUnit();
            }
        });

        final JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE);
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeCoalesceUnit();
            }
        });

        final DCPanel buttonPanel = new DCPanel();
        buttonPanel.setBorder(new EmptyBorder(0, 4, 0, 0));
        buttonPanel.setLayout(new VerticalLayout(2));
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);

        final DCPanel outerPanel = new DCPanel();
        outerPanel.setLayout(new BorderLayout());

        outerPanel.add(_unitContainerPanel, BorderLayout.CENTER);
        outerPanel.add(buttonPanel, BorderLayout.EAST);

        add(outerPanel);

        final CoalesceUnit[] currentValue = (CoalesceUnit[]) getBeanJobBuilder().getConfiguredProperty(_unitProperty);
        if (currentValue == null) {
            addCoalesceUnit();
        } else {
            batchUpdateWidget(new Runnable() {
                @Override
                public void run() {
                    for (CoalesceUnit unit : currentValue) {
                        addCoalesceUnit(unit);
                    }
                }
            });
        }
    }

    @Override
    public void onPanelRemove() {
        super.onPanelRemove();
        getAnalysisJobBuilder().getSourceColumnListeners().remove(this);
        getAnalysisJobBuilder().getTransformerChangeListeners().add(this);
    }

    public void removeCoalesceUnit() {
        final int componentCount = _unitContainerPanel.getComponentCount();
        if (componentCount > 1) {
            final int index = componentCount - 1;
            _unitContainerPanel.remove(index);
            _unitContainerPanel.updateUI();
            fireBothValuesChanged();
        }
    }

    public void addCoalesceUnit(CoalesceUnit unit) {
        final CoalesceUnitPanel panel = new CoalesceUnitPanel(this, unit);
        _unitContainerPanel.add(panel);
    }

    public void addCoalesceUnit() {
        final CoalesceUnitPanel panel = new CoalesceUnitPanel(this);
        _unitContainerPanel.add(panel);
        fireBothValuesChanged();
    }

    private MinimalPropertyWidget<CoalesceUnit[]> createUnitPropertyWidget() {
        return new MinimalPropertyWidget<CoalesceUnit[]>(getBeanJobBuilder(), _unitProperty) {

            @Override
            public JComponent getWidget() {
                // do not return a visual widget
                return null;
            }

            @Override
            public CoalesceUnit[] getValue() {
                CoalesceUnit[] units = getCoalesceUnits();
                if (units.length == 0) {
                    logger.debug("Returning Units.value = null");
                    return null;
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Returning Units.value = {}", Arrays.toString(units));
                }
                return units;
            }

            @Override
            public boolean isSet() {
                return MultipleCoalesceUnitPropertyWidget.this.isSet();
            }

            @Override
            protected void setValue(CoalesceUnit[] value) {
                if (value == null) {
                    return;
                }
                if (EqualsBuilder.equals(value, getValue())) {
                    return;
                }
                setCoalesceUnits(value);
            }
        };
    }

    @Override
    public boolean isSet() {
        final List<CoalesceUnitPanel> panels = getCoalesceUnitPanels();
        if (panels.isEmpty()) {
            return false;
        }
        for (CoalesceUnitPanel panel : panels) {
            if (!panel.isSet()) {
                return false;
            }
        }
        return true;
    }

    private List<CoalesceUnitPanel> getCoalesceUnitPanels() {
        List<CoalesceUnitPanel> result = new ArrayList<CoalesceUnitPanel>();
        Component[] components = _unitContainerPanel.getComponents();
        for (Component component : components) {
            if (component instanceof CoalesceUnitPanel) {
                result.add((CoalesceUnitPanel) component);
            }
        }
        return result;
    }

    public void setCoalesceUnits(final CoalesceUnit[] units) {
        batchUpdateWidget(new Runnable() {
            @Override
            public void run() {
                _unitContainerPanel.removeAll();
                for (CoalesceUnit unit : units) {
                    addCoalesceUnit(unit);
                }
            }
        });
    }

    public CoalesceUnit[] getCoalesceUnits() {
        List<CoalesceUnitPanel> panels = getCoalesceUnitPanels();
        List<CoalesceUnit> result = new ArrayList<CoalesceUnit>();
        for (CoalesceUnitPanel panel : panels) {
            if (panel.isSet()) {
                CoalesceUnit unit = panel.getCoalesceUnit();
                result.add(unit);
            }
        }
        return result.toArray(new CoalesceUnit[result.size()]);
    }

    @Override
    public InputColumn<?>[] getValue() {
        final List<InputColumn<?>> availableInputColumns = getAnalysisJobBuilder().getAvailableInputColumns(
                Object.class);
        final InputColumn<?>[] allInputColumns = availableInputColumns.toArray(new InputColumn[availableInputColumns
                .size()]);
        final List<InputColumn<?>> resultList = new ArrayList<InputColumn<?>>();

        final CoalesceUnit[] units = getCoalesceUnits();
        if (units.length == 0) {
            logger.debug("Returning Input.value = null");
            return null;
        }

        for (CoalesceUnit unit : units) {
            final InputColumn<?>[] inputColumns = unit.getInputColumns(allInputColumns);
            for (final InputColumn<?> inputColumn : inputColumns) {
                resultList.add(inputColumn);
            }
        }

        logger.debug("Returning Input.value = {}", resultList);
        final InputColumn<?>[] result = resultList.toArray(new InputColumn[resultList.size()]);
        return result;
    }

    @Override
    protected void setValue(InputColumn<?>[] value) {
        // we ignore this setValue call since the setValue call of the
        // unitPropertyWidget will also contain the input column references.
    }

    public PropertyWidget<?> getUnitPropertyWidget() {
        return _unitPropertyWidget;
    }

    /**
     * Called when an input column is picked (selected) by a child
     * CoalesceUnitPanel
     * 
     * @param item
     */
    public void onInputColumnPicked(InputColumn<?> item) {
        _pickedInputColumns.add(item);
        updateAvailableInputColumns();
        fireBothValuesChanged();
    }

    /**
     * Called when an input column is released (de-selected) by a child
     * CoalesceUnitPanel
     * 
     * @param item
     */
    public void onInputColumnReleased(InputColumn<?> item) {
        _pickedInputColumns.remove(item);
        updateAvailableInputColumns();
        fireBothValuesChanged();
    }

    private void updateAvailableInputColumns() {
        List<InputColumn<?>> availableInputColumns = getAvailableInputColumns();

        List<CoalesceUnitPanel> panels = getCoalesceUnitPanels();
        for (CoalesceUnitPanel panel : panels) {
            panel.setAvailableInputColumns(availableInputColumns);
        }
    }

    public List<InputColumn<?>> getAvailableInputColumns() {
        final List<InputColumn<?>> availableInputColumns = getAnalysisJobBuilder().getAvailableInputColumns(
                Object.class);

        if (getBeanJobBuilder() instanceof TransformerJobBuilder) {
            // remove all the columns that are generated by the transformer
            // itself!
            final TransformerJobBuilder<?> tjb = (TransformerJobBuilder<?>) getBeanJobBuilder();
            final List<MutableInputColumn<?>> outputColumns = tjb.getOutputColumns();
            availableInputColumns.removeAll(outputColumns);
        }

        availableInputColumns.removeAll(_pickedInputColumns);
        return availableInputColumns;
    }

    @Override
    public void onNameChanged(MutableInputColumn<?> inputColumn, String oldName, String newName) {
        updateAvailableInputColumns();
    }

    @Override
    public void onVisibilityChanged(MutableInputColumn<?> inputColumn, boolean hidden) {
        if (hidden) {
            _hiddenInputColumns.add(inputColumn);
        } else {
            _hiddenInputColumns.remove(inputColumn);
        }
    }

    @Override
    public void onAdd(TransformerJobBuilder<?> transformerJobBuilder) {
        updateAvailableInputColumns();
    }

    @Override
    public void onRemove(TransformerJobBuilder<?> transformerJobBuilder) {
        updateAvailableInputColumns();
    }

    @Override
    public void onConfigurationChanged(TransformerJobBuilder<?> transformerJobBuilder) {
    }

    @Override
    public void onRequirementChanged(TransformerJobBuilder<?> transformerJobBuilder) {
    }

    @Override
    public void onOutputChanged(TransformerJobBuilder<?> transformerJobBuilder,
            List<MutableInputColumn<?>> outputColumns) {
        for (MutableInputColumn<?> outputColumn : outputColumns) {
            outputColumn.addListener(this);
        }
        updateAvailableInputColumns();
    }

    @Override
    public void onAdd(InputColumn<?> sourceColumn) {
        updateAvailableInputColumns();
    }

    @Override
    public void onRemove(InputColumn<?> sourceColumn) {
        updateAvailableInputColumns();
    }

    private void fireBothValuesChanged() {
        if (!isUpdating() && !isBatchUpdating()) {
            _unitPropertyWidget.fireValueChanged();
            fireValueChanged();
        }
    }
}
