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
package dk.eobjects.datacleaner.profiler.pattern;

import java.sql.Connection;

import dk.eobjects.datacleaner.profiler.IProfileResult;
import dk.eobjects.datacleaner.profiler.ProfileManagerTest;
import dk.eobjects.datacleaner.testware.DataCleanerTestCase;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;

public class PatternFinderProfileTest extends DataCleanerTestCase {

	public void testProfile() throws Exception {
		ProfileManagerTest.initProfileManager();

		PatternFinderProfile columnProfile = new PatternFinderProfile();

		// Get data from test db connection
		Connection connection = getTestDbConnection();
		Schema schema = getTestDbSchema(connection);
		Table table = schema.getTableByName("PRODUCTS");
		Column productLineColumn = table.getColumnByName("PRODUCTLINE");
		DataSet data = new DataContext(connection).executeQuery(new Query()
				.from(table).select(productLineColumn));

		columnProfile.initialize(productLineColumn);

		// Exception cause by bug in JdbcDataFactory,
		// JdbcDataFactoryTest.testIntegrationSingleColumn
		while (data.next()) {
			Row row = data.getRow();
			columnProfile.process(row, 1);
		}

		IProfileResult result = columnProfile.getResult();

		assertEquals(
				"ProfileResult[profileDescriptor=BasicProfileDescriptor[displayName=Pattern finder,profileClass=class dk.eobjects.datacleaner.profiler.pattern.PatternFinderProfile],matrices={Matrix[columnNames={PRODUCTLINE},aaaaaaa aaaa={MatrixValue[value=62,detailQuery=SELECT \"PRODUCTS\".\"PRODUCTLINE\", COUNT(*) FROM APP.\"PRODUCTS\" GROUP BY \"PRODUCTS\".\"PRODUCTLINE\"]},aaaaaaaaaaa={MatrixValue[value=37,detailQuery=SELECT \"PRODUCTS\".\"PRODUCTLINE\", COUNT(*) FROM APP.\"PRODUCTS\" GROUP BY \"PRODUCTS\".\"PRODUCTLINE\"]},aaaaaa aaa aaaaa={MatrixValue[value=11,detailQuery=SELECT \"PRODUCTS\".\"PRODUCTLINE\", COUNT(*) FROM APP.\"PRODUCTS\" GROUP BY \"PRODUCTS\".\"PRODUCTLINE\"]}]}]",
				result.toString());
	}
}