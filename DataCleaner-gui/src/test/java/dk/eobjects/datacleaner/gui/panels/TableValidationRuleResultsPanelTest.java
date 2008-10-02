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
package dk.eobjects.datacleaner.gui.panels;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import dk.eobjects.datacleaner.gui.widgets.DataTable;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;

public class TableValidationRuleResultsPanelTest extends TestCase {

	/**
	 * Ticket #132: Can't show COUNT(*) in DataTable on validation rule results
	 * panel
	 */
	public void testTableHeaders() throws Exception {
		Column col1 = new Column("foo");
		Column col2 = new Column("bar");
		SelectItem[] selectItems = new SelectItem[] { new SelectItem(col1),
				new SelectItem(col2), SelectItem.getCountAllItem() };

		List<Row> rows = new ArrayList<Row>();
		rows.add(new Row(selectItems, new Object[] { "f", "b", 2 }));
		DataSet data = new DataSet(rows);
		DataTable dataTable = new DataTable(data);
		assertNotNull(dataTable);
	}
}