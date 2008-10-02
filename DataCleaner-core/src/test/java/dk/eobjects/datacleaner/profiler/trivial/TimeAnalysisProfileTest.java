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
import java.sql.Date;
import java.sql.Time;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.joda.time.LocalTime;
import org.joda.time.MutableDateTime;

import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.IProfile;
import dk.eobjects.datacleaner.profiler.IProfileResult;
import dk.eobjects.datacleaner.profiler.MatrixValue;
import dk.eobjects.datacleaner.testware.DataCleanerTestCase;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.IRowFilter;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;
import dk.eobjects.metamodel.schema.Table;

public class TimeAnalysisProfileTest extends DataCleanerTestCase {

	public void testDrillToDetails() throws Exception {
		Connection connection = getTestDbConnection();
		DataContext dc = new DataContext(connection);
		Table paymentsTable = dc.getDefaultSchema().getTableByName("PAYMENTS");
		Column column = paymentsTable.getColumnByName("PAYMENTDATE");
		Query q = new Query().select(column).selectCount().from(paymentsTable)
				.groupBy(column);
		DataSet dataSet = dc.executeQuery(q);

		IProfile profile = new TimeAnalysisProfile();
		profile.initialize(column);
		profile.setProperties(new HashMap<String, String>());

		while (dataSet.next()) {
			Row row = dataSet.getRow();
			profile.process(row, ((Number) row.getValue(SelectItem
					.getCountAllItem())).longValue());
		}
		dataSet.close();

		IProfileResult result = profile.getResult();
		if (result.getError() != null) {
			throw result.getError();
		}
		IMatrix matrix = result.getMatrices()[0];
		MatrixValue value = matrix.getValue("Where [Year=2003]", "PAYMENTDATE");
		assertNotNull(value);

		dataSet = value.getDetails(dc);
		assertEquals("{_CUSTOMERNUMBER_,_CHECKNUMBER_,_PAYMENTDATE_,_AMOUNT_}",
				ArrayUtils.toString(dataSet.getSelectItems())
						.replace('\"', '_'));
		List<Object[]> objectArrays = dataSet.toObjectArrays();
		assertEquals(102, objectArrays.size());
		assertEquals("{103,JM555205,2003-06-05 00:00:00.0,16560}", ArrayUtils
				.toString(objectArrays.get(0)));

		connection.close();
	}

	public void testProcessingDateTime() throws Exception {
		Table t = new Table("foo");
		Column dateColumn = new Column("My date and time column",
				ColumnType.DATE, t, 0, true);
		t.addColumn(dateColumn);
		Column[] columns = { dateColumn };
		IProfile profile = new TimeAnalysisProfile();
		SelectItem[] selectItems = new SelectItem[] { new SelectItem(dateColumn) };

		profile.initialize(columns);

		MutableDateTime mdt = new MutableDateTime(2000, 1, 1, 0, 0, 0, 0);
		profile.process(new Row(selectItems, new Object[] { mdt.toDate() }), 1);

		mdt.addHours(10);
		mdt.addDays(2);
		profile.process(new Row(selectItems, new Object[] { mdt.toDate() }), 1);

		IProfileResult result = profile.getResult();
		IMatrix[] matrices = result.getMatrices();
		assertEquals(1, matrices.length);
		IMatrix matrix = matrices[0];
		assertEquals(
				"Matrix[columnNames={My date and time column},Highest value={2000-01-03 10:00:00},Lowest value={2000-01-01 00:00:00},Where [Year=2000]={MatrixValue[value=2,detailQuery=SELECT foo.My date and time column FROM foo WHERE foo.My date and time column > DATE '1999-12-31' AND foo.My date and time column < DATE '2001-01-01']}]",
				matrix.toString());
		IRowFilter[] rowFilters = matrix.getValue("Where [Year=2000]",
				"My date and time column").getDetailRowFilters();
		assertEquals(1, rowFilters.length);
	}

	public void testProcessingDateAndTimeSeperate() throws Exception {
		Table t = new Table("foo");
		Column dateColumn = new Column("My date column", ColumnType.DATE, t, 0,
				true);
		Column timeColumn = new Column("My time column", ColumnType.TIME, t, 1,
				true);
		t.addColumn(dateColumn).addColumn(timeColumn);
		Column[] columns = { dateColumn, timeColumn };
		IProfile profile = new TimeAnalysisProfile();

		SelectItem[] selectItems = new SelectItem[] {
				new SelectItem(dateColumn), new SelectItem(timeColumn) };

		profile.initialize(columns);

		MutableDateTime mdt1 = new MutableDateTime(2000, 1, 1, 0, 2, 3, 4);
		Date date = new Date(mdt1.getMillis());
		MutableDateTime mdt2 = new MutableDateTime(2000, 1, 1, 0, 0, 0, 0);
		Time time = new Time(mdt2.getMillis());
		profile.process(new Row(selectItems, new Object[] { date, time }), 1);

		mdt1.addDays(200);
		mdt1.addYears(2);
		date = new Date(mdt1.getMillis());
		mdt2.addSeconds(200);
		time = new Time(mdt2.getMillis());
		profile.process(new Row(selectItems, new Object[] { date, time }), 1);

		mdt1.addHours(-451);
		date = new Date(mdt1.getMillis());
		mdt2.addSeconds(200);
		time = new Time(mdt2.getMillis());
		profile.process(new Row(selectItems, new Object[] { date, time }), 1);

		IProfileResult result = profile.getResult();
		IMatrix[] matrices = result.getMatrices();
		assertEquals(1, matrices.length);
		assertEquals(
				"Matrix[columnNames={My date column,My time column},Highest value={2002-07-19 00:02:03,00:06:40},Lowest value={2000-01-01 00:02:03,00:00:00},"
						+ "Where [Year=2000]={MatrixValue[value=1,detailQuery=SELECT foo.My date column, foo.My time column FROM foo WHERE foo.My date column > DATE '1999-12-31' AND foo.My date column < DATE '2001-01-01'],MatrixValue[value=3,detailQuery=SELECT foo.My date column, foo.My time column FROM foo]},"
						+ "Where [Year=2002]={MatrixValue[value=2,detailQuery=SELECT foo.My date column, foo.My time column FROM foo WHERE foo.My date column > DATE '2001-12-31' AND foo.My date column < DATE '2003-01-01'],0}]",
				matrices[0].toString());
	}

	public void testJodaTimeMidnigthConstant() throws Exception {
		MutableDateTime mdt = new MutableDateTime(2000, 1, 1, 0, 0, 0, 0);
		assertEquals(mdt.toDateTime().toLocalTime(), LocalTime.MIDNIGHT);
	}
}