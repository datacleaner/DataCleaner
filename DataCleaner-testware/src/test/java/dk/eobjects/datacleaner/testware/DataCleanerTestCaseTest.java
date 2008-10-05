package dk.eobjects.datacleaner.testware;

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
import java.sql.Connection;

import org.apache.commons.lang.ArrayUtils;

import dk.eobjects.datacleaner.testware.DataCleanerTestCase;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.schema.Schema;

/**
 * Testing the testware!
 */
public class DataCleanerTestCaseTest extends DataCleanerTestCase {

	public void testGetTestDcConnection() throws Exception {
		Connection con = getTestDbConnection();
		assertNotNull(con);
		assertFalse(con.isClosed());
		assertTrue(con.isReadOnly());

		DataContext dc = getTestDataContext();
		Schema schema = dc.getDefaultSchema();
		assertEquals("JdbcSchema[name=PUBLIC]", schema.toString());
		assertEquals(
				"{JdbcTable[name=CUSTOMERS,type=TABLE,remarks=<null>],JdbcTable[name=CUSTOMER_W_TER,type=TABLE,remarks=<null>],JdbcTable[name=DEPARTMENT_MANAGERS,type=TABLE,remarks=<null>],JdbcTable[name=DIM_TIME,type=TABLE,remarks=<null>],JdbcTable[name=EMPLOYEES,type=TABLE,remarks=<null>],JdbcTable[name=OFFICES,type=TABLE,remarks=<null>],JdbcTable[name=ORDERDETAILS,type=TABLE,remarks=<null>],JdbcTable[name=ORDERFACT,type=TABLE,remarks=<null>],JdbcTable[name=ORDERS,type=TABLE,remarks=<null>],JdbcTable[name=PAYMENTS,type=TABLE,remarks=<null>],JdbcTable[name=PRODUCTS,type=TABLE,remarks=<null>],JdbcTable[name=QUADRANT_ACTUALS,type=TABLE,remarks=<null>],JdbcTable[name=TRIAL_BALANCE,type=TABLE,remarks=<null>]}",
				ArrayUtils.toString(schema.getTables()));
	}
}
