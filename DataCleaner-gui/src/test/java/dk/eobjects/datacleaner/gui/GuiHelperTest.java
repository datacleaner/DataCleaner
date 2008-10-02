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
package dk.eobjects.datacleaner.gui;

import junit.framework.TestCase;
import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;
import dk.eobjects.metamodel.schema.Table;

public class GuiHelperTest extends TestCase {

	public void testColumnLabelFunctions() throws Exception {
		ColumnSelection dataSelection = new ColumnSelection(null);
		Table table1 = new Table("table1");
		Table table2 = new Table("table2");
		Column column1 = new Column("column1", ColumnType.INTEGER);
		Column column2 = new Column("column2", ColumnType.INTEGER);
		Column column3 = new Column("column3", ColumnType.INTEGER);
		Column column4 = new Column("column4", ColumnType.INTEGER);
		table1.addColumn(column1);
		column1.setTable(table1);
		table1.addColumn(column2);
		column2.setTable(table1);
		table2.addColumn(column3);
		column3.setTable(table2);
		table2.addColumn(column4);
		column4.setTable(table2);

		dataSelection.toggleTable(table1);
		dataSelection.toggleColumn(column2);
		dataSelection.toggleColumn(column4);

		String column4Label = GuiHelper.getLabelForColumn(column4);
		assertEquals("table2.column4", column4Label);

		Object columnByLabel = GuiHelper.getColumnByLabel(dataSelection,
				column4Label);
		assertSame(columnByLabel, column4);

		String table1Label = GuiHelper.getLabelForTable(table1);
		assertEquals("table1.*", table1Label);
	}
}