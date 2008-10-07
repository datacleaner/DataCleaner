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

import java.util.HashMap;
import java.util.Map;

import org.joda.time.IllegalFieldValueException;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.IProfileResult;
import dk.eobjects.datacleaner.testware.DataCleanerTestCase;
import dk.eobjects.metamodel.CsvDataContextStrategy;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public class DateMaskProfileTest extends DataCleanerTestCase {

	/**
	 * A test to get to know the date formatter api and verify it's behaviour
	 */
	public void testFormatter() throws Exception {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
		formatter.parseDateTime("2008-02-28");
		try {
			formatter.parseDateTime("2008-02-30");
			fail("Exception should have been thrown");
		} catch (IllegalFieldValueException e) {
			assertEquals(
					"Cannot parse \"2008-02-30\": Value 30 for dayOfMonth must be in the range [1,29]",
					e.getMessage());
		}
	}

	public void testSingleColumn() throws Exception {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(DateMaskProfile.PREFIX_PROPERTY_REGEX + 0, "yyyy-MM-dd");
		properties.put(DateMaskProfile.PREFIX_PROPERTY_REGEX + 1, "yyyy-dd-MM");
		properties.put(DateMaskProfile.PREFIX_PROPERTY_REGEX + 2, "MM/dd/yyyy");
		Column column = new Column("col1");
		SelectItem[] selectItems = { new SelectItem(column) };

		DateMaskProfile profile = new DateMaskProfile();
		profile.setProperties(properties);
		profile.initialize(column);
		profile.setDetailsEnabled(false);

		profile.process(new Row(selectItems, new Object[] { "2008-12-31" }), 2);

		assertEquals(
				"Matrix[columnNames={col1},yyyy-MM-dd={2},yyyy-dd-MM={0},MM/dd/yyyy={0},"
						+ "No Matches={0},Multiple Matches={0}]", profile
						.getResult().getMatrices()[0].toString());

		profile.process(new Row(selectItems, new Object[] { "2008-01-01" }), 1);

		// will not be matched (doesn't live up to the mask)
		profile.process(new Row(selectItems, new Object[] { "foobar" }), 4);

		assertEquals(
				"Matrix[columnNames={col1},yyyy-MM-dd={3},yyyy-dd-MM={1},MM/dd/yyyy={0},"
						+ "No Matches={4},Multiple Matches={1}]", profile
						.getResult().getMatrices()[0].toString());

		profile.process(new Row(selectItems, new Object[] { "02/28/2008" }), 8);

		// will not be matched (there is no 31st of february)
		profile
				.process(new Row(selectItems, new Object[] { "02/31/2008" }),
						16);

		assertEquals(
				"Matrix[columnNames={col1},yyyy-MM-dd={3},yyyy-dd-MM={1},MM/dd/yyyy={8},"
						+ "No Matches={20},Multiple Matches={1}]", profile
						.getResult().getMatrices()[0].toString());
	}

	public void testSeveralColumns() throws Exception {
		DataContext dc = new DataContext(new CsvDataContextStrategy(
				getTestResourceAsFile("datemask_fields.csv")));
		Table table = dc.getDefaultSchema().getTables()[0];
		Column[] columns = table.getColumns();

		Map<String, String> properties = new HashMap<String, String>();
		properties.put(DateMaskProfile.PREFIX_PROPERTY_REGEX + 0, "yyyy-MM-dd");
		properties.put(DateMaskProfile.PREFIX_PROPERTY_REGEX + 1, "dd-MM-yyyy");

		DateMaskProfile profile = new DateMaskProfile();
		profile.setProperties(properties);
		profile.initialize(columns);

		DataSet data = dc.executeQuery(new Query().select(columns).from(table));
		while (data.next()) {
			profile.process(data.getRow(), 1);
		}
		data.close();

		IProfileResult result = profile.getResult();
		assertEquals(1, result.getMatrices().length);
		IMatrix matrix = result.getMatrices()[0];

		assertEquals(
				"Matrix[columnNames={date1,date2,desc},"
						+ "yyyy-MM-dd={0,"
						+ "MatrixValue[value=3,detailQuery=SELECT datemask_fields.date1, datemask_fields.date2, datemask_fields.desc FROM datemask_fields.csv.datemask_fields]"
						+ ",0},"
						+ "dd-MM-yyyy={"
						+ "MatrixValue[value=3,detailQuery=SELECT datemask_fields.date1, datemask_fields.date2, datemask_fields.desc FROM datemask_fields.csv.datemask_fields]"
						+ ",0,0},"
						+ "No Matches={MatrixValue[value=1,detailQuery=SELECT datemask_fields.date1, datemask_fields.date2, datemask_fields.desc FROM datemask_fields.csv.datemask_fields],"
						+ "MatrixValue[value=1,detailQuery=SELECT datemask_fields.date1, datemask_fields.date2, datemask_fields.desc FROM datemask_fields.csv.datemask_fields],"
						+ "MatrixValue[value=4,detailQuery=SELECT datemask_fields.date1, datemask_fields.date2, datemask_fields.desc FROM datemask_fields.csv.datemask_fields]},"
						+ "Multiple Matches={0,0,0}]", matrix.toString());

		assertEquals(3, matrix.getValue("dd-MM-yyyy", "date1").getDetails(dc)
				.toObjectArrays().size());

		assertEquals(4, matrix.getValue("No Matches", "desc").getDetails(dc)
				.toObjectArrays().size());
	}
}