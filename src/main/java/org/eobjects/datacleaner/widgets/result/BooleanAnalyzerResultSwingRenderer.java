package org.eobjects.datacleaner.widgets.result;

import java.awt.FlowLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.result.BooleanAnalyzerResult;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.actions.InvokeResultProducerActionListener;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.table.DCTable;
import org.jdesktop.swingx.VerticalLayout;

@RendererBean(SwingRenderingFormat.class)
public class BooleanAnalyzerResultSwingRenderer implements Renderer<BooleanAnalyzerResult, JComponent> {

	@Override
	public JComponent render(BooleanAnalyzerResult result) {
		DCPanel panel = new DCPanel();
		panel.setLayout(new VerticalLayout(4));

		DCTable detailsTable = new CrosstabResultSwingRenderer().render(result);

		DCTable aggregationsTable = new DCTable();
		TableModel aggregationsTableModel = new DefaultTableModel(new String[] { "Aggregate", "All columns" }, 3);
		aggregationsTableModel.setValueAt("Multiple true values (Ambiguous matches)", 0, 0);
		aggregationsTableModel.setValueAt(createCell("Multiple true values", result.getMultipleTrueRows()), 0, 1);
		aggregationsTableModel.setValueAt("Entirely true values (Matches all)", 1, 0);
		aggregationsTableModel.setValueAt(createCell("Entirely true values", result.getOnlyTrueRows()), 1, 1);
		aggregationsTableModel.setValueAt("Entirely false values (No matches)", 2, 0);
		aggregationsTableModel.setValueAt(createCell("Entirely false values", result.getOnlyFalseRows()), 2, 1);
		aggregationsTable.setModel(aggregationsTableModel);

		panel.add(detailsTable);

		aggregationsTable.setColumnControlVisible(false);
		DCPanel aggregationsTablePanel = aggregationsTable.toPanel();
		aggregationsTablePanel.setBorder(WidgetUtils.BORDER_THIN);

		panel.add(aggregationsTablePanel);
		return panel;
	}

	private DCPanel createCell(String drillToDetailWindowTitle, AnnotatedRowsResult annotatedRowResult) {
		JButton button = WidgetFactory.createSmallButton("images/actions/drill-to-detail.png");
		button.setMargin(new Insets(0, 0, 0, 0));
		button.addActionListener(new InvokeResultProducerActionListener(drillToDetailWindowTitle, annotatedRowResult));

		DCPanel panel = new DCPanel();
		panel.setLayout(new FlowLayout(SwingConstants.RIGHT, 0, 0));
		panel.add(new JLabel(Integer.toString(annotatedRowResult.getRowCount())));
		panel.add(Box.createHorizontalStrut(4));
		panel.add(button);

		return panel;
	}
}
