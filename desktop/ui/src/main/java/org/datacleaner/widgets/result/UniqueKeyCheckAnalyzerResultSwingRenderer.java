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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.table.DefaultTableModel;

import org.datacleaner.api.RendererBean;
import org.datacleaner.beans.uniqueness.UniqueKeyCheckAnalyzerResult;
import org.datacleaner.result.renderer.AbstractRenderer;
import org.datacleaner.result.renderer.SwingRenderingFormat;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.ChartUtils;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.table.DCTable;
import org.jdesktop.swingx.VerticalLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.title.ShortTextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.general.DefaultPieDataset;

@RendererBean(SwingRenderingFormat.class)
public class UniqueKeyCheckAnalyzerResultSwingRenderer extends AbstractRenderer<UniqueKeyCheckAnalyzerResult, JComponent> {

    @Override
    public JComponent render(UniqueKeyCheckAnalyzerResult result) {
        final int nullCount = result.getNullCount();
        final int nonUniqueCount = result.getNonUniqueCount();

        final DefaultKeyedValues keyedValues = new DefaultKeyedValues();
        keyedValues.addValue("Unique keys", result.getUniqueCount());

        final List<Title> subTitles = new ArrayList<Title>();
        subTitles.add(new ShortTextTitle("Row count: " + result.getRowCount()));
        subTitles.add(new ShortTextTitle("Unique key count: " + result.getUniqueCount()));

        if (nonUniqueCount > 0) {
            keyedValues.addValue("Non-unique keys", nonUniqueCount);
            subTitles.add(new ShortTextTitle("Non-unique key count: " + nonUniqueCount));
        }

        final String title;
        if (nullCount == 0) {
            title = "Unique and non-unique keys";
        } else {
            keyedValues.addValue(LabelUtils.NULL_LABEL, nullCount);
            title = "Unique, non-unique and <null> keys";
            subTitles.add(new ShortTextTitle("<null> key count: " + result.getNullCount()));
        }

        final DefaultPieDataset dataset = new DefaultPieDataset(keyedValues);
        final JFreeChart chart = ChartFactory.createRingChart(title, dataset, true, true, false);

        chart.setSubtitles(subTitles);

        ChartUtils.applyStyles(chart);
        ChartPanel chartPanel = new ChartPanel(chart);

        final DCPanel leftPanel = new DCPanel();
        leftPanel.setLayout(new VerticalLayout());
        leftPanel.add(WidgetUtils.decorateWithShadow(chartPanel));

        final Map<String, Integer> samples = result.getNonUniqueSamples();
        if (samples == null || samples.isEmpty()) {
            return leftPanel;
        }

        final DefaultTableModel samplesTableModel = new DefaultTableModel(new String[] { "Key", "Count" }, 0);
        for (final Entry<String, Integer> entry : samples.entrySet()) {
            final String key = entry.getKey();
            final Integer count = entry.getValue();
            samplesTableModel.addRow(new Object[] { key, count });
        }
        final DCTable samplesTable = new DCTable(samplesTableModel);

        final DCPanel rightPanel = new DCPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(DCLabel.dark("Sample non-unique keys:"));
        rightPanel.add(samplesTable.toPanel(), BorderLayout.CENTER);

        final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setOpaque(false);
        split.add(leftPanel);
        split.add(rightPanel);
        split.setDividerLocation(550);

        return split;
    }

}
