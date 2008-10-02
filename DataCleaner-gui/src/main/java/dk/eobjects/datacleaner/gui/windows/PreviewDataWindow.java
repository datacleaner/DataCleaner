/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.gui.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;

import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.widgets.DataTable;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public class PreviewDataWindow extends AbstractWindow {

	private String _title;

	@Override
	public void disposeInternal() {
		super.disposeInternal();
		_title = null;
	}

	public PreviewDataWindow(Table table, Column[] columns,
			DataContext dataContext, int numRows) {
		super();
		_title = "Preview: " + table.getName();
		if (columns == null || columns.length == 0) {
			columns = table.getColumns();
		}
		Query q = new Query().from(table).select(columns).setMaxRows(numRows);
		DataSet data = dataContext.executeQuery(q);

		_panel.setLayout(new BorderLayout());
		DataTable dataTable = new DataTable(data);
		JScrollPane scrollPane = new JScrollPane(dataTable.toPanel());
		_panel.add(scrollPane, BorderLayout.CENTER);

		Dimension tableSize = dataTable.getPreferredSize();
		_panel.setPreferredSize(new Dimension(tableSize.width + 60, 400));
	}

	@Override
	public ImageIcon getFrameIcon() {
		return GuiHelper.getImageIcon("images/database_table.png");
	}

	@Override
	public String getTitle() {
		return _title;
	}
}