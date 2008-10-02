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

public class ProfileResultTest extends TestCase {

	public void testAddMatrix() throws Exception {
		ProfileResult profileResult = new ProfileResult(
				new BasicProfileDescriptor());
		MatrixValue[][] numbers = { { mv(1), mv(2) }, { mv(3), mv(4) } };
		SimpleMatrix matrix = new SimpleMatrix("col1,col2", "row1,row2",
				numbers);
		profileResult.addMatrix(matrix);
		IMatrix[] matrices = profileResult.getMatrices();

		assertEquals(1, matrices.length);
		assertSame(matrix, matrices[0]);
	}

	public void testToString() throws Exception {
		ProfileManagerTest.initProfileManager();

		ProfileResult profileResult = new ProfileResult(
				new BasicProfileDescriptor());
		MatrixValue[][] numbers1 = { { mv(1), mv(2) }, { mv(3), mv(4) } };
		SimpleMatrix matrix1 = new SimpleMatrix("col1,col2", "row1,row2",
				numbers1);
		profileResult.addMatrix(matrix1);
		MatrixValue[][] numbers2 = { { mv(5), mv(6) }, { mv(7), mv(8) } };
		SimpleMatrix matrix2 = new SimpleMatrix("col3,col4", "row3,row4",
				numbers2);
		profileResult.addMatrix(matrix2);

		assertEquals(
				"ProfileResult[profileDescriptor=BasicProfileDescriptor[displayName=null,profileClass=null],matrices={Matrix[columnNames={col1,col2},row1={1,2},row2={3,4}],Matrix[columnNames={col3,col4},row3={5,6},row4={7,8}]}]",
				profileResult.toString());
	}

	private MatrixValue mv(Object o) {
		return new MatrixValue(o);
	}
}