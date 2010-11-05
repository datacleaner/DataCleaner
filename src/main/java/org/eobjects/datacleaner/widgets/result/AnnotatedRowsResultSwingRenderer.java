package org.eobjects.datacleaner.widgets.result;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.table.TableModel;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.widgets.table.ColumnHighlighter;
import org.eobjects.datacleaner.widgets.table.DCTable;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.decorator.Highlighter;

@RendererBean(SwingRenderingFormat.class)
public class AnnotatedRowsResultSwingRenderer implements Renderer<AnnotatedRowsResult, JPanel> {

	private static final String[] VIEWS = new String[] { "View detailed rows", "View distinct values & counts" };

	@Override
	public JPanel render(final AnnotatedRowsResult result) {
		DCPanel panel = new DCPanel();
		panel.setLayout(new VerticalLayout(4));

		final DCTable table = new DCTable();

		InputColumn<?>[] highlightedColumns = result.getHighlightedColumns();
		List<InputColumn<?>> inputColumns = result.getInputColumns();

		if (highlightedColumns.length == 1 && inputColumns.size() > 1) {
			final JComboBox comboBox = new JComboBox(VIEWS);
			comboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (comboBox.getSelectedItem() == VIEWS[0]) {
						applyDetailedView(table, result);
					} else {
						applyDistinctValuesView(table, result);
					}
				}
			});
			comboBox.setSelectedItem(VIEWS[0]);

			DCPanel buttonPanel = new DCPanel();
			buttonPanel.setLayout(new HorizontalLayout(4));
			buttonPanel.add(comboBox);
			panel.add(buttonPanel);
		} else if (inputColumns.size() == 1) {
			applyDistinctValuesView(table, result);
		} else {
			applyDetailedView(table, result);
		}

		panel.add(table.toPanel());
		return panel;
	}

	private void applyDistinctValuesView(DCTable table, AnnotatedRowsResult result) {
		InputColumn<?>[] highlightedColumns = result.getHighlightedColumns();
		TableModel tableModel = result.toDistinctValuesTableModel(highlightedColumns[0]);
		table.setModel(tableModel);
		table.setHighlighters(new Highlighter[0]);
	}

	private void applyDetailedView(DCTable table, AnnotatedRowsResult result) {
		table.setModel(result.toTableModel());
		InputColumn<?>[] highlightedColumns = result.getHighlightedColumns();
		List<InputColumn<?>> inputColumns = result.getInputColumns();

		if (inputColumns.size() > highlightedColumns.length) {
			// if there's context information available (columns
			// besides the actual columns of interest) then highlight the
			// columns of interest.
			if (highlightedColumns.length > 0) {
				int[] highligthedColumnIndexes = new int[highlightedColumns.length];
				for (int i = 0; i < highligthedColumnIndexes.length; i++) {
					highligthedColumnIndexes[i] = result.getColumnIndex(highlightedColumns[i]);
				}

				table.addHighlighter(new ColumnHighlighter(highligthedColumnIndexes));
			}
		}
	}
}
