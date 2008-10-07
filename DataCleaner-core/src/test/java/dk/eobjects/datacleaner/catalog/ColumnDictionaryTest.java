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
package dk.eobjects.datacleaner.catalog;

import java.sql.Connection;

import org.apache.commons.lang.ArrayUtils;

import dk.eobjects.datacleaner.testware.DataCleanerTestCase;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.JdbcDataContextFactory;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public class ColumnDictionaryTest extends DataCleanerTestCase {

	public void testIsValid() throws Exception {
		Connection con = getTestDbConnection();
		DataContext dc = JdbcDataContextFactory.getDataContext(con);
		Table employeeTable = dc.getDefaultSchema().getTableByName("EMPLOYEES");

		Column column = employeeTable.getColumnByName("LASTNAME");
		ColumnDictionary dictionary = new ColumnDictionary("my_dictionary", dc,
				column);

		assertEquals("{false,false}", ArrayUtils.toString(dictionary.isValid(
				"foo", "bar")));

		assertEquals("{false,true,false,true}", ArrayUtils.toString(dictionary
				.isValid("foo", "Jones", "bar", "Nishi")));

		assertEquals(
				"{true,true,true,true,true,true,true,true,true,true,true}",
				ArrayUtils.toString(dictionary.isValid("Jones", "Nishi",
						"Fixter", "Marsh", "King", "Kato", "Bow", "Jennings",
						"Murphy", "Patterson", "Firrelli")));
	}

	/**
	 * Ticket #164: Database dictionaries fail if prompted for values with
	 * single quotes
	 */
	public void testValueWithSingleQuote() throws Exception {
		Connection con = getTestDbConnection();
		DataContext dc = JdbcDataContextFactory.getDataContext(con);
		Table employeeTable = dc.getDefaultSchema().getTableByName("CUSTOMERS");

		Column column = employeeTable.getColumnByName("CUSTOMERNAME");
		ColumnDictionary dictionary = new ColumnDictionary("my_dictionary", dc,
				column);

		assertEquals("{false}", ArrayUtils.toString(dictionary
				.isValid("kasper's source's site")));

		boolean[] valid = dictionary.isValid("Anna's Decorations, Ltd");
		assertEquals("{true}", ArrayUtils.toString(valid));

		assertEquals("{false,true,true}", ArrayUtils.toString(dictionary
				.isValid("kasper's source's site", "Marta's Replicas Co.",
						"Anna's Decorations, Ltd")));
	}

	public void testValueNull() throws Exception {
		Connection con = getTestDbConnection();
		DataContext dc = JdbcDataContextFactory.getDataContext(con);
		Table employeeTable = dc.getDefaultSchema().getTableByName("CUSTOMERS");

		Column column = employeeTable.getColumnByName("CUSTOMERNAME");
		ColumnDictionary dictionary = new ColumnDictionary("my_dictionary", dc,
				column);

		assertEquals("{false,false,true}", ArrayUtils.toString(dictionary
				.isValid("kasper's source's site", null,
						"Anna's Decorations, Ltd")));
	}
}