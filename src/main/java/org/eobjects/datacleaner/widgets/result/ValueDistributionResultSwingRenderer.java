/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.widgets.result;

import java.awt.BorderLayout;
import java.util.List;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.beans.valuedist.ValueCount;
import org.eobjects.analyzer.result.ValueDistributionResult;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.LabelConstants;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

@RendererBean(SwingRenderingFormat.class)
public class ValueDistributionResultSwingRenderer implements Renderer<ValueDistributionResult, DCPanel> {

	@Override
	public DCPanel render(ValueDistributionResult result) {
		DefaultPieDataset dataset = new DefaultPieDataset();

		List<ValueCount> valueCounts = result.getTopValues().getValueCounts();
		for (ValueCount valueCount : valueCounts) {
			dataset.setValue(valueCount.getValue(), valueCount.getCount());
		}
		int nullCount = result.getNullCount();
		if (nullCount > 0) {
			dataset.setValue(LabelConstants.NULL_LABEL, nullCount);
		}
		int uniqueCount = result.getUniqueCount();
		if (uniqueCount > 0) {
			dataset.setValue(LabelConstants.UNIQUE_LABEL, uniqueCount);
		}

		JFreeChart chart = ChartFactory.createPieChart3D(result.getColumnName(), dataset, true, true, false);
		ChartPanel chartPanel = new ChartPanel(chart);
		DCPanel panel = new DCPanel();
		panel.setLayout(new BorderLayout());
		panel.add(chartPanel, BorderLayout.CENTER);
		panel.setSize(400, 400);
		return panel;
	}

}
