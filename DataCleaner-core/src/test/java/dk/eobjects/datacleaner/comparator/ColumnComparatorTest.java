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
package dk.eobjects.datacleaner.comparator;

import org.apache.commons.lang.ArrayUtils;

import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.MatrixValue;
import dk.eobjects.metamodel.schema.Column;
import junit.framework.TestCase;

public class ColumnComparatorTest extends TestCase {

	public void testMultipleColumns() throws Exception {
		ColumnComparator c = new ColumnComparator();
		Column fooColumn = new Column("foo");
		Column barColumn = new Column("bar");
		Column bahColumn = new Column("bah");
		c.initialize(fooColumn, barColumn, bahColumn);

		c.processValue(fooColumn, "w00p", 3);
		c.processValue(fooColumn, "w00pah", 2);
		c.processValue(bahColumn, "w00pah", 1);

		assertEquals(
				"Matrix[columnNames={foo,bar,bah},w00p={3,0,0},w00pah={2,0,1}]",
				c.getResult().toString());
		

		assertEquals("Matrix[columnNames={foo,bar,bah},w00p={3,0,0},w00pah={2,0,1}]", c
				.getResultForColumn(fooColumn).toString());
		assertEquals("Matrix[columnNames={foo,bar,bah}]", c.getResultForColumn(
				barColumn).toString());
		assertEquals("Matrix[columnNames={foo,bar,bah},w00pah={2,0,1}]", c.getResultForColumn(
				bahColumn).toString());

		c.processValue(barColumn, "w00pah", 2);

		assertEquals(
				"Matrix[columnNames={foo,bar,bah},w00p={3,0,0},w00pah={2,2,1}]",
				c.getResult().toString());

		c.processValue(bahColumn, "w00pah", 2);

		assertEquals("Matrix[columnNames={foo,bar,bah},w00p={3,0,0}]", c
				.getResult().toString());

		c.processValue(barColumn, "w00p", 4);
		c.processValue(bahColumn, "w00p", 4);

		assertEquals("Matrix[columnNames={foo,bar,bah},w00p={3,4,4}]", c
				.getResult().toString());
	}

	public void testOnlyInequalities() throws Exception {
		ColumnComparator c = new ColumnComparator();
		Column fooColumn = new Column("foo");
		Column barColumn = new Column("bar");
		c.initialize(fooColumn, barColumn);

		c.processValue(fooColumn, "w00p", 3);
		c.processValue(fooColumn, "w00pah", 2);
		assertEquals("Matrix[columnNames={foo,bar},w00p={3,0},w00pah={2,0}]", c
				.getResult().toString());

		c.processValue(barColumn, "w00p", 3);
		assertEquals("Matrix[columnNames={foo,bar},w00pah={2,0}]", c
				.getResult().toString());

		c.processValue(barColumn, "w00pah", 2);
		assertEquals("Matrix[columnNames={foo,bar}]", c.getResult().toString());

		IMatrix result = c.getResult();
		MatrixValue[][] values = result.getValues();
		assertEquals("{}", ArrayUtils.toString(values));
	}
}