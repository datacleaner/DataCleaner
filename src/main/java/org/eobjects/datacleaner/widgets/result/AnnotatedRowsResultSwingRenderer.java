package org.eobjects.datacleaner.widgets.result;

import javax.swing.JPanel;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.widgets.table.ColumnHighlighter;
import org.eobjects.datacleaner.widgets.table.DCTable;

@RendererBean(SwingRenderingFormat.class)
public class AnnotatedRowsResultSwingRenderer implements Renderer<AnnotatedRowsResult, JPanel> {

	@Override
	public JPanel render(AnnotatedRowsResult result) {
		DCTable table = new DCTable(result.toTableModel());

		InputColumn<?>[] highlightedColumns = result.getHighlightedColumns();
		if (result.getInputColumns().size() > highlightedColumns.length) {
			// if there's context information available (columns besides the
			// actual columns of interest) then highlight the columns of
			// interest.
			if (highlightedColumns.length > 0) {
				int[] highligthedColumnIndexes = new int[highlightedColumns.length];
				for (int i = 0; i < highligthedColumnIndexes.length; i++) {
					highligthedColumnIndexes[i] = result.getColumnIndex(highlightedColumns[i]);
				}

				table.addHighlighter(new ColumnHighlighter(highligthedColumnIndexes));
			}
		}
		return table.toPanel();
	}

}
