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

import junit.framework.TestCase;
import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.IProfileResult;
import dk.eobjects.datacleaner.profiler.MatrixValue;
import dk.eobjects.datacleaner.profiler.ProfileManagerTest;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;
import dk.eobjects.metamodel.schema.Table;

public class StringAnalysisProfileTest extends TestCase {

	public void testProcessing() throws Exception {
		ProfileManagerTest.initProfileManager();

		StringAnalysisProfile profile = new StringAnalysisProfile();
		Table t = new Table("myTable");
		Column charColumn = new Column("charColumn", ColumnType.CHAR, t, 0, true);
		Column stringColumn = new Column("stringColumn", ColumnType.VARCHAR, t, 1, true);
		Column[] columns = new Column[] { charColumn, stringColumn };
		profile.initialize(columns);

		SelectItem[] selectItems = new SelectItem[] { new SelectItem(charColumn), new SelectItem(stringColumn) };

		profile.process(new Row(selectItems, new Object[] { 'a', "foo" }), 1);
		profile.process(new Row(selectItems, new Object[] { 'A', "foobar" }), 1);
		profile.process(new Row(selectItems, new Object[] { 'A', "bar" }), 1);
		profile.process(new Row(selectItems, new Object[] { 'B', "FOO" }), 1);
		profile.process(new Row(selectItems, new Object[] { 'b', "Foo Bar" }), 1);
		profile.process(new Row(selectItems, new Object[] { '1', "Fo oo Ba ar" }), 1);
		profile.process(new Row(selectItems, new Object[] { '?', "" }), 1);
		profile.process(new Row(selectItems, new Object[] { null, null }), 1);

		IProfileResult result = profile.getResult();
		assertNull(result.getError());
		IMatrix[] matrices = result.getMatrices();
		assertEquals(1, matrices.length);
		assertEquals(
				"Matrix[columnNames={charColumn,stringColumn},"
						+ "Char count={7,33},"
						+ "Max chars={MatrixValue[value=1,detailQuery=SELECT myTable.charColumn, COUNT(*) FROM myTable GROUP BY myTable.charColumn],MatrixValue[value=11,detailQuery=SELECT myTable.stringColumn, COUNT(*) FROM myTable GROUP BY myTable.stringColumn]},"
						+ "Min chars={MatrixValue[value=1,detailQuery=SELECT myTable.charColumn, COUNT(*) FROM myTable GROUP BY myTable.charColumn],MatrixValue[value=0,detailQuery=SELECT myTable.stringColumn, COUNT(*) FROM myTable GROUP BY myTable.stringColumn]},"
						+ "Avg chars={1,4.714},Uppercase chars={42%,21%},"
						+ "Lowercase chars={28%,66%},"
						+ "Non-letter chars={28%,12%},"
						+ "Word count={7,10},"
						+ "Max words={MatrixValue[value=1,detailQuery=SELECT myTable.charColumn, COUNT(*) FROM myTable GROUP BY myTable.charColumn],MatrixValue[value=4,detailQuery=SELECT myTable.stringColumn, COUNT(*) FROM myTable GROUP BY myTable.stringColumn]},"
						+ "Min words={MatrixValue[value=1,detailQuery=SELECT myTable.charColumn, COUNT(*) FROM myTable GROUP BY myTable.charColumn],MatrixValue[value=0,detailQuery=SELECT myTable.stringColumn, COUNT(*) FROM myTable GROUP BY myTable.stringColumn]}]",
				matrices[0].toString());

		MatrixValue value = result.getMatrices()[0].getValue("Max chars", "stringColumn");
		assertTrue(value.isDetailed());
		assertEquals(1, value.getDetailRowFilters().length);
	}

	public void testOnlyNull() throws Exception {
		ProfileManagerTest.initProfileManager();

		StringAnalysisProfile profile = new StringAnalysisProfile();
		Table t = new Table("myTable");
		Column charColumn = new Column("charColumn", ColumnType.CHAR, t, 0, true);
		Column stringColumn = new Column("stringColumn", ColumnType.VARCHAR, t, 1, true);
		Column[] columns = new Column[] { charColumn, stringColumn };
		profile.initialize(columns);
		profile.setProperties(new HashMap<String, String>());
		SelectItem[] selectItems = new SelectItem[] { new SelectItem(charColumn), new SelectItem(stringColumn) };
		profile.process(new Row(selectItems, new Object[] { null, null }), 1);
		IProfileResult result = profile.getResult();
		assertNull("found error: " + result.getError(), result.getError());
		IMatrix[] matrices = result.getMatrices();
		assertEquals(1, matrices.length);
		assertEquals(
				"Matrix[columnNames={charColumn,stringColumn}," +
				"Char count={0,0},Max chars={<null>,<null>}," +
				"Min chars={<null>,<null>},Avg chars={<null>,<null>}," +
				"Uppercase chars={0%,0%}," +
				"Lowercase chars={0%,0%}," +
				"Non-letter chars={0%,0%}," +
				"Word count={0,0}," +
				"Max words={<null>,<null>}," +
				"Min words={<null>,<null>}]",
				matrices[0].toString());
	}
}