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
import dk.eobjects.datacleaner.profiler.IProfileResult;
import dk.eobjects.datacleaner.profiler.ProfileManagerTest;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;

public class RepeatedValuesProfileTest extends TestCase {

	@SuppressWarnings("deprecation")
	public void testProcessing() throws Exception {
		ProfileManagerTest.initProfileManager();

		RepeatedValuesProfile profile = new RepeatedValuesProfile();
		Column boolColumn = new Column("Col1", ColumnType.BOOLEAN);
		Column stringColumn = new Column("Col2", ColumnType.VARCHAR);
		Column[] columns = new Column[] { boolColumn, stringColumn };
		SelectItem[] selectItems = new SelectItem[] {
				new SelectItem(boolColumn), new SelectItem(stringColumn) };
		profile.initialize(columns);

		profile.process(new Row(selectItems,
				new Object[] { Boolean.TRUE, "foo" }), 30);
		profile.process(new Row(selectItems, new Object[] { Boolean.FALSE,
				"bar" }), 4);
		profile.process(new Row(selectItems, new Object[] { null, "123" }), 3);
		profile.process(new Row(selectItems,
				new Object[] { Boolean.FALSE, " " }), 2);
		profile.process(new Row(selectItems,
				new Object[] { Boolean.TRUE, "zzz" }), 1);

		IProfileResult result = profile.getResult();
		IMatrix[] matrices = result.getMatrices();
		assertEquals(2, matrices.length);
		assertEquals(
				"Matrix[columnNames={Col1 count,Col1 %},true={MatrixValue[value=31,detailQuery=SELECT Col1, Col2 FROM  WHERE Col1 = 'true'],77%},false={MatrixValue[value=6,detailQuery=SELECT Col1, Col2 FROM  WHERE Col1 = 'false'],15%},null={MatrixValue[value=3,detailQuery=SELECT Col1, Col2 FROM  WHERE Col1 IS NULL],7%}]",
				matrices[0].toString());
		assertEquals(
				"Matrix[columnNames={Col2 count,Col2 %},foo={MatrixValue[value=30,detailQuery=SELECT Col1, Col2 FROM  WHERE Col2 = 'foo'],75%},bar={MatrixValue[value=4,detailQuery=SELECT Col1, Col2 FROM  WHERE Col2 = 'bar'],10%},123={MatrixValue[value=3,detailQuery=SELECT Col1, Col2 FROM  WHERE Col2 = '123'],7%}, ={MatrixValue[value=2,detailQuery=SELECT Col1, Col2 FROM  WHERE Col2 = ' '],5%}]",
				matrices[1].toString());

		profile.setSignificanceRate(20);
		result = profile.getResult();
		matrices = result.getMatrices();
		assertEquals(2, matrices.length);
		assertEquals(
				"Matrix[columnNames={Col1 count,Col1 %},true={MatrixValue[value=31,detailQuery=SELECT Col1, Col2 FROM  WHERE Col1 = 'true'],77%}]",
				matrices[0].toString());
		assertEquals(
				"Matrix[columnNames={Col2 count,Col2 %},foo={MatrixValue[value=30,detailQuery=SELECT Col1, Col2 FROM  WHERE Col2 = 'foo'],75%}]",
				matrices[1].toString());
	}
}