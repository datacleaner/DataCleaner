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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.ResultProducer;
import org.eobjects.analyzer.result.renderer.CrosstabRenderer;
import org.eobjects.analyzer.result.renderer.CrosstabRendererCallback;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.ChartUtils;
import org.eobjects.datacleaner.util.LabelUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.widgets.Alignment;
import org.eobjects.datacleaner.widgets.table.DCTable;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public abstract class AbstractCrosstabResultSwingRenderer<R extends CrosstabResult> implements Renderer<R, JComponent> {

	final DrillToDetailsCallback _drillToDetailsCallback = new DrillToDetailsCallbackImpl();

	@Override
	public JComponent render(R result) {
		final DCTable table = renderTable(result.getCrosstab());

		final JComponent tableComponent;
		if ("".equals(table.getColumnName(1))) {
			tableComponent = table;
		} else {
			tableComponent = table.toPanel();
		}

		final JXCollapsiblePane chartContainer = new JXCollapsiblePane(Direction.UP);
		chartContainer.setCollapsed(true);

		final DisplayChartCallback displayChartCallback = new DisplayChartCallbackImpl(chartContainer);
		decorate(result, table, displayChartCallback);

		final DCPanel resultPanel = new DCPanel();
		resultPanel.setLayout(new BorderLayout());
		resultPanel.add(chartContainer, BorderLayout.NORTH);
		resultPanel.add(tableComponent, BorderLayout.CENTER);
		return resultPanel;
	}

	protected void decorate(R result, DCTable table, DisplayChartCallback displayChartCallback) {
		if (result.getCrosstab().getDimensionCount() == 2 && table.getColumnCount() > 2) {
			addDefaultBarCharts(table, displayChartCallback);
		}
	}

	protected void addDefaultBarCharts(DCTable table, DisplayChartCallback displayChartCallback) {
		final int rowCount = table.getRowCount();
		final int columnCount = table.getColumnCount();
		for (int i = 0; i < rowCount; i++) {
			boolean entirelyNumbers = true;
			for (int j = 1; j < columnCount; j++) {
				Object value = table.getValueAt(i, j);
				if (!(value instanceof Number)) {
					entirelyNumbers = false;
					break;
				}
			}
			if (entirelyNumbers) {
				Object firstRowCell = table.getValueAt(i, 0);
				if (firstRowCell instanceof String) {
					String measureName = firstRowCell.toString();
					addDefaultBarChart(table, displayChartCallback, i, measureName);
				}
			}
		}
	}

	protected void addDefaultBarChart(final DCTable table, final DisplayChartCallback displayChartCallback, final int row,
			final String measureName) {
		final ActionListener action = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

				final int columnCount = table.getColumnCount();
				for (int j = 1; j < columnCount; j++) {
					final Number value = (Number) table.getValueAt(row, j);
					dataset.setValue(value, table.getColumnName(j), "");
				}

				final JFreeChart chart = ChartFactory.createBarChart3D("", "", measureName, dataset,
						PlotOrientation.VERTICAL, true, true, false);
				ChartUtils.applyStyles(chart);
				final ChartPanel chartPanel = new ChartPanel(chart);
				displayChartCallback.displayChart(chartPanel);
			}
		};

		final DCPanel panel = createActionableValuePanel(measureName, Alignment.LEFT, action, "images/chart-types/bar.png");
		table.setValueAt(panel, row, 0);
	}

	public DCTable renderTable(Crosstab<?> crosstab) {
		final CrosstabRenderer renderer = new CrosstabRenderer(crosstab);
		final RendererCallback rendererCallback = new RendererCallback();
		final TableModel tableModel = renderer.render(rendererCallback);
		final Alignment alignment = rendererCallback.getAlignment();
		final DCTable table = new DCTable(tableModel);

		table.setSortable(false);
		table.setAlignment(0, Alignment.LEFT);
		final int columnCount = table.getColumnCount();
		for (int i = 1; i < columnCount; i++) {
			table.setAlignment(i, alignment);
		}

		if (columnCount >= 10) {
			table.setHorizontalScrollEnabled(true);
		}

		table.setRowHeight(22);
		return table;
	}

	protected void horizontalHeaderCell(String category, TableModel tableModel, int row, int col) {
		if (row >= 0) {
			tableModel.setValueAt(category, row, col);
		}
	}

	protected void verticalHeaderCell(String category, TableModel tableModel, int row, int col) {
		if (row >= 0) {
			tableModel.setValueAt(category, row, col);
		}
	}

	protected void valueCell(Object value, final ResultProducer drillToDetailResultProducer, TableModel tableModel, int row,
			int col, boolean headersIncluded, Alignment alignment) {
		final Object resultValue;

		ActionListener action = null;
		if (drillToDetailResultProducer != null) {
			final StringBuilder sb = new StringBuilder("Detailed result for [");

			sb.append(getLabelText(value));
			sb.append(" (");

			final String cat1;
			if (headersIncluded) {
				cat1 = tableModel.getColumnName(col);
			} else {
				cat1 = tableModel.getValueAt(0, col).toString();
			}
			sb.append(cat1).append(", ");

			final String cat2 = tableModel.getValueAt(row, 0).toString();
			sb.append(cat2);

			sb.append(")]");

			action = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					_drillToDetailsCallback.drillToDetails(sb.toString(), drillToDetailResultProducer);
				}
			};
			resultValue = createActionableValuePanel(value, alignment, action, "images/actions/drill-to-detail.png");
		} else {
			resultValue = value;
		}

		tableModel.setValueAt(resultValue, row, col);
	}

	private final class RendererCallback implements CrosstabRendererCallback<TableModel> {

		private boolean headersIncluded;
		private TableModel _tableModel;
		private int _row = 0;
		private int _col = 0;
		private Alignment _alignment = Alignment.LEFT;

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

			final String[] columnNames = new String[cols];
			if (horizontalDimensions.size() == 1) {
				headersIncluded = true;

				final CrosstabDimension horizontalDimension = horizontalDimensions.get(0);
				final List<String> categories = horizontalDimension.getCategories();
				columnNames[0] = "";
				for (int i = 1; i < columnNames.length; i++) {
					columnNames[i] = categories.get(i - 1);
				}

				// minus one row, because the header is included
				rows--;
				_row--;
			} else {
				headersIncluded = false;
				for (int i = 0; i < columnNames.length; i++) {
					columnNames[i] = "";
				}
			}
			_tableModel = new DefaultTableModel(columnNames, rows);

			if (ReflectionUtils.isNumber(crosstab.getValueClass())) {
				_alignment = Alignment.RIGHT;
			}
		}

		public Alignment getAlignment() {
			return _alignment;
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
			AbstractCrosstabResultSwingRenderer.this.horizontalHeaderCell(category, _tableModel, _row, _col);
			_col++;
		}

		@Override
		public void verticalHeaderCell(String category, CrosstabDimension dimension, int height) {
			AbstractCrosstabResultSwingRenderer.this.verticalHeaderCell(category, _tableModel, _row, _col);
			_col++;
		}

		@Override
		public void valueCell(Object value, final ResultProducer drillToDetailResultProducer) {
			AbstractCrosstabResultSwingRenderer.this.valueCell(value, drillToDetailResultProducer, _tableModel, _row, _col,
					headersIncluded, _alignment);
			_col++;
		}

		@Override
		public TableModel getResult() {
			return _tableModel;
		}

		@Override
		public void emptyHeader(CrosstabDimension verticalDimension, CrosstabDimension horizontalDimension) {
			if (_row >= 0) {
				_tableModel.setValueAt("", _row, _col);
			}
			_col++;
		}
	}

	private static String getLabelText(Object value) {
		if (value == null) {
			return LabelUtils.NULL_LABEL;
		} else if (value instanceof Double || value instanceof Float) {
			return NumberFormat.getInstance().format(value);
		} else {
			return value.toString();
		}
	}

	public static DCPanel createActionableValuePanel(Object value, Alignment alignment, ActionListener action,
			String iconImagePath) {
		final JLabel label = new JLabel(getLabelText(value));
		final DCPanel panel = new DCPanel();
		panel.add(label);

		panel.setLayout(new FlowLayout(alignment.getFlowLayoutAlignment(), 0, 0));

		if (action != null && iconImagePath != null) {
			final JButton button = WidgetFactory.createSmallButton(iconImagePath);
			button.addActionListener(action);
			panel.add(Box.createHorizontalStrut(4));
			panel.add(button);
		}
		
		return panel;
	}
}
