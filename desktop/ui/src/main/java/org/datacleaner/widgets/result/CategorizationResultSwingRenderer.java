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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.table.DefaultTableModel;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.Provided;
import org.datacleaner.api.RendererBean;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.result.AnnotatedRowsResult;
import org.datacleaner.result.CategorizationResult;
import org.datacleaner.result.renderer.AbstractRenderer;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.result.renderer.SwingRenderingFormat;
import org.datacleaner.util.ChartUtils;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.table.DCTable;
import org.datacleaner.windows.DetailsResultWindow;
import org.jdesktop.swingx.VerticalLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

@RendererBean(SwingRenderingFormat.class)
public class CategorizationResultSwingRenderer extends AbstractRenderer<CategorizationResult, JComponent> {

    @Inject
    @Provided
    WindowContext windowContext;

    @Inject
    @Provided
    RendererFactory rendererFactory;

    public CategorizationResultSwingRenderer() {
    }

    @Override
    public JComponent render(CategorizationResult analyzerResult) {
        final DefaultTableModel model = new DefaultTableModel(new Object[] { "Match outcome", LabelUtils.COUNT_LABEL },
                3);
        final DefaultPieDataset dataset = new DefaultPieDataset();
        int row = 0;
        final Collection<String> categoryNames = analyzerResult.getCategoryNames();
        for (String categoryName : categoryNames) {
            final AnnotatedRowsResult sample = analyzerResult.getCategoryRowSample(categoryName);
            final Number count = analyzerResult.getCategoryCount(categoryName);
            row = addValue(row, dataset, model, categoryName, count.intValue(), sample);
        }

        final DCTable table = new DCTable(model);
        table.setColumnControlVisible(false);
        table.setRowHeight(22);

        final JFreeChart chart = ChartFactory.createPieChart(null, dataset, true, false, false);
        ChartUtils.applyStyles(chart);

        final ChartPanel chartPanel = new ChartPanel(chart);

        final DCPanel leftPanel = WidgetUtils.decorateWithShadow(chartPanel);

        final DCPanel rightPanel = new DCPanel();
        rightPanel.setLayout(new VerticalLayout());
        rightPanel.add(WidgetUtils.decorateWithShadow(table.toPanel()));

        final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setOpaque(false);
        split.add(leftPanel);
        split.add(rightPanel);
        split.setDividerLocation(550);

        return split;
    }

    private int addValue(final int row, final DefaultPieDataset dataset, final DefaultTableModel model,
            final String desc, final int count, final AnnotatedRowsResult sampleResult) {
        dataset.setValue(desc, count);
        model.setValueAt(desc, row, 0);
        if (sampleResult == null || count == 0) {
            model.setValueAt(count, row, 1);
        } else {
            final DCPanel panel = new DCPanel();
            panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

            final JLabel label = new JLabel(count + "");
            final JButton button = WidgetFactory.createSmallButton(IconUtils.ACTION_DRILL_TO_DETAIL);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    drillToGroup(desc, sampleResult);
                }

            });

            panel.add(label);
            panel.add(Box.createHorizontalStrut(4));
            panel.add(button);

            model.setValueAt(panel, row, 1);
        }
        return row + 1;
    }

    private void drillToGroup(String title, AnnotatedRowsResult sampleResult) {
        List<AnalyzerResult> results = Arrays.<AnalyzerResult> asList(sampleResult);
        final DetailsResultWindow window = new DetailsResultWindow(title, results, windowContext, rendererFactory);
        window.open();
    }
}
