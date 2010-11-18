package org.eobjects.datacleaner.widgets.result;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.result.BooleanAnalyzerResult;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.table.DCTable;
import org.jdesktop.swingx.VerticalLayout;

@RendererBean(SwingRenderingFormat.class)
public class BooleanAnalyzerResultSwingRenderer implements Renderer<BooleanAnalyzerResult, JComponent> {

	@Override
	public JComponent render(BooleanAnalyzerResult result) {
		CrosstabResultSwingRenderer crosstabResultSwingRenderer = new CrosstabResultSwingRenderer();

		Crosstab<Number> columnStatisticsCrosstab = result.getColumnStatisticsCrosstab();
		Crosstab<Number> valueCombinationCrosstab = result.getValueCombinationCrosstab();

		DCTable columnStatisticsTable = crosstabResultSwingRenderer.render(columnStatisticsCrosstab);
		if (valueCombinationCrosstab == null) {
			return columnStatisticsTable;
		}

		DCTable valueCombinationTable = crosstabResultSwingRenderer.render(valueCombinationCrosstab);

		DCPanel panel = new DCPanel();
		panel.setLayout(new VerticalLayout(4));

		JLabel label = new JLabel("Column statistics:");
		label.setFont(WidgetUtils.FONT_HEADER);
		panel.add(label);
		panel.add(columnStatisticsTable);
		
		panel.add(Box.createVerticalStrut(4));

		label = new JLabel("Frequency of combinations:");
		label.setFont(WidgetUtils.FONT_HEADER);
		panel.add(label);
		panel.add(valueCombinationTable);

		return panel;
	}
}
