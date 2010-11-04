package org.eobjects.datacleaner.widgets.result;

import java.awt.FlowLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.ResultProducer;
import org.eobjects.analyzer.result.renderer.CrosstabRenderer;
import org.eobjects.analyzer.result.renderer.CrosstabRendererCallback;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.datacleaner.actions.InvokeResultProducerActionListener;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.LabelConstants;
import org.eobjects.datacleaner.widgets.builder.WidgetFactory;
import org.eobjects.datacleaner.widgets.table.DCTable;

@RendererBean(SwingRenderingFormat.class)
public class CrosstabResultSwingRenderer implements Renderer<CrosstabResult, DCTable> {

	@Override
	public DCTable render(CrosstabResult result) {
		CrosstabRenderer renderer = new CrosstabRenderer(result.getCrosstab());
		TableModel tableModel = renderer.render(new Callback());
		DCTable table = new DCTable(tableModel);
		table.setRowHeight(22);
		return table;
	}

	private static final class Callback implements CrosstabRendererCallback<TableModel> {

		private TableModel _tableModel;
		private int _row = 0;
		private int _col = 0;
		private int _alignment = SwingConstants.LEFT;;

		@Override
		public void beginTable(Crosstab<?> crosstab, List<CrosstabDimension> horizontalDimensions,
				List<CrosstabDimension> verticalDimensions) {
			int rows = 1;
			int cols = 1;
			for (CrosstabDimension crosstabDimension : verticalDimensions) {
				rows = rows * crosstabDimension.getCategoryCount();
			}
			rows += horizontalDimensions.size();

			for (CrosstabDimension crosstabDimension : horizontalDimensions) {
				cols = cols * crosstabDimension.getCategoryCount();
			}
			cols += verticalDimensions.size();

			_tableModel = new DefaultTableModel(rows, cols);

			if (ReflectionUtils.isNumber(crosstab.getValueClass())) {
				_alignment = SwingConstants.RIGHT;
			}
		}

		@Override
		public void endTable() {
		}

		@Override
		public void beginRow() {
		}

		@Override
		public void endRow() {
			_row++;
			_col = 0;
		}

		@Override
		public void horizontalHeaderCell(String category, CrosstabDimension dimension, int width) {
			_tableModel.setValueAt(category, _row, _col);
			_col++;
		}

		@Override
		public void verticalHeaderCell(String category, CrosstabDimension dimension, int height) {
			_tableModel.setValueAt(category, _row, _col);
			_col++;
		}

		@Override
		public void valueCell(Object value, final ResultProducer drillToDetailResultProducer) {
			DCPanel panel = new DCPanel();
			panel.setLayout(new FlowLayout(_alignment, 10, 0));
			JLabel label = new JLabel();
			if (value == null) {
				label.setText(LabelConstants.NULL_LABEL);
			} else if (value instanceof Double || value instanceof Float) {
				label.setText(NumberFormat.getInstance().format(value));
			} else {
				label.setText(value.toString());
			}
			panel.add(label);

			if (drillToDetailResultProducer != null) {
				JButton button = WidgetFactory.createSmallButton("images/actions/drill-to-detail.png").toComponent();
				button.setMargin(new Insets(0, 0, 0, 0));
				button.addActionListener(new InvokeResultProducerActionListener(drillToDetailResultProducer));
				panel.add(button);
			}
			panel.setAlignmentX(_alignment);
			_tableModel.setValueAt(panel, _row, _col);
			_col++;
		}

		@Override
		public TableModel getResult() {
			return _tableModel;
		}

		@Override
		public void emptyHeader(CrosstabDimension verticalDimension, CrosstabDimension horizontalDimension) {
			_tableModel.setValueAt("", _row, _col);
			_col++;
		}
	}

}
