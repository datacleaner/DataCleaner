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

import junit.framework.TestCase;
import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.IProfile;
import dk.eobjects.datacleaner.profiler.IProfileResult;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;
import dk.eobjects.metamodel.schema.Table;

public class NumberAnalysisProfileTest extends TestCase {

	public void testCount() throws Exception {
		Table table = new Table("foobar");
		Column column = new Column("My column", ColumnType.INTEGER, table, 0,
				true);
		Column[] columns = { column };
		IProfile profile = new NumberAnalysisProfile();

		profile.initialize(columns);
		SelectItem[] selectItems = new SelectItem[] { new SelectItem(column) };

		profile.process(new Row(selectItems, new Object[] { 4 }), 2);
		profile.process(new Row(selectItems, new Object[] { null }), 5);
		profile.process(new Row(selectItems, new Object[] { 8 }), 2);

		assertEquals(
				"Matrix[columnNames={My column},Highest value={MatrixValue[value=8,detailQuery= FROM foobar WHERE foobar.My column = 8]},Lowest value={MatrixValue[value=4,detailQuery= FROM foobar WHERE foobar.My column = 4]},Sum={24},Mean={6},Geometric mean={5,66},Standard deviation={2,31},Variance={5,33}]",
				profile.getResult().getMatrices()[0].toString());
	}

	public void testProcessing() throws Exception {
		Table table = new Table("foobar");
		Column bigintColumn = new Column("My bigint column", ColumnType.BIGINT,
				table, 0, true);
		Column floatColumn = new Column("My float column", ColumnType.FLOAT,
				table, 1, true);
		Column[] columns = { bigintColumn, floatColumn };
		SelectItem[] selectItems = new SelectItem[] {
				new SelectItem(bigintColumn), new SelectItem(floatColumn) };
		IProfile profile = new NumberAnalysisProfile();

		profile.initialize(columns);

		profile.process(new Row(selectItems, new Object[] { 10, 0.1f }), 1);
		profile.process(new Row(selectItems, new Object[] { 20, 0.2f }), 1);
		profile.process(new Row(selectItems, new Object[] { 30, 0.3f }), 1);
		profile.process(new Row(selectItems, new Object[] { 40, 0.4f }), 1);
		profile.process(new Row(selectItems, new Object[] { 50, 0.5f }), 1);
		profile.process(new Row(selectItems, new Object[] { 60, 0.6f }), 1);
		profile.process(new Row(selectItems, new Object[] { 70, 0.7f }), 1);
		profile.process(new Row(selectItems, new Object[] { 80, 0.8f }), 1);

		IProfileResult result = profile.getResult();
		IMatrix[] matrices = result.getMatrices();
		assertEquals(1, matrices.length);
		assertEquals(
				"Matrix[columnNames={My bigint column,My float column},Highest value={MatrixValue[value=80,detailQuery= FROM foobar WHERE foobar.My bigint column = 80],MatrixValue[value=0,8,detailQuery= FROM foobar WHERE foobar.My float column = 0.8]},Lowest value={MatrixValue[value=10,detailQuery= FROM foobar WHERE foobar.My bigint column = 10],MatrixValue[value=0,1,detailQuery= FROM foobar WHERE foobar.My float column = 0.1]},Sum={360,3,6},Mean={45,0,45},Geometric mean={37,64,0,38},Standard deviation={24,49,0,24},Variance={600,0,06}]",
				matrices[0].toString());
	}
}