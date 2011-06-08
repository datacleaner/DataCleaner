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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.table.TableModel;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.result.renderer.AbstractRenderer;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.actions.SaveDataSetActionListener;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.widgets.table.ColumnHighlighter;
import org.eobjects.datacleaner.widgets.table.DCTable;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.decorator.Highlighter;

@RendererBean(SwingRenderingFormat.class)
public class AnnotatedRowsResultSwingRenderer extends AbstractRenderer<AnnotatedRowsResult, DCPanel> {

	private static final String[] VIEWS = new String[] { "View detailed rows", "View distinct values" };

	@Override
	public DCPanel render(final AnnotatedRowsResult result) {
		DCPanel panel = new DCPanel();
		panel.setLayout(new VerticalLayout(4));

		final DCTable table = new DCTable();
		table.setColumnControlVisible(false);

		InputColumn<?>[] highlightedColumns = result.getHighlightedColumns();
		List<InputColumn<?>> inputColumns = result.getInputColumns();

		DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new HorizontalLayout(4));

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

			buttonPanel.add(comboBox);
		} else {
			applyDetailedView(table, result);
		}

		final JButton saveToFileButton = new JButton("Save dataset", ImageManager.getInstance().getImageIcon(
				"images/actions/save.png"));
		saveToFileButton.addActionListener(new SaveDataSetActionListener(result.getInputColumns(), result.getRows()));
		buttonPanel.add(saveToFileButton);

		panel.add(buttonPanel);
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
