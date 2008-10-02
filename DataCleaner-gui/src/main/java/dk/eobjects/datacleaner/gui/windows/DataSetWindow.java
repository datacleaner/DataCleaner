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
import dk.eobjects.metamodel.data.DataSet;

public class DataSetWindow extends AbstractWindow {

	private DataTable _dataTable;
	private String _title;

	@Override
	public void disposeInternal() {
		super.disposeInternal();
		_dataTable = null;
		_title = null;
	}

	public DataSetWindow(String title, DataSet dataSet) {
		super();
		_title = title;
		_dataTable = new DataTable(dataSet);

		_panel.setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(_dataTable.toPanel());
		_panel.add(scrollPane, BorderLayout.CENTER);

		Dimension tableSize = _dataTable.getPreferredSize();
		_panel.setPreferredSize(new Dimension(tableSize.width + 60, 400));
	}

	@Override
	public ImageIcon getFrameIcon() {
		return GuiHelper.getImageIcon("images/database_table.png");
	}

	public void updateDataSet(DataSet data) {
		_dataTable.updateTable(data);
	}

	@Override
	public String getTitle() {
		return _title;
	}
}