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

import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
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
import org.eobjects.datacleaner.util.LabelUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.widgets.table.DCTable;

@RendererBean(SwingRenderingFormat.class)
public class CrosstabResultSwingRenderer implements Renderer<CrosstabResult, JComponent> {

	@Override
	public JComponent render(CrosstabResult result) {
		DCTable table = renderTable(result.getCrosstab());
		if ("".equals(table.getColumnName(1))) {
			return table;
		} else {
			return table.toPanel();
		}
	}

	public DCTable renderTable(Crosstab<?> crosstab) {
		CrosstabRenderer renderer = new CrosstabRenderer(crosstab);
		TableModel tableModel = renderer.render(new Callback());
		DCTable table = new DCTable(tableModel);
		table.setRowHeight(22);
		return table;
	}

	private static final class Callback implements CrosstabRendererCallback<TableModel> {

		private boolean headersIncluded;
		private TableModel _tableModel;
		private int _row = 0;
		private int _col = 0;
		private int _alignment = SwingConstants.LEFT;

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
			if (_row >= 0) {
				_tableModel.setValueAt(category, _row, _col);
			}
			_col++;
		}

		@Override
		public void verticalHeaderCell(String category, CrosstabDimension dimension, int height) {
			if (_row >= 0) {
				_tableModel.setValueAt(category, _row, _col);
			}
			_col++;
		}

		@Override
		public void valueCell(Object value, final ResultProducer drillToDetailResultProducer) {
			ActionListener action = null;
			if (drillToDetailResultProducer != null) {
				final StringBuilder sb = new StringBuilder("Detailed result for [");

				sb.append(getLabelText(value));
				sb.append(" (");

				final String cat1;
				if (headersIncluded) {
					cat1 = _tableModel.getColumnName(_col);
				} else {
					cat1 = _tableModel.getValueAt(0, _col).toString();
				}
				sb.append(cat1).append(", ");

				final String cat2 = _tableModel.getValueAt(_row, 0).toString();
				sb.append(cat2);

				sb.append(")]");

				action = new InvokeResultProducerActionListener(sb.toString(), drillToDetailResultProducer);
			}

			DCPanel panel = createActionableValuePanel(value, _alignment, action, "images/actions/drill-to-detail.png");

			_tableModel.setValueAt(panel, _row, _col);
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

	public static DCPanel createActionableValuePanel(Object value, int alignment, ActionListener action, String iconImagePath) {
		final JLabel label = new JLabel(getLabelText(value));
		final DCPanel panel = new DCPanel();
		panel.add(label);
		panel.setLayout(new FlowLayout(alignment, 0, 0));

		if (action != null && iconImagePath != null) {
			final JButton button = WidgetFactory.createSmallButton(iconImagePath);
			button.addActionListener(action);
			panel.add(Box.createHorizontalStrut(4));
			panel.add(button);
		}

		return panel;
	}
}
