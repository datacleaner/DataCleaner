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

import java.util.List;

import javax.swing.JComponent;
import javax.swing.table.DefaultTableModel;

import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.result.ListResult;
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
