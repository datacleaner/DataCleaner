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
package dk.eobjects.datacleaner.util;

import dk.eobjects.datacleaner.testware.DataCleanerTestCase;

public class AverageBuilderTest extends DataCleanerTestCase {

	public void testAddValue() throws Exception {
		assertEquals(3d, new AverageBuilder().addValue(2).addValue(4).getAverage());
		assertEquals(4.5d, new AverageBuilder().addValue(2).addValue(7).getAverage());
		assertEquals(4d, new AverageBuilder().addValue(2).addValue(4).addValue(4).addValue(6).addValue(3).addValue(5)
				.getAverage());
	}
}
