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
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.beans.coalesce.CoalesceUnit;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.widgets.properties.AbstractPropertyWidget;
import org.eobjects.datacleaner.widgets.properties.MinimalPropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.metamodel.util.EqualsBuilder;
import org.jdesktop.swingx.VerticalLayout;

/**
 * {@link PropertyWidget} for two properties at one time: An array of
 * {@link InputColumn}s and an array of {@link CoalesceUnit}s.
 */
public class MultipleCoalesceUnitPropertyWidget extends AbstractPropertyWidget<InputColumn<?>[]> {

    private final ConfiguredPropertyDescriptor _unitProperty;
    private final PropertyWidget<CoalesceUnit[]> _unitPropertyWidget;
    private final DCPanel _unitContainerPanel;

    public MultipleCoalesceUnitPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
            ConfiguredPropertyDescriptor inputProperty, ConfiguredPropertyDescriptor unitProperty) {
        super(beanJobBuilder, inputProperty);
        _unitProperty = unitProperty;
        _unitContainerPanel = new DCPanel();
        _unitContainerPanel.setLayout(new VerticalLayout(2));

        _unitPropertyWidget = createUnitPropertyWidget();

        final JButton addButton = WidgetFactory.createSmallButton(IconUtils.ACTION_ADD);
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addCoalesceUnit();
                fireValueChanged();
            }
        });

        final JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE);
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeCoalesceUnit();
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

        outerPanel.add(_unitContainerPanel, BorderLayout.CENTER);
        outerPanel.add(buttonPanel, BorderLayout.EAST);

        add(outerPanel);
    }

    public void removeCoalesceUnit() {
        final int componentCount = _unitContainerPanel.getComponentCount();
        if (componentCount > 0) {
            final int index = componentCount - 1;
            _unitContainerPanel.remove(index);
            _unitContainerPanel.updateUI();
            fireValueChanged();
        }
    }

    public void addCoalesceUnit(CoalesceUnit unit) {
        final CoalesceUnitPanel panel = new CoalesceUnitPanel(this, unit);
        _unitContainerPanel.add(panel);
    }

    public void addCoalesceUnit() {
        final CoalesceUnitPanel panel = new CoalesceUnitPanel(this);
        _unitContainerPanel.add(panel);
    }

    private PropertyWidget<CoalesceUnit[]> createUnitPropertyWidget() {
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
                    return null;
                }
                return units;
            }

            @Override
            public boolean isSet() {
                return MultipleCoalesceUnitPropertyWidget.this.isSet();
            }

            @Override
            protected void setValue(CoalesceUnit[] value) {
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
            return null;
        }

        for (CoalesceUnit unit : units) {
            final InputColumn<?>[] inputColumns = unit.getInputColumns(allInputColumns);
            for (final InputColumn<?> inputColumn : inputColumns) {
                resultList.add(inputColumn);
            }
        }

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

}
