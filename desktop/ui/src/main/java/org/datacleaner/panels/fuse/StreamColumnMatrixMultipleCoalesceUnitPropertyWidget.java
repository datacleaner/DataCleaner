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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.EqualsBuilder;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.components.fuse.CoalesceUnit;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.TransformerChangeListener;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.SourceColumnFinder;
import org.datacleaner.widgets.properties.AbstractPropertyWidget;
import org.datacleaner.widgets.properties.MinimalPropertyWidget;
import org.datacleaner.widgets.properties.PropertyWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * A {@link PropertyWidget} for representing both a property with an array of
 * {@link CoalesceUnit}s and another property with an array of
 * {@link InputColumn}s.
 * 
 * This widget presents the incoming streams as columns and available
 * {@link InputColumn}s from each stream as rows in a matrix where the user gets
 * the design a new {@link OutputDataStream}.
 */
public class StreamColumnMatrixMultipleCoalesceUnitPropertyWidget extends AbstractPropertyWidget<InputColumn<?>[]>
        implements TransformerChangeListener, MutableInputColumn.Listener {

    private static final Logger logger = LoggerFactory
            .getLogger(StreamColumnMatrixMultipleCoalesceUnitPropertyWidget.class);

    private final ConfiguredPropertyDescriptor _unitProperty;
    private final MinimalPropertyWidget<CoalesceUnit[]> _unitPropertyWidget;
    private final DCPanel _containerPanel;
    private final List<StreamColumnListPanel> _tablePanels;

    public StreamColumnMatrixMultipleCoalesceUnitPropertyWidget(ComponentBuilder componentBuilder,
            ConfiguredPropertyDescriptor inputProperty, ConfiguredPropertyDescriptor unitProperty) {
        super(componentBuilder, inputProperty);
        _unitProperty = unitProperty;
        _tablePanels = new ArrayList<>();

        getAnalysisJobBuilder().addTransformerChangeListener(this);

        _containerPanel = new DCPanel();
        _containerPanel.setLayout(new BoxLayout(_containerPanel, BoxLayout.X_AXIS));

        _unitPropertyWidget = createUnitPropertyWidget();

        // only facilitate horizontal scroll
        final JScrollPane scroll = new JScrollPane(_containerPanel);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        add(scroll);

        final CoalesceUnit[] coalesceUnits = (CoalesceUnit[]) getComponentBuilder()
                .getConfiguredProperty(_unitProperty);

        refresh(coalesceUnits);
    }

    private void refresh(final CoalesceUnit[] units) {
        _tablePanels.clear();
        _containerPanel.removeAll();

        final AnalysisJobBuilder ajb = getAnalysisJobBuilder();

        InputColumn<?>[] inputColumns = getCurrentValue();
        if (inputColumns == null) {
            inputColumns = ajb.getSourceColumns().toArray(new InputColumn[0]);
        }

        // TODO: We need a SourceColumnFinder that is aware of also nested jobs
        final SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
        sourceColumnFinder.addSources(ajb);

        // build registries of available columns and coalesced columns
        final Multimap<Table, InputColumn<?>> allTablesAndColumns = ArrayListMultimap.create();
        final Multimap<Table, InputColumn<?>> coalescedTablesAndColumns = ArrayListMultimap.create();
        {
            if (units != null) {
                for (CoalesceUnit unit : units) {
                    final InputColumn<?>[] coalescedInputColumns = unit.getInputColumns(inputColumns);
                    for (InputColumn<?> inputColumn : coalescedInputColumns) {
                        final Table table = sourceColumnFinder.findOriginatingTable(inputColumn);
                        coalescedTablesAndColumns.put(table, inputColumn);
                    }
                }
            }
            for (InputColumn<?> inputColumn : inputColumns) {
                Table table = sourceColumnFinder.findOriginatingTable(inputColumn);
                allTablesAndColumns.put(table, inputColumn);
            }
        }

        for (final Table table : allTablesAndColumns.keySet()) {
            final StreamColumnListPanel tablePanel = new StreamColumnListPanel(ajb, table, new StreamColumnListPanel.Listener() {
                @Override
                public void onValueChanged(StreamColumnListPanel panel) {
                    fireValueChanged();
                    _unitPropertyWidget.fireValueChanged();
                }
            });

            final Collection<InputColumn<?>> selectedColumns = coalescedTablesAndColumns.get(table);
            for (InputColumn<?> inputColumn : selectedColumns) {
                tablePanel.addInputColumn(inputColumn, true);
            }

            final Collection<InputColumn<?>> columns = new ArrayList<>(allTablesAndColumns.get(table));
            columns.removeAll(selectedColumns);
            for (InputColumn<?> inputColumn : columns) {
                tablePanel.addInputColumn(inputColumn, false);
            }

            _tablePanels.add(tablePanel);
            _containerPanel.add(tablePanel);
        }
    }

    @Override
    public void onPanelRemove() {
        super.onPanelRemove();
        getAnalysisJobBuilder().addTransformerChangeListener(this);
    }

    public PropertyWidget<?> getUnitPropertyWidget() {
        return _unitPropertyWidget;
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
            public boolean isSet() {
                return StreamColumnMatrixMultipleCoalesceUnitPropertyWidget.this.isSet();
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

    protected void setCoalesceUnits(CoalesceUnit[] value) {
        // TODO Auto-generated method stub

    }

    protected CoalesceUnit[] getCoalesceUnits() {
        int max = 0;

        final List<List<InputColumn<?>>> allCoalescedInputColumns = new ArrayList<>();
        for (StreamColumnListPanel tablePanel : _tablePanels) {
            final List<InputColumn<?>> coalescedInputColumns = tablePanel.getCoalescedInputColumns();
            allCoalescedInputColumns.add(coalescedInputColumns);
            max = Math.max(max, coalescedInputColumns.size());
        }

        final CoalesceUnit[] result = new CoalesceUnit[max];
        for (int i = 0; i < result.length; i++) {
            final List<InputColumn<?>> coalesceUnitInputColumns = new ArrayList<>();
            for (List<InputColumn<?>> coalescedInputColumnsForTable : allCoalescedInputColumns) {
                if (coalescedInputColumnsForTable.size() - 1 >= i) {
                    coalesceUnitInputColumns.add(coalescedInputColumnsForTable.get(i));
                }
            }
            final CoalesceUnit unit = new CoalesceUnit(coalesceUnitInputColumns);
            result[i] = unit;
        }
        return result;
    }

    @Override
    public InputColumn<?>[] getValue() {
        final List<InputColumn<?>> result = new ArrayList<>();
        for (StreamColumnListPanel tablePanel : _tablePanels) {
            result.addAll(tablePanel.getCoalescedInputColumns());
        }
        return result.toArray(new InputColumn<?>[result.size()]);
    }

    @Override
    public void onNameChanged(MutableInputColumn<?> inputColumn, String oldName, String newName) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onVisibilityChanged(MutableInputColumn<?> inputColumn, boolean hidden) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onOutputChanged(TransformerComponentBuilder<?> transformerJobBuilder,
            List<MutableInputColumn<?>> outputColumns) {
        if (transformerJobBuilder != getComponentBuilder()) {
            refresh(getCoalesceUnits());
        }
    }

    @Override
    protected void setValue(InputColumn<?>[] value) {
        // TODO
    }

    @Override
    public void onAdd(TransformerComponentBuilder<?> builder) {
    }

    @Override
    public void onConfigurationChanged(TransformerComponentBuilder<?> builder) {
    }

    @Override
    public void onRequirementChanged(TransformerComponentBuilder<?> builder) {
    }

    @Override
    public void onRemove(TransformerComponentBuilder<?> componentBuilder) {
    }
}
