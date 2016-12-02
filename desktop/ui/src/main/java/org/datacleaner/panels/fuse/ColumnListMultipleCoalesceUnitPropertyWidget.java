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

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang.ArrayUtils;
import org.apache.metamodel.util.EqualsBuilder;
import org.datacleaner.api.InputColumn;
import org.datacleaner.components.fuse.CoalesceUnit;
import org.datacleaner.components.fuse.CoalesceUnitMissingColumnException;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.SourceColumnChangeListener;
import org.datacleaner.job.builder.TransformerChangeListener;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.widgets.properties.AbstractPropertyWidget;
import org.datacleaner.widgets.properties.MinimalPropertyWidget;
import org.datacleaner.widgets.properties.PropertyWidget;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PropertyWidget} for two properties at one time: An array of
 * {@link InputColumn}s and an array of {@link CoalesceUnit}s.
 *
 * This widget displays a list of {@link CoalesceUnit}s with {@link InputColumn}
 * s that the user can add an remove from.
 */
public class ColumnListMultipleCoalesceUnitPropertyWidget extends AbstractPropertyWidget<InputColumn<?>[]>
        implements SourceColumnChangeListener, TransformerChangeListener, MutableInputColumn.Listener {

    private static final Logger logger = LoggerFactory.getLogger(ColumnListMultipleCoalesceUnitPropertyWidget.class);

    private final ConfiguredPropertyDescriptor _unitProperty;
    private final MinimalPropertyWidget<CoalesceUnit[]> _unitPropertyWidget;
    private final DCPanel _unitContainerPanel;

    private final Set<InputColumn<?>> _pickedInputColumns;

    public ColumnListMultipleCoalesceUnitPropertyWidget(final ComponentBuilder componentBuilder,
            final ConfiguredPropertyDescriptor inputProperty, final ConfiguredPropertyDescriptor unitProperty) {
        super(componentBuilder, inputProperty);
        _unitProperty = unitProperty;
        _pickedInputColumns = new HashSet<>();
        _unitContainerPanel = new DCPanel();
        _unitContainerPanel.setLayout(new VerticalLayout(2));

        getAnalysisJobBuilder().addSourceColumnChangeListener(this);
        getAnalysisJobBuilder().addTransformerChangeListener(this);

        _unitPropertyWidget = createUnitPropertyWidget();

        final JButton addButton = WidgetFactory.createSmallButton(IconUtils.ACTION_ADD_DARK);
        addButton.addActionListener(e -> addCoalesceUnit());

        final JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE_DARK);
        removeButton.addActionListener(e -> removeCoalesceUnit());

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

        final CoalesceUnit[] currentValue = (CoalesceUnit[]) getComponentBuilder().getConfiguredProperty(_unitProperty);
        if (currentValue == null) {
            addCoalesceUnit();
        } else {
            batchUpdateWidget(() -> {
                for (final CoalesceUnit unit : currentValue) {
                    addCoalesceUnit(unit);
                }
            });
        }
    }

    @Override
    public void onPanelRemove() {
        super.onPanelRemove();
        getAnalysisJobBuilder().removeSourceColumnChangeListener(this);
        getAnalysisJobBuilder().removeTransformerChangeListener(this);
    }

    public void removeCoalesceUnit() {
        final int componentCount = _unitContainerPanel.getComponentCount();
        if (componentCount > 1) {
            final int index = componentCount - 1;
            _unitContainerPanel.remove(index);
            _unitContainerPanel.updateUI();
            fireBothValuesChanged();
        }
        updateUI();
    }

    private void removeCoalesceUnitPanel(final CoalesceUnitPanel coalesceUnitPanel) {
        final int componentCount = _unitContainerPanel.getComponentCount();
        if (componentCount > 1) {
            _unitContainerPanel.remove(coalesceUnitPanel);
            _unitContainerPanel.updateUI();
            fireBothValuesChanged();
        }
        updateUI();
    }

    public void addCoalesceUnit(final CoalesceUnit unit) {
        final CoalesceUnitPanel panel = new CoalesceUnitPanel(this, unit);
        _unitContainerPanel.add(panel);
        updateUI();
    }

    public void addCoalesceUnit() {
        final CoalesceUnitPanel panel = new CoalesceUnitPanel(this);
        _unitContainerPanel.add(panel);
        fireBothValuesChanged();
        updateUI();
    }

    private MinimalPropertyWidget<CoalesceUnit[]> createUnitPropertyWidget() {
        return new MinimalPropertyWidget<CoalesceUnit[]>(getComponentBuilder(), _unitProperty) {

            @Override
            public JComponent getWidget() {
                // do not return a visual widget
                return null;
            }

            @Override
            public CoalesceUnit[] getValue() {
                final CoalesceUnit[] units = getCoalesceUnits();
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
            protected void setValue(final CoalesceUnit[] value) {
                if (value == null) {
                    return;
                }
                if (EqualsBuilder.equals(value, getValue())) {
                    return;
                }
                setCoalesceUnits(value);
            }

            @Override
            public boolean isSet() {
                return ColumnListMultipleCoalesceUnitPropertyWidget.this.isSet();
            }
        };
    }

    @Override
    public boolean isSet() {
        final List<CoalesceUnitPanel> panels = getCoalesceUnitPanels();
        if (panels.isEmpty()) {
            return false;
        }
        for (final CoalesceUnitPanel panel : panels) {
            if (!panel.isSet()) {
                return false;
            }
        }
        return true;
    }

    private List<CoalesceUnitPanel> getCoalesceUnitPanels() {
        final List<CoalesceUnitPanel> result = new ArrayList<>();
        final Component[] components = _unitContainerPanel.getComponents();
        for (final Component component : components) {
            if (component instanceof CoalesceUnitPanel) {
                result.add((CoalesceUnitPanel) component);
            }
        }
        return result;
    }

    public CoalesceUnit[] getCoalesceUnits() {
        final List<CoalesceUnitPanel> panels = getCoalesceUnitPanels();
        final List<CoalesceUnit> result = new ArrayList<>();
        for (final CoalesceUnitPanel panel : panels) {
            if (panel.isSet()) {
                final CoalesceUnit unit = panel.getCoalesceUnit();
                result.add(unit);
            }
        }
        return result.toArray(new CoalesceUnit[result.size()]);
    }

    public void setCoalesceUnits(final CoalesceUnit[] units) {
        batchUpdateWidget(() -> {
            _unitContainerPanel.removeAll();
            for (final CoalesceUnit unit : units) {
                addCoalesceUnit(unit);
            }
        });
    }

    @Override
    public InputColumn<?>[] getValue() {
        final List<InputColumn<?>> availableInputColumns =
                getAnalysisJobBuilder().getAvailableInputColumns(Object.class);
        final InputColumn<?>[] allInputColumns =
                availableInputColumns.toArray(new InputColumn[availableInputColumns.size()]);
        final List<InputColumn<?>> resultList = new ArrayList<>();

        final CoalesceUnit[] units = getCoalesceUnits();
        if (units.length == 0) {
            logger.debug("Returning Input.value = null");
            return null;
        }

        for (final CoalesceUnit unit : units) {
            final InputColumn<?>[] updatedInputColumns = unit.getUpdatedInputColumns(allInputColumns, true);
            Collections.addAll(resultList, updatedInputColumns);
        }

        logger.debug("Returning Input.value = {}", resultList);
        return resultList.toArray(new InputColumn[resultList.size()]);
    }

    @Override
    protected void setValue(final InputColumn<?>[] value) {
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
    public void onInputColumnPicked(final InputColumn<?> item) {
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
    public void onInputColumnReleased(final InputColumn<?> item) {
        _pickedInputColumns.remove(item);
        updateAvailableInputColumns();
        fireBothValuesChanged();
    }

    private void updateAvailableInputColumns() {
        final List<InputColumn<?>> availableInputColumns = getAvailableInputColumns();

        final List<CoalesceUnitPanel> panels = getCoalesceUnitPanels();
        for (final CoalesceUnitPanel panel : panels) {
            panel.setAvailableInputColumns(availableInputColumns);
        }
    }

    public List<InputColumn<?>> getAvailableInputColumns() {
        final List<InputColumn<?>> availableInputColumns =
                getAnalysisJobBuilder().getAvailableInputColumns(Object.class);

        if (getComponentBuilder() instanceof TransformerComponentBuilder) {
            // remove all the columns that are generated by the transformer
            // itself!
            final TransformerComponentBuilder<?> tjb = (TransformerComponentBuilder<?>) getComponentBuilder();
            final List<MutableInputColumn<?>> outputColumns = tjb.getOutputColumns();
            availableInputColumns.removeAll(outputColumns);
        }

        availableInputColumns.removeAll(_pickedInputColumns);
        return availableInputColumns;
    }

    @Override
    public void onNameChanged(final MutableInputColumn<?> inputColumn, final String oldName, final String newName) {
        updateAvailableInputColumns();
    }

    @Override
    public void onVisibilityChanged(final MutableInputColumn<?> inputColumn, final boolean hidden) {
    }

    @Override
    public void onAdd(final TransformerComponentBuilder<?> transformerJobBuilder) {
        updateAvailableInputColumns();
    }

    @Override
    public void onRemove(final TransformerComponentBuilder<?> transformerJobBuilder) {
        updateAvailableInputColumns();
    }

    @Override
    public void onConfigurationChanged(final TransformerComponentBuilder<?> transformerJobBuilder) {
    }

    @Override
    public void onRequirementChanged(final TransformerComponentBuilder<?> transformerJobBuilder) {
    }

    @Override
    public void onOutputChanged(final TransformerComponentBuilder<?> transformerJobBuilder,
            final List<MutableInputColumn<?>> outputColumns) {
        for (final MutableInputColumn<?> outputColumn : outputColumns) {
            outputColumn.addListener(this);
        }
        updateAvailableInputColumns();
    }

    @Override
    public void onValueTouched(final InputColumn<?>[] value) {
        try {
            super.onValueTouched(value);
        } catch (final CoalesceUnitMissingColumnException e) {
            logger.warn("Missing input column for coalesce unit", e);
            final CoalesceUnit failingCoalesceUnit = e.getCoalesceUnit();
            final CoalesceUnit[] newCoalesceUnits =
                    (CoalesceUnit[]) ArrayUtils.removeElement(_unitPropertyWidget.getValue(), failingCoalesceUnit);
            for (final InputColumn<?> inputColumn : failingCoalesceUnit.getInputColumns()) {
                _pickedInputColumns.remove(inputColumn);
            }
            getCoalesceUnitPanels().stream()
                    .filter(coalesceUnitPanel -> coalesceUnitPanel.getCoalesceUnit().equals(failingCoalesceUnit))
                    .forEach(this::removeCoalesceUnitPanel);
            _unitPropertyWidget.onValueTouched(newCoalesceUnits);
            updateAvailableInputColumns();
            fireBothValuesChanged();
            updateUI();
        }
    }

    @Override
    public void onAdd(final InputColumn<?> sourceColumn) {
        updateAvailableInputColumns();
    }

    @Override
    public void onRemove(final InputColumn<?> sourceColumn) {
        updateAvailableInputColumns();
    }

    private void fireBothValuesChanged() {
        if (!isUpdating() && !isBatchUpdating()) {
            final Map<ConfiguredPropertyDescriptor, Object> properties = new HashMap<>();
            properties.put(getPropertyDescriptor(), getValue());
            properties.put(_unitProperty, _unitPropertyWidget.getValue());
            fireValuesChanged(properties);
        }
    }
}
