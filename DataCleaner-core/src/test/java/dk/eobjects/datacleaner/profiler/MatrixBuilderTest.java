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

public class MatrixBuilderTest extends TestCase {

	public void testBuildSimpleMatrix() throws Exception {
		MatrixBuilder matrixBuilder = new MatrixBuilder();
		assertTrue(matrixBuilder.isEmpty());
		matrixBuilder.addRow("row1");
		assertTrue(matrixBuilder.isEmpty());
		matrixBuilder.addColumn("col1", 1);
		assertFalse(matrixBuilder.isEmpty());
		matrixBuilder.addColumn("col2", 2);
		matrixBuilder.addRow("row2", 3, 4);
		IMatrix matrix = matrixBuilder.getMatrix();
		assertEquals("Matrix[columnNames={col1,col2},row1={1,2},row2={3,4}]",
				matrix.toString());

		try {
			matrixBuilder.addColumn("col3", 2);
			fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid size of values, expected: 2, actual: 1", e
					.getMessage());
		}
		try {
			matrixBuilder.addRow("row3", 2, 43, 2, 2);
			fail("Exception should have been thrown");
		} catch (IllegalArgumentException e) {
			assertEquals("Invalid size of values, expected: 2, actual: 4", e
					.getMessage());
		}

		matrixBuilder.addRow("row3", 5, 6);
		matrix = matrixBuilder.getMatrix();
		assertEquals(
				"Matrix[columnNames={col1,col2},row1={1,2},row2={3,4},row3={5,6}]",
				matrix.toString());

		matrixBuilder.replaceValue(1, 0, 7);
		matrix = matrixBuilder.getMatrix();
		assertEquals(
				"Matrix[columnNames={col1,col2},row1={1,2},row2={7,4},row3={5,6}]",
				matrix.toString());

		matrixBuilder.replaceValue("col2", "row1", 8);
		matrix = matrixBuilder.getMatrix();
		assertEquals(
				"Matrix[columnNames={col1,col2},row1={1,8},row2={7,4},row3={5,6}]",
				matrix.toString());
	}

	public void testSorting() throws Exception {
		MatrixBuilder matrixBuilder = new MatrixBuilder();
		matrixBuilder.addColumn("column1");
		matrixBuilder.addColumn("column2");
		matrixBuilder.addRow("pattern 1", 3, 1);
		matrixBuilder.addRow("pattern 2", 10, 2);
		matrixBuilder.addRow("pattern 3", 1, 3);
		matrixBuilder.addRow("pattern 3", 3, 2);
		matrixBuilder.addRow("pattern 4", 4, 4);
		IMatrix matrix = matrixBuilder.getMatrix();
		assertEquals(
				"Matrix[columnNames={column1,column2},pattern 1={3,1},pattern 2={10,2},pattern 3={1,3},pattern 3={3,2},pattern 4={4,4}]",
				matrix.toString());

		matrixBuilder.sortColumn(0, MatrixBuilder.DESCENDING);
		matrix = matrixBuilder.getMatrix();
		assertEquals(
				"Matrix[columnNames={column1,column2},pattern 2={10,2},pattern 4={4,4},pattern 3={3,2},pattern 1={3,1},pattern 3={1,3}]",
				matrix.toString());

		matrixBuilder.addColumn("column3", 5, 4, 3, 2, 1);
		matrixBuilder.sortColumn("column3", MatrixBuilder.ASCENDING);
		matrix = matrixBuilder.getMatrix();
		assertEquals(
				"Matrix[columnNames={column1,column2,column3},pattern 3={1,3,1},pattern 1={3,1,2},pattern 3={3,2,3},pattern 4={4,4,4},pattern 2={10,2,5}]",
				matrix.toString());
	}

	public void testSortSimilarValues() throws Exception {
		MatrixBuilder matrixBuilder = new MatrixBuilder();
		matrixBuilder.addColumn("column1");
		matrixBuilder.addColumn("column2");
		matrixBuilder.addRow("pattern 1", "aaa", 1);
		matrixBuilder.addRow("pattern 2", "ccc", 2);
		matrixBuilder.addRow("pattern 3", "aaa", 3);
		matrixBuilder.addRow("pattern 4", "bbb", 4);
		matrixBuilder.addRow("pattern 5", "aaa", 5);

		matrixBuilder.sortColumn(0, MatrixBuilder.DESCENDING);

		assertEquals(
				"Matrix[columnNames={column1,column2},pattern 2={ccc,2},pattern 4={bbb,4},pattern 5={aaa,5},pattern 3={aaa,3},pattern 1={aaa,1}]",
				matrixBuilder.getMatrix().toString());

		matrixBuilder.sortColumn(0, MatrixBuilder.ASCENDING);

		assertEquals(
				"Matrix[columnNames={column1,column2},pattern 5={aaa,5},pattern 3={aaa,3},pattern 1={aaa,1},pattern 4={bbb,4},pattern 2={ccc,2}]",
				matrixBuilder.getMatrix().toString());

		matrixBuilder.sortColumn(1, MatrixBuilder.ASCENDING);

		assertEquals(
				"Matrix[columnNames={column1,column2},pattern 1={aaa,1},pattern 2={ccc,2},pattern 3={aaa,3},pattern 4={bbb,4},pattern 5={aaa,5}]",
				matrixBuilder.getMatrix().toString());
	}
}