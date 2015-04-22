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

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.table.DefaultTableModel;

import org.datacleaner.api.RendererBean;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.result.CategorizationResult;
import org.datacleaner.result.renderer.SwingRenderingFormat;
import org.datacleaner.util.ChartUtils;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.table.DCTable;
import org.jdesktop.swingx.VerticalLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

@RendererBean(SwingRenderingFormat.class)
public class CategorizationResultSwingRenderer extends CategorizationResultAbstractSwingRenderer<CategorizationResult> {

    public CategorizationResultSwingRenderer() {
    }

    @Override
    public JComponent render(CategorizationResult analyzerResult) {
        final DefaultPieDataset dataset = new DefaultPieDataset();
        final DefaultTableModel model = prepareModel(analyzerResult, dataset);

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
}
