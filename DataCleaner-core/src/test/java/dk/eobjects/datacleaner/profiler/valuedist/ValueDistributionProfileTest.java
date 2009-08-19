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
package dk.eobjects.datacleaner.profiler.valuedist;

import java.util.HashMap;

import org.apache.commons.lang.ArrayUtils;

import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.IProfile;
import dk.eobjects.datacleaner.profiler.MatrixValue;
import dk.eobjects.datacleaner.profiler.ProfileManagerTest;
import dk.eobjects.datacleaner.testware.DataCleanerTestCase;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;
import dk.eobjects.metamodel.schema.Table;

public class ValueDistributionProfileTest extends DataCleanerTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ProfileManagerTest.initProfileManager();
	}

	public void testNullValuesFullResult() throws Exception {
		Table t = new Table("foobar");
		Column[] columns = new Column[] { new Column("foo").setTable(t) };
		t.addColumn(columns[0]);

		SelectItem[] selectItems = new SelectItem[] { new SelectItem(columns[0]) };

		ValueDistributionProfile profile = new ValueDistributionProfile();
		HashMap<String, String> properties = new HashMap<String, String>();
		profile.setDetailsEnabled(true);
		profile.setProperties(properties);
		profile.initialize(columns);

		profile.process(new Row(selectItems, new Object[] { null }), 1);

		assertEquals(
				"Matrix[columnNames={foo frequency,Percentage of total},<Unique values>={MatrixValue[value=1,detailSelectItems={<Unique values>},detailRows=1],100%}]",
				profile.getResult().getMatrices()[0].toString());
		DataSet details = profile.getResult().getMatrices()[0].getValue(0, 0)
				.getDetails(null);
		assertNotNull(details);
		assertTrue(details.next());
		assertNull(details.getRow().getValue(0));
		assertFalse(details.next());

		profile.process(new Row(selectItems, new Object[] { "foo" }), 1);

		assertEquals(
				"Matrix[columnNames={foo frequency,Percentage of total},<Unique values>={MatrixValue[value=2,detailSelectItems={<Unique values>},detailRows=2],100%}]",
				profile.getResult().getMatrices()[0].toString());

		profile.process(new Row(selectItems, new Object[] { null }), 1);

		assertEquals(
				"Matrix[columnNames={foo frequency,Percentage of total},<null>={MatrixValue[value=2,detailQuery=SELECT foobar.foo FROM foobar WHERE foobar.foo IS NULL],66%},<Unique values>={MatrixValue[value=1,detailSelectItems={<Unique values>},detailRows=1],33%}]",
				profile.getResult().getMatrices()[0].toString());

		profile.close();
	}

	public void testNullValuesTopBottomResult() throws Exception {
		Table t = new Table("foobar");
		Column[] columns = new Column[] { new Column("foo").setTable(t) };
		t.addColumn(columns[0]);

		SelectItem[] selectItems = new SelectItem[] { new SelectItem(columns[0]) };

		ValueDistributionProfile profile = new ValueDistributionProfile();
		HashMap<String, String> properties = new HashMap<String, String>();
		properties.put(ValueDistributionProfile.PROPERTY_TOP_N, "4");
		properties.put(ValueDistributionProfile.PROPERTY_BOTTOM_N, "3");
		profile.setDetailsEnabled(false);
		profile.setProperties(properties);
		profile.initialize(columns);

		profile.process(new Row(selectItems, new Object[] { null }), 3);
		profile.process(new Row(selectItems, new Object[] { "foo" }), 2);
		profile.process(new Row(selectItems, new Object[] { "bar" }), 1);

		assertEquals(
				"{Matrix[columnNames={foo},top 1={null (3)},top 2={foo (2)},top 3={<null>},top 4={<null>},"
						+ "bottom 3={<null>},bottom 2={<null>},bottom 1={<Unique values> (1)}]}",
				ArrayUtils.toString(profile.getResult().getMatrices()));

		profile.close();
	}

	public void testOnlyUnique() throws Exception {
		Table t = new Table("foobar");
		Column[] columns = new Column[] { new Column("foo").setTable(t),
				new Column("bar").setTable(t) };
		t.addColumn(columns[0]).addColumn(columns[1]);

		SelectItem[] selectItems = new SelectItem[] {
				new SelectItem(columns[0]), new SelectItem(columns[1]) };

		IProfile profile = new ValueDistributionProfile();
		HashMap<String, String> properties = new HashMap<String, String>();
		properties.put(ValueDistributionProfile.PROPERTY_TOP_N, "4");
		properties.put(ValueDistributionProfile.PROPERTY_BOTTOM_N, "3");
		profile.setProperties(properties);
		profile.initialize(columns);

		profile.process(new Row(selectItems, new Object[] { "foo", 26 }), 1);
		profile.process(new Row(selectItems, new Object[] { "bar", 22 }), 1);
		profile.process(new Row(selectItems, new Object[] { "foobar", 12 }), 1);

		assertEquals(
				"{Matrix[columnNames={foo,bar},top 1={<null>,<null>},top 2={<null>,<null>},top 3={<null>,<null>},top 4={<null>,<null>},bottom 3={<null>,<null>},bottom 2={<null>,<null>},bottom 1={MatrixValue[value=<Unique values> (3),detailQuery=SELECT foobar.foo, COUNT(*) FROM foobar GROUP BY foobar.foo HAVING COUNT(*) = 1],MatrixValue[value=<Unique values> (3),detailQuery=SELECT foobar.bar, COUNT(*) FROM foobar GROUP BY foobar.bar HAVING COUNT(*) = 1]}]}",
				ArrayUtils.toString(profile.getResult().getMatrices()));

		profile.close();
	}

	public void testProfilingWithoutTopBottom() throws Exception {
		Table t = new Table("foobar");
		Column[] columns = new Column[] { new Column("foo").setTable(t),
				new Column("bar").setTable(t) };
		t.addColumn(columns[0]).addColumn(columns[1]);

		SelectItem[] selectItems = new SelectItem[] {
				new SelectItem(columns[0]), new SelectItem(columns[1]) };

		IProfile profile = new ValueDistributionProfile();
		HashMap<String, String> properties = new HashMap<String, String>();
		profile.setProperties(properties);
		profile.initialize(columns);

		assertEquals("{}", ArrayUtils.toString(profile.getResult()
				.getMatrices()));

		profile.process(new Row(selectItems, new Object[] { "kasper", 26 }), 1);

		assertEquals(
				"{Matrix[columnNames={foo frequency,Percentage of total},"
						+ "<Unique values>={MatrixValue[value=1,detailSelectItems={<Unique values>},detailRows=1],100%}],"
						+ "Matrix[columnNames={bar frequency,Percentage of total},"
						+ "<Unique values>={MatrixValue[value=1,detailSelectItems={<Unique values>},detailRows=1],100%}]}",
				ArrayUtils.toString(profile.getResult().getMatrices()));

		profile.process(new Row(selectItems, new Object[] { "kasper", 25 }), 1);

		assertEquals(
				"{Matrix[columnNames={foo frequency,Percentage of total},kasper={MatrixValue[value=2,detailQuery=SELECT foobar.foo, foobar.bar FROM foobar WHERE foobar.foo = 'kasper'],100%}],Matrix[columnNames={bar frequency,Percentage of total},<Unique values>={MatrixValue[value=2,detailSelectItems={<Unique values>},detailRows=2],100%}]}",
				ArrayUtils.toString(profile.getResult().getMatrices()));

		profile.process(new Row(selectItems, new Object[] { "kasper", 25 }), 1);

		assertEquals(
				"{Matrix[columnNames={foo frequency,Percentage of total},kasper={MatrixValue[value=3,detailQuery=SELECT foobar.foo, foobar.bar FROM foobar WHERE foobar.foo = 'kasper'],100%}],Matrix[columnNames={bar frequency,Percentage of total},25={MatrixValue[value=2,detailQuery=SELECT foobar.foo, foobar.bar FROM foobar WHERE foobar.bar = '25'],66%},<Unique values>={MatrixValue[value=1,detailSelectItems={<Unique values>},detailRows=1],33%}]}",
				ArrayUtils.toString(profile.getResult().getMatrices()));

		profile.close();
	}

	public void testTopBottomProperty() throws Exception {
		Table t = new Table("foobar");
		Column[] columns = new Column[] { new Column("col1").setTable(t),
				new Column("col2").setTable(t) };
		t.addColumn(columns[0]).addColumn(columns[1]);
		SelectItem[] selectItems = new SelectItem[] {
				new SelectItem(columns[0]), new SelectItem(columns[1]) };

		IProfile profile = new ValueDistributionProfile();
		HashMap<String, String> properties = new HashMap<String, String>();
		properties.put(ValueDistributionProfile.PROPERTY_TOP_N, "4");
		properties.put(ValueDistributionProfile.PROPERTY_BOTTOM_N, "3");
		profile.setProperties(properties);
		profile.initialize(columns);

		profile
				.process(new Row(selectItems, new Object[] { "foo", "bar" }),
						40);

		IMatrix[] matrices = profile.getResult().getMatrices();
		assertEquals(1, matrices.length);
		assertEquals(
				"Matrix[columnNames={col1,col2},top 1={MatrixValue[value=foo (40),detailQuery=SELECT foobar.col1, foobar.col2 FROM foobar WHERE foobar.col1 = 'foo'],MatrixValue[value=bar (40),detailQuery=SELECT foobar.col1, foobar.col2 FROM foobar WHERE foobar.col2 = 'bar']},top 2={<null>,<null>},top 3={<null>,<null>},top 4={<null>,<null>},bottom 3={<null>,<null>},bottom 2={<null>,<null>},bottom 1={<null>,<null>}]",
				matrices[0].toString());

		profile.process(new Row(selectItems, new Object[] { "n", "s" }), 2);
		matrices = profile.getResult().getMatrices();
		assertEquals(1, matrices.length);
		assertEquals(
				"Matrix[columnNames={col1,col2},top 1={MatrixValue[value=foo (40),detailQuery=SELECT foobar.col1, foobar.col2 FROM foobar WHERE foobar.col1 = 'foo'],MatrixValue[value=bar (40),detailQuery=SELECT foobar.col1, foobar.col2 FROM foobar WHERE foobar.col2 = 'bar']},top 2={MatrixValue[value=n (2),detailQuery=SELECT foobar.col1, foobar.col2 FROM foobar WHERE foobar.col1 = 'n'],MatrixValue[value=s (2),detailQuery=SELECT foobar.col1, foobar.col2 FROM foobar WHERE foobar.col2 = 's']},top 3={<null>,<null>},top 4={<null>,<null>},bottom 3={<null>,<null>},bottom 2={<null>,<null>},bottom 1={<null>,<null>}]",
				matrices[0].toString());

		profile.process(new Row(selectItems, new Object[] { "m", "t" }), 1);
		matrices = profile.getResult().getMatrices();
		assertEquals(1, matrices.length);
		assertEquals(
				"Matrix[columnNames={col1,col2},top 1={MatrixValue[value=foo (40),detailQuery=SELECT foobar.col1, foobar.col2 FROM foobar WHERE foobar.col1 = 'foo'],MatrixValue[value=bar (40),detailQuery=SELECT foobar.col1, foobar.col2 FROM foobar WHERE foobar.col2 = 'bar']},top 2={MatrixValue[value=n (2),detailQuery=SELECT foobar.col1, foobar.col2 FROM foobar WHERE foobar.col1 = 'n'],MatrixValue[value=s (2),detailQuery=SELECT foobar.col1, foobar.col2 FROM foobar WHERE foobar.col2 = 's']},top 3={<null>,<null>},top 4={<null>,<null>},bottom 3={<null>,<null>},bottom 2={<null>,<null>},bottom 1={MatrixValue[value=<Unique values> (1),detailQuery=SELECT foobar.col1, COUNT(*) FROM foobar GROUP BY foobar.col1 HAVING COUNT(*) = 1],MatrixValue[value=<Unique values> (1),detailQuery=SELECT foobar.col2, COUNT(*) FROM foobar GROUP BY foobar.col2 HAVING COUNT(*) = 1]}]",
				matrices[0].toString());

		profile
				.process(new Row(selectItems, new Object[] { "foobar", "" }),
						10);
		profile.process(new Row(selectItems, new Object[] { "foobar", "bar" }),
				9);
		profile.process(new Row(selectItems, new Object[] { "abc", "123" }), 8);
		profile.process(new Row(selectItems, new Object[] { "!", "-" }), 7);
		profile.process(new Row(selectItems, new Object[] { "def", "456" }), 6);
		profile.process(new Row(selectItems, new Object[] { "ghi", "789" }), 5);
		profile.process(new Row(selectItems, new Object[] { "jkl", "000" }), 4);
		profile.process(new Row(selectItems, new Object[] { "m", "r" }), 3);
		profile.process(new Row(selectItems, new Object[] { null, null }), 2);
		profile.process(new Row(selectItems, new Object[] { "o", "u" }), 1);
		profile.process(new Row(selectItems, new Object[] { "o", "v" }), 1);

		Exception error = profile.getResult().getError();
		if (error != null) {
			error.printStackTrace();
			fail("Error while executing profile: " + error.getMessage());
		}

		matrices = profile.getResult().getMatrices();
		assertEquals(1, matrices.length);
		assertEquals(
				"Matrix[columnNames={col1,col2},top 1={MatrixValue[value=foo (40),detailQuery=SELECT foobar.col1, foobar.col2 FROM foobar WHERE foobar.col1 = 'foo'],MatrixValue[value=bar (49),detailQuery=SELECT foobar.col1, foobar.col2 FROM foobar WHERE foobar.col2 = 'bar']},top 2={MatrixValue[value=foobar (19),detailQuery=SELECT foobar.col1, foobar.col2 FROM foobar WHERE foobar.col1 = 'foobar'],MatrixValue[value= (10),detailQuery=SELECT foobar.col1, foobar.col2 FROM foobar WHERE foobar.col2 = '']},top 3={MatrixValue[value=abc (8),detailQuery=SELECT foobar.col1, foobar.col2 FROM foobar WHERE foobar.col1 = 'abc'],MatrixValue[value=123 (8),detailQuery=SELECT foobar.col1, foobar.col2 FROM foobar WHERE foobar.col2 = '123']},top 4={MatrixValue[value=! (7),detailQuery=SELECT foobar.col1, foobar.col2 FROM foobar WHERE foobar.col1 = '!'],MatrixValue[value=- (7),detailQuery=SELECT foobar.col1, foobar.col2 FROM foobar WHERE foobar.col2 = '-']},bottom 3={MatrixValue[value=<null> (2),detailQuery=SELECT foobar.col1, foobar.col2 FROM foobar WHERE foobar.col1 IS NULL],MatrixValue[value=<null> (2),detailQuery=SELECT foobar.col1, foobar.col2 FROM foobar WHERE foobar.col2 IS NULL]},bottom 2={MatrixValue[value=n (2),detailQuery=SELECT foobar.col1, foobar.col2 FROM foobar WHERE foobar.col1 = 'n'],MatrixValue[value=s (2),detailQuery=SELECT foobar.col1, foobar.col2 FROM foobar WHERE foobar.col2 = 's']},bottom 1={MatrixValue[value=o (2),detailQuery=SELECT foobar.col1, foobar.col2 FROM foobar WHERE foobar.col1 = 'o'],MatrixValue[value=<Unique values> (3),detailQuery=SELECT foobar.col2, COUNT(*) FROM foobar GROUP BY foobar.col2 HAVING COUNT(*) = 1]}]",
				matrices[0].toString());

		profile.close();
	}

	public void testSingleQuoteInValue() throws Exception {
		Table t = new Table("blogs").setQuote("\"");
		Column[] columns = new Column[] { new Column("blog name",
				ColumnType.VARCHAR, t, 0, true).setQuote("\"") };
		t.addColumn(columns[0]);
		SelectItem[] selectItems = new SelectItem[] { new SelectItem(columns[0]) };

		IProfile profile = new ValueDistributionProfile();
		HashMap<String, String> properties = new HashMap<String, String>();
		profile.setProperties(properties);
		profile.initialize(columns);

		profile.process(new Row(selectItems,
				new Object[] { "my travelling blog" }), 1);
		profile.process(
				new Row(selectItems, new Object[] { "kasper's source" }), 4);

		IMatrix matrix = profile.getResult().getMatrices()[0];
		MatrixValue matrixValue = matrix.getValue("kasper's source",
				"blog name frequency");
		assertEquals(
				"MatrixValue[value=4,detailQuery=SELECT \"blogs\".\"blog name\" FROM \"blogs\" WHERE \"blogs\".\"blog name\" LIKE 'kasper%s source']",
				matrixValue.toString());

		properties.put(ValueDistributionProfile.PROPERTY_TOP_N, "2");
		properties.put(ValueDistributionProfile.PROPERTY_BOTTOM_N, "2");
		profile.setProperties(properties);

		matrix = profile.getResult().getMatrices()[0];
		assertEquals(
				"Matrix[columnNames={blog name},top 1={MatrixValue[value=kasper's source (4),detailQuery=SELECT \"blogs\".\"blog name\" FROM \"blogs\" WHERE \"blogs\".\"blog name\" LIKE 'kasper%s source']},top 2={<null>},bottom 2={<null>},bottom 1={MatrixValue[value=<Unique values> (1),detailQuery=SELECT \"blogs\".\"blog name\", COUNT(*) FROM \"blogs\" GROUP BY \"blogs\".\"blog name\" HAVING COUNT(*) = 1]}]",
				matrix.toString());

		profile.close();
	}
}