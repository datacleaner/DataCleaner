package org.eobjects.datacleaner.widgets.result;

import javax.swing.JComponent;
import javax.swing.table.TableModel;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.result.TableModelResult;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.widgets.table.DCTable;

@RendererBean(SwingRenderingFormat.class)
public class TableModelResultSwingRenderer implements Renderer<TableModelResult, JComponent> {

	@Override
	public JComponent render(TableModelResult result) {
		TableModel tableModel = result.toTableModel();
		return new DCTable(tableModel).toPanel();
	}

}
