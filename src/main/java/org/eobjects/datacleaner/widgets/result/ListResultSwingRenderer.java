package org.eobjects.datacleaner.widgets.result;

import java.util.List;

import javax.swing.JComponent;
import javax.swing.table.DefaultTableModel;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.result.ListResult;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.widgets.table.DCTable;

@RendererBean(SwingRenderingFormat.class)
public class ListResultSwingRenderer implements Renderer<ListResult<?>, JComponent> {

	@Override
	public JComponent render(ListResult<?> result) {
		List<?> values = result.getValues();
		DefaultTableModel model = new DefaultTableModel(new String[] { "Values" }, values.size());
		int i = 0;
		for (Object object : values) {
			model.setValueAt(object, i, 0);
			i++;
		}
		DCTable table = new DCTable(model);
		return table.toPanel();
	}

}
