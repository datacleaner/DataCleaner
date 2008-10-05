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
package dk.eobjects.datacleaner.profiler.trivial;

import java.sql.Connection;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.commons.lang.ArrayUtils;

import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.IProfileResult;
import dk.eobjects.datacleaner.profiler.MatrixValue;
import dk.eobjects.datacleaner.profiler.ProfileManagerTest;
import dk.eobjects.datacleaner.testware.DataCleanerTestCase;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;
import dk.eobjects.metamodel.schema.Table;

public class StandardMeasuresProfileTest extends DataCleanerTestCase {

	public void testProcessing() throws Exception {
		ProfileManagerTest.initProfileManager();

		StandardMeasuresProfile profile = new StandardMeasuresProfile();
		Table t = new Table("foobar");
		Column boolColumn = new Column("Col1", ColumnType.BOOLEAN, t, 0, true);
		Column stringColumn = new Column("Col2", ColumnType.VARCHAR, t, 1, true);
		Column[] columns = new Column[] { boolColumn, stringColumn };
		profile.initialize(columns);

		SelectItem[] selectItems = new SelectItem[] {
				new SelectItem(boolColumn), new SelectItem(stringColumn) };

		profile.process(new Row(selectItems,
				new Object[] { Boolean.TRUE, "foo" }), 3);
		profile.process(new Row(selectItems, new Object[] { null, "123" }), 2);
		profile.process(new Row(selectItems, new Object[] { Boolean.FALSE,
				"bar" }), 4);
		profile.process(new Row(selectItems,
				new Object[] { Boolean.FALSE, " " }), 2);
		profile.process(new Row(selectItems,
				new Object[] { Boolean.TRUE, "zzz" }), 1);

		IProfileResult result = profile.getResult();
		assertNull("Found exception: " + result.getError(), result.getError());
		IMatrix[] matrices = result.getMatrices();
		assertEquals(1, matrices.length);
		assertEquals(
				"Matrix[columnNames={Col1,Col2},Row count={12,12},Null values={MatrixValue[value=2,detailQuery=SELECT foobar.Col1, foobar.Col2 FROM foobar WHERE foobar.Col1 IS NULL],0},Empty values={0,MatrixValue[value=2,detailQuery=SELECT foobar.Col1, foobar.Col2 FROM foobar WHERE foobar.Col2 = '']},Highest value={true,zzz},Lowest value={false, }]",
				matrices[0].toString());
	}

	public void testNoRows() throws Exception {
		ProfileManagerTest.initProfileManager();

		StandardMeasuresProfile profile = new StandardMeasuresProfile();
		Column boolColumn = new Column("Col1", ColumnType.BOOLEAN);
		Column stringColumn = new Column("Col2", ColumnType.VARCHAR);
		Column[] columns = new Column[] { boolColumn, stringColumn };
		profile.initialize(columns);
		IProfileResult result = profile.getResult();
		assertEquals(0, result.getMatrices().length);
		Exception error = result.getError();
		assertEquals("No rows in selected data to profile!", error.getMessage());
	}

	public void testGetHighestValue() throws Exception {
		Object obj1 = null;
		Object obj2 = "foobar";
		Object highestObject = StandardMeasuresProfile.getHighestObject(obj1,
				obj2);
		assertSame(obj2, highestObject);

		obj1 = "barfoo";
		highestObject = StandardMeasuresProfile.getHighestObject(obj1, obj2);
		assertSame(obj2, highestObject);

		obj1 = null;
		obj2 = null;
		highestObject = StandardMeasuresProfile.getHighestObject(obj1, obj2);
		assertNull(highestObject);

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DATE, 1);
		obj1 = calendar.getTime();
		highestObject = StandardMeasuresProfile.getHighestObject(obj1, obj2);
		assertSame(obj1, highestObject);

