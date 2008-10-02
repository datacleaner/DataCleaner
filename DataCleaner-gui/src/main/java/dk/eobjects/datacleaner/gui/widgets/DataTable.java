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
package dk.eobjects.datacleaner.gui.widgets;

import javax.swing.table.TableModel;

import dk.eobjects.metamodel.data.DataSet;

public class DataTable extends DataCleanerTable {

	private static final long serialVersionUID = -7273690692033185976L;

	public DataTable(DataSet data) {
		super();
		updateTable(data);
	}

	public void updateTable(DataSet data) {
		TableModel tableModel = data.toTableModel();
		setModel(tableModel);
	}

	@Override
	public Object getValueAt(int row, int column) {
		Object value = super.getValueAt(row, column);
		if (value instanceof String) {
			String string = (String) value;
			if (string.startsWith(" ") || string.endsWith(" ")
					|| string.trim().equals("")) {
				value = '\"' + string + '\"';
			}
		}
		return value;
	}
}