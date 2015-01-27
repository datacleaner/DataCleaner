/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.widgets.result;

import javax.swing.JComponent;
import javax.swing.table.TableModel;

import org.datacleaner.api.Renderer;
import org.datacleaner.api.RendererBean;
import org.datacleaner.api.RendererPrecedence;
import org.datacleaner.result.TableModelResult;
import org.datacleaner.result.renderer.SwingRenderingFormat;
import org.datacleaner.widgets.table.DCTable;

@RendererBean(SwingRenderingFormat.class)
public class TableModelResultSwingRenderer implements Renderer<TableModelResult, JComponent> {

	@Override
	public JComponent render(TableModelResult result) {
		TableModel tableModel = result.toTableModel();
		DCTable table = new DCTable(tableModel);
		return table.toPanel();
	}
	
	@Override
	public RendererPrecedence getPrecedence(TableModelResult renderable) {
	    return RendererPrecedence.LOW;
	}

}
