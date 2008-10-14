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

import java.io.File;

import junit.framework.TestCase;
import dk.eobjects.metamodel.CsvDataContextStrategy;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.schema.Table;

public class DataTableTest extends TestCase {

	public void testMoreColumnsThanRows() throws Exception {
		File file = new File("src/test/resources/more_columns_than_rows.csv");
		DataContext dataContext = new DataContext(new CsvDataContextStrategy(
				file));
		Table table = dataContext.getDefaultSchema().getTables()[0];
		DataSet data = dataContext.executeQuery(new Query().from(table).select(
				table.getColumns()));
		DataTable dataTable = new DataTable(data);
		assertEquals(4, dataTable.getColumnCount());
		assertEquals(2, dataTable.getRowCount());
	}

	public void testMoreRowsThanColumns() throws Exception {
		File file = new File("src/test/resources/more_rows_than_columns.csv");
		DataContext dataContext = new DataContext(new CsvDataContextStrategy(
				file));
		Table table = dataContext.getDefaultSchema().getTables()[0];
		DataSet data = dataContext.executeQuery(new Query().from(table).select(
				table.getColumns()));
		DataTable dataTable = new DataTable(data);
		assertEquals(4, dataTable.getColumnCount());
		assertEquals(5, dataTable.getRowCount());

		assertEquals("2", dataTable.getValueAt(2, 0).toString());
		assertEquals("\"\"", dataTable.getValueAt(2, 1).toString());
		assertEquals("\" 4\"", dataTable.getValueAt(2, 2).toString());
	}

	public void testMoreColumnsThanRowsWithSchema() throws Exception {
		File file = new File("src/test/resources/more_columns_than_rows.csv");
		DataContext dataContext = new DataContext(new CsvDataContextStrategy(
				file));
		Table table = dataContext.getDefaultSchema().getTables()[0];
		DataSet data = dataContext.executeQuery(new Query().from(table).select(
				table.getColumns()));
		DataTable dataTable = new DataTable(data);
		assertEquals(4, dataTable.getColumnCount());
		assertEquals(2, dataTable.getRowCount());
	}

	public void testMoreRowsThanColumnsWithSchema() throws Exception {
		File file = new File("src/test/resources/more_rows_than_columns.csv");
		DataContext dataContext = new DataContext(new CsvDataContextStrategy(
				file));
		Table table = dataContext.getDefaultSchema().getTables()[0];
		DataSet data = dataContext.executeQuery(new Query().from(table).select(
				table.getColumns()));
		DataTable dataTable = new DataTable(data);
		assertEquals(4, dataTable.getColumnCount());
		assertEquals(5, dataTable.getRowCount());
	}

}