package org.eobjects.datacleaner.widgets.result;

import javax.swing.JComponent;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.result.DataSetResult;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.widgets.table.DCTable;

import dk.eobjects.metamodel.data.DataSet;

@RendererBean(SwingRenderingFormat.class)
public class DataSetResultSwingRenderer implements Renderer<DataSetResult, JComponent> {

	@Override
	public JComponent render(DataSetResult result) {
		DataSet dataSet = result.getDataSet();
		return new DCTable(dataSet.toTableModel()).toPanel();
	}

}
