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

import junit.framework.TestCase;
import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;
import dk.eobjects.metamodel.schema.Table;

public class TableDataSelectionComboBoxTest extends TestCase {

	public void testObserverBehaviour() throws Exception {
		ColumnSelection dataSelection = new ColumnSelection(null);
		TableDataSelectionComboBox comboBox = new TableDataSelectionComboBox(
				dataSelection);
		assertEquals(0, comboBox.getItemCount());
		assertNull(comboBox.getSelectedTable());

		Table table = new Table("My table");
		Column column = new Column("My column", ColumnType.VARCHAR);
		column.setTable(table);
		dataSelection.toggleColumn(column);

		assertEquals(1, comboBox.getItemCount());
		assertSame(table, comboBox.getSelectedTable());

		Table table2 = new Table("My table2");
		Column column2 = new Column("My column2", ColumnType.VARCHAR);
		column2.setTable(table2);
		dataSelection.toggleColumn(column2);

		assertEquals(2, comboBox.getItemCount());
		assertSame(table, comboBox.getSelectedTable());
	}
}