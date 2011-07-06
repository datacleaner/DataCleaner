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

import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.result.BooleanAnalyzerResult;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.renderer.AbstractRenderer;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.table.CrosstabPanel;
import org.jdesktop.swingx.VerticalLayout;

@RendererBean(SwingRenderingFormat.class)
public class BooleanAnalyzerResultSwingRenderer extends AbstractRenderer<BooleanAnalyzerResult, JComponent> {
	
	@Inject
	@Provided
	WindowContext _windowContext;

	@Override
	public JComponent render(BooleanAnalyzerResult result) {
		DefaultCrosstabResultSwingRenderer crosstabResultSwingRenderer = new DefaultCrosstabResultSwingRenderer(_windowContext);

		Crosstab<Number> columnStatisticsCrosstab = result.getColumnStatisticsCrosstab();
		Crosstab<Number> valueCombinationCrosstab = result.getValueCombinationCrosstab();

		CrosstabPanel columnStatisticsPanel = crosstabResultSwingRenderer.renderInternal(new CrosstabResult(
				columnStatisticsCrosstab));
		if (valueCombinationCrosstab == null) {
			return columnStatisticsPanel;
		}

		CrosstabPanel valueCombinationPanel = crosstabResultSwingRenderer.renderInternal(new CrosstabResult(
				valueCombinationCrosstab));

		DCPanel panel = new DCPanel();
		panel.setLayout(new VerticalLayout(4));

		JLabel label = new JLabel("Column statistics:");
		label.setFont(WidgetUtils.FONT_HEADER1);
		panel.add(label);
		panel.add(columnStatisticsPanel);

		panel.add(Box.createVerticalStrut(4));

		label = new JLabel("Frequency of combinations:");
		label.setFont(WidgetUtils.FONT_HEADER1);
		panel.add(label);
		panel.add(valueCombinationPanel);

		return panel;
	}
}
