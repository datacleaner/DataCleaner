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
package org.datacleaner.widgets.result;

import java.awt.BorderLayout;
import java.util.Collection;

import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

import org.apache.metamodel.util.LazyRef;
import org.apache.metamodel.util.Ref;
import org.datacleaner.api.Provided;
import org.datacleaner.api.RendererBean;
import org.datacleaner.beans.valuedist.GroupedValueDistributionResult;
import org.datacleaner.beans.valuedist.ValueDistributionAnalyzer;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.SchemaNavigator;
import org.datacleaner.guice.DCModuleImpl;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerJobBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.result.CompositeValueFrequency;
import org.datacleaner.result.GroupedValueCountingAnalyzerResult;
import org.datacleaner.result.SingleValueFrequency;
import org.datacleaner.result.ValueCountingAnalyzerResult;
import org.datacleaner.result.ValueFrequency;
import org.datacleaner.result.renderer.AbstractRenderer;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.result.renderer.SwingRenderingFormat;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.LookAndFeelManager;
import org.datacleaner.widgets.DCCollapsiblePanel;
import org.datacleaner.windows.ResultWindow;
import org.jdesktop.swingx.VerticalLayout;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Renderer for {@link GroupedValueDistributionResult}s as Swing components.
 * 
 * The results will be displayed using a chart and a table of values and their
 * counts.
 * 
 * @author Kasper Sørensen
 */
@RendererBean(SwingRenderingFormat.class)
public class ValueDistributionResultSwingRenderer extends AbstractRenderer<ValueCountingAnalyzerResult, JComponent> {

    @Inject
    @Provided
    RendererFactory _rendererFactory;

    @Inject
    @Provided
    WindowContext _windowContext;

    @Override
    public JComponent render(ValueCountingAnalyzerResult result) {
        if (result instanceof GroupedValueCountingAnalyzerResult) {
            GroupedValueCountingAnalyzerResult groupedResult = (GroupedValueCountingAnalyzerResult) result;
            return renderGroupedResult(groupedResult);
        } else {
            ValueDistributionResultSwingRendererGroupDelegate delegate = new ValueDistributionResultSwingRendererGroupDelegate(
                    result.getName(), _rendererFactory, _windowContext);
            return delegate.renderGroupResult(result);
        }
    }

    public JComponent renderGroupedResult(GroupedValueCountingAnalyzerResult groupedResult) {
        final DCPanel panel = new DCPanel();
        panel.setLayout(new VerticalLayout(0));
        Collection<? extends ValueCountingAnalyzerResult> results = groupedResult.getGroupResults();
        for (final ValueCountingAnalyzerResult res : results) {
            if (panel.getComponentCount() != 0) {
                panel.add(Box.createVerticalStrut(10));
            }

            final Ref<JComponent> componentRef = new LazyRef<JComponent>() {
                @Override
                protected JComponent fetch() {
                    ValueDistributionResultSwingRendererGroupDelegate delegate = new ValueDistributionResultSwingRendererGroupDelegate(
                            res.getName(), _rendererFactory, _windowContext);
                    final JComponent renderedResult = delegate.renderGroupResult(res);
                    final DCPanel decoratedPanel = createDecoration(renderedResult);
                    return decoratedPanel;
                }
            };

            final String label = "Value distribution for group '" + LabelUtils.getLabel(res.getName()) + "'";

            final ValueFrequency distinctValue = getDistinctValueCount(res);
            final DCCollapsiblePanel collapsiblePanel;
            if (distinctValue == null) {
                collapsiblePanel = new DCCollapsiblePanel(label, label, false, componentRef);
            } else {
                final String collapsedLabel = label + ": " + LabelUtils.getLabel(distinctValue.getValue()) + "="
                        + distinctValue.getCount() + "";
                collapsiblePanel = new DCCollapsiblePanel(collapsedLabel, label, true, componentRef);
            }
            panel.add(collapsiblePanel.toPanel());
        }
        return panel;
    }

    /**
     * Determines if a group result has just a single distinct value count. If
     * so, this value count is returned, or else null is returned.
     * 
     * @param res
     * @return
     */
    private ValueFrequency getDistinctValueCount(ValueCountingAnalyzerResult res) {
        int distinctValueCounter = 0;
        ValueFrequency valueCount = null;
        if (res.getNullCount() > 0) {
            distinctValueCounter++;
            valueCount = new SingleValueFrequency(LabelUtils.NULL_LABEL, res.getNullCount());
        }
        int uniqueCount = res.getUniqueCount();
        if (uniqueCount > 0) {
            if (uniqueCount > 1) {
                // more than one distinct value
                return null;
            }
            distinctValueCounter++;
            final Collection<String> uniqueValues = res.getUniqueValues();
            String label = LabelUtils.UNIQUE_LABEL;
            if (!uniqueValues.isEmpty()) {
                label = uniqueValues.iterator().next();
            }
            valueCount = new CompositeValueFrequency(label, 1);
        }

        if (distinctValueCounter > 1) {
            // more than one distinct value
            return null;
        }

        final Collection<ValueFrequency> valueCounts = res.getValueCounts();
        if (valueCounts.size() > 0) {
            distinctValueCounter += valueCounts.size();
            valueCount = valueCounts.iterator().next();
        }
        if (distinctValueCounter > 1) {
            // more than one distinct value
            return null;
        }
        return valueCount;
    }

    private DCPanel createDecoration(JComponent renderedResult) {
        final DCPanel wrappingPanel = new DCPanel();
        wrappingPanel.setLayout(new BorderLayout());
        wrappingPanel.add(renderedResult, BorderLayout.CENTER);
        wrappingPanel.setBorder(new EmptyBorder(4, 20, 4, 4));
        return wrappingPanel;
    }

    /**
     * A main method that will display the results of a few example value
     * distributions. Useful for tweaking the charts and UI.
     * 
     * @param args
     */
    public static void main(String[] args) {
        LookAndFeelManager.get().init();

        Injector injector = Guice.createInjector(new DCModuleImpl());

        // run a small job
        final AnalysisJobBuilder ajb = injector.getInstance(AnalysisJobBuilder.class);
        Datastore ds = injector.getInstance(DatastoreCatalog.class).getDatastore("orderdb");
        DatastoreConnection con = ds.openConnection();
        SchemaNavigator sn = con.getSchemaNavigator();
        ajb.setDatastore(ds);
        ajb.addSourceColumns(sn.convertToTable("PUBLIC.CUSTOMERS").getColumns());

        AnalyzerJobBuilder<ValueDistributionAnalyzer> singleValueDist = ajb
                .addAnalyzer(ValueDistributionAnalyzer.class);
        singleValueDist.addInputColumn(ajb.getSourceColumnByName("PUBLIC.CUSTOMERS.ADDRESSLINE2"));

        AnalyzerJobBuilder<ValueDistributionAnalyzer> groupedValueDist = ajb
                .addAnalyzer(ValueDistributionAnalyzer.class);
        groupedValueDist.addInputColumn(ajb.getSourceColumnByName("PUBLIC.CUSTOMERS.CITY"));
        groupedValueDist.setConfiguredProperty("Group column", ajb.getSourceColumnByName("PUBLIC.CUSTOMERS.COUNTRY"));

        ResultWindow resultWindow = injector.getInstance(ResultWindow.class);
        resultWindow.setVisible(true);
        resultWindow.startAnalysis();
    }
}
