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
package dk.eobjects.datacleaner.profiler;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;

public class MatrixTest extends TestCase {

	public void testGetters() throws Exception {
		String[] columns = { "col1", "col2" };
		String[] rows = { "row1", "row2" };
		MatrixValue[][] numbers = { { mv(1), mv(2) }, { mv(3), mv(4) } };
		SimpleMatrix matrix = new SimpleMatrix(columns, rows, numbers);

		assertEquals("{col1,col2}", ArrayUtils
				.toString(matrix.getColumnNames()));
		assertEquals("{row1,row2}", ArrayUtils.toString(matrix.getRowNames()));
		MatrixValue[][] values = matrix.getValues();
		assertEquals("{{1,2},{3,4}}", ArrayUtils.toString(values));

		assertEquals(2, matrix.getValue("row1", "col2").getValue());
	}

	public void testCommaSeparatedConstructor() throws Exception {
		MatrixValue[][] numbers = { { new MatrixValue(1), new MatrixValue(2) },
				{ new MatrixValue(3), new MatrixValue(4) } };
		SimpleMatrix matrix = new SimpleMatrix("col1, col2", "row1,row2",
				numbers);
		assertEquals("{col1,col2}", ArrayUtils
				.toString(matrix.getColumnNames()));
		assertEquals("{row1,row2}", ArrayUtils.toString(matrix.getRowNames()));
	}

	public void testToString() throws Exception {
		MatrixValue[][] numbers = { { mv(10), mv(20) }, { mv(30), mv(40) } };
		SimpleMatrix matrix = new SimpleMatrix("foo, bar", "data,cleaner",
				numbers);

		assertEquals(
				"Matrix[columnNames={foo,bar},data={10,20},cleaner={30,40}]",
				matrix.toString());
	}

	private MatrixValue mv(Object o) {
		return new MatrixValue(o);
	}
}