		calendar.set(Calendar.DATE, 3);
		obj2 = calendar.getTime();
		highestObject = StandardMeasuresProfile.getHighestObject(obj1, obj2);
		assertSame(obj2, highestObject);
	}

	public void testDetailedResults() throws Exception {
		Connection connection = getTestDbConnection();
		DataContext dc = new DataContext(connection);
		Table customerTable = dc.getDefaultSchema().getTableByName("CUSTOMERS");
		assertNotNull(customerTable);

		Column[] columns = customerTable.getColumns();

		StandardMeasuresProfile profile = new StandardMeasuresProfile();
		profile.setProperties(new HashMap<String, String>());
		profile.initialize(columns);

		Query q = new Query();
		q.from(customerTable);
		q.select(columns);
		SelectItem countAllItem = SelectItem.getCountAllItem();
		q.select(countAllItem);
		q.groupBy(columns);

		DataSet data = dc.executeQuery(q);
		while (data.next()) {
			Row row = data.getRow();
			profile.process(row, (Integer) row.getValue(countAllItem));
		}

		IProfileResult result = profile.getResult();
		IMatrix[] matrices = result.getMatrices();
		assertEquals(1, matrices.length);

		assertEquals(
				"{Row count,Null values,Empty values,Highest value,Lowest value}",
				ArrayUtils.toString(matrices[0].getRowNames()));
		assertEquals(
				"{CUSTOMERNUMBER,CUSTOMERNAME,CONTACTLASTNAME,CONTACTFIRSTNAME,PHONE,ADDRESSLINE1,ADDRESSLINE2,CITY,STATE,POSTALCODE,COUNTRY,SALESREPEMPLOYEENUMBER,CREDITLIMIT}",
				ArrayUtils.toString(matrices[0].getColumnNames()));

		MatrixValue customerAdd2Null = matrices[0].getValue("Null values",
				"ADDRESSLINE2");
		assertTrue(customerAdd2Null.isDetailed());

		MatrixValue customerAdd2Empty = matrices[0].getValue("Empty values",
				"ADDRESSLINE2");
		assertEquals(0l, customerAdd2Empty.getValue());
		assertFalse(customerAdd2Empty.isDetailed());

		MatrixValue customerNumberNull = matrices[0].getValue("Null values",
				"CUSTOMERNUMBER");
		// All customers have a customer number, so no details
		assertEquals(0l, customerNumberNull.getValue());
		assertFalse(customerNumberNull.isDetailed());

		MatrixValue customerNumberEmpty = matrices[0].getValue("Empty values",
				"CUSTOMERNUMBER");
		// Only literal column types should have details for empty values
		assertFalse(customerNumberEmpty.isDetailed());

		DataSet detailData = customerAdd2Null.getDetails(dc);
		assertEquals(
				"{\"CUSTOMERS\".\"CUSTOMERNUMBER\",\"CUSTOMERS\".\"CUSTOMERNAME\",\"CUSTOMERS\".\"CONTACTLASTNAME\",\"CUSTOMERS\".\"CONTACTFIRSTNAME\",\"CUSTOMERS\".\"PHONE\",\"CUSTOMERS\".\"ADDRESSLINE1\",\"CUSTOMERS\".\"ADDRESSLINE2\",\"CUSTOMERS\".\"CITY\",\"CUSTOMERS\".\"STATE\",\"CUSTOMERS\".\"POSTALCODE\",\"CUSTOMERS\".\"COUNTRY\",\"CUSTOMERS\".\"SALESREPEMPLOYEENUMBER\",\"CUSTOMERS\".\"CREDITLIMIT\"}",
				ArrayUtils.toString(detailData.getSelectItems()));
		assertTrue(detailData.next());
		assertEquals(
				"Row[values={103,Atelier graphique,Schmitt,Carine,40.32.2555,54, rue Royale,<null>,Nantes,<null>,44000,France,1370,21000.0}]",
				ArrayUtils.toString(detailData.getRow().toString()));
		assertEquals(108, detailData.toTableModel().getRowCount());
	}
}