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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
import org.datacleaner.job.builder.SourceColumnChangeListener;
import org.datacleaner.job.builder.TransformerChangeListener;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.SourceColumnFinder;
import org.datacleaner.widgets.properties.AbstractPropertyWidget;
import org.datacleaner.widgets.properties.MinimalPropertyWidget;
import org.datacleaner.widgets.properties.PropertyWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        implements SourceColumnChangeListener, TransformerChangeListener, MutableInputColumn.Listener {

    private static final Logger logger = LoggerFactory
            .getLogger(StreamColumnMatrixMultipleCoalesceUnitPropertyWidget.class);

    private final ConfiguredPropertyDescriptor _unitProperty;
    private final MinimalPropertyWidget<CoalesceUnit[]> _unitPropertyWidget;
    private final DCPanel _containerPanel;

    public StreamColumnMatrixMultipleCoalesceUnitPropertyWidget(ComponentBuilder componentBuilder,
            ConfiguredPropertyDescriptor inputProperty, ConfiguredPropertyDescriptor unitProperty) {
        super(componentBuilder, inputProperty);
        _unitProperty = unitProperty;

        getAnalysisJobBuilder().addSourceColumnChangeListener(this);
        getAnalysisJobBuilder().addTransformerChangeListener(this);

        _containerPanel = new DCPanel();
        _containerPanel.setLayout(new BoxLayout(_containerPanel, BoxLayout.X_AXIS));

        _unitPropertyWidget = createUnitPropertyWidget();

        // only facilitate horizontal scroll
        final JScrollPane scroll = new JScrollPane(_containerPanel);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        
        add(scroll);

        refresh();
    }

    private void refresh() {
        InputColumn<?>[] inputColumns = getCurrentValue();
        AnalysisJobBuilder ajb = getAnalysisJobBuilder();

        // TODO: We need a SourceColumnFinder that is aware of also nested jobs
        final SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
        sourceColumnFinder.addSources(ajb);

        final Set<Table> tablesRepresented = new TreeSet<>();
        for (InputColumn<?> inputColumn : inputColumns) {
            Table table = sourceColumnFinder.findOriginatingTable(inputColumn);
            tablesRepresented.add(table);
        }

        _containerPanel.removeAll();
        
        for (Table table : tablesRepresented) {
            DCPanel tablePanel = new DCPanel();
            tablePanel.setTitledBorder(table.getName());
            _containerPanel.add(tablePanel);
        }
        
    }

    @Override
    public void onPanelRemove() {
        super.onPanelRemove();
        getAnalysisJobBuilder().removeSourceColumnChangeListener(this);
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputColumn<?>[] getValue() {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
    }

    @Override
    public void onAdd(InputColumn<?> sourceColumn) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRemove(InputColumn<?> sourceColumn) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void setValue(InputColumn<?>[] value) {
        // TODO Auto-generated method stub

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
