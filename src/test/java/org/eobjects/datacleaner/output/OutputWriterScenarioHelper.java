/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.datacleaner.output;

import java.util.ArrayList;
import java.util.Date;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.junit.Assert;
import org.junit.Ignore;

import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.util.DateUtils;
import dk.eobjects.metamodel.util.Month;

/**
 * Helper class for reuse by test cases that want to test an OutputWriters
 * ability to write different types of values
 * 
 * @author Kasper Sørensen
 * 
 */
@Ignore
public class OutputWriterScenarioHelper {

	private final ArrayList<InputColumn<?>> columns;
	private final MockInputColumn<String> col1;
	private final MockInputColumn<Integer> col2;
	private final MockInputColumn<Date> col3;
	private final MockInputColumn<Float> col4;

	public OutputWriterScenarioHelper() {
		col1 = new MockInputColumn<String>("str", String.class);
		col2 = new MockInputColumn<Integer>("my int", Integer.class);
		col3 = new MockInputColumn<Date>("my date", Date.class);
		col4 = new MockInputColumn<Float>("my float", Float.class);

		columns = new ArrayList<InputColumn<?>>();
		columns.add(col1);
		columns.add(col2);
		columns.add(col3);
		columns.add(col4);
	}

	private Date getExampleDate() {
		return DateUtils.get(2010, Month.MARCH, 21);
	}

	private Date normalizeDate(Date d) {
		return DateUtils.get(d);
	}

	public ArrayList<InputColumn<?>> getColumns() {
		return columns;
	}

	public void writeExampleData(OutputWriter writer) {
		// write just two columns in a row (the rest should be null
		writer.createRow().setValue(col1, "hello, world").setValue(col2, null).setValue(col3, getExampleDate()).setValue(col4, null).write();

		writer.createRow().setValue(col1, null).setValue(col2, -20).setValue(col3, null).setValue(col4, 3.14f).write();

		writer.close();
	}

	public void performAssertions(DataSet dataSet, boolean typeSafe) {
		Assert.assertTrue(dataSet.next());
		assertEquals("hello, world", dataSet.getRow().getValue(0), typeSafe);
		assertEquals(null, dataSet.getRow().getValue(1), typeSafe);

		assertEquals(getExampleDate(), dataSet.getRow().getValue(2), typeSafe);
		assertEquals(null, dataSet.getRow().getValue(3), typeSafe);

		Assert.assertTrue(dataSet.next());
		assertEquals(null, dataSet.getRow().getValue(0), typeSafe);
		assertEquals(-20, dataSet.getRow().getValue(1), typeSafe);
		assertEquals(null, dataSet.getRow().getValue(2), typeSafe);
		assertEquals(3.14f, dataSet.getRow().getValue(3), typeSafe);

		Assert.assertFalse(dataSet.next());
	}

	private void assertEquals(Object o1, Object o2, boolean typeSafe) {
		if (typeSafe) {
			if (o1 instanceof Date && o2 instanceof Date) {
				Date date1 = (Date) o1;
				Date date2 = normalizeDate((Date) o2);

				long time1 = date1.getTime();
				long time2 = date2.getTime();
				if (time1 != time2) {
					System.out.println("time1: " + time1);
					System.out.println("time2: " + time2);
					System.out.println("Diff: " + (time2 - time1));
					Assert.fail("Non-equal dates: " + date1 + " and " + date2);
				}
			} else if (o1 instanceof Float && o2 instanceof Double) {
				Float f1 = (Float) o1;
				Double d2 = (Double) o2;
				Assert.assertEquals(f1.doubleValue(), d2.doubleValue(), 0.01);
			} else {
				Assert.assertEquals(o1, o2);
			}
		} else {
			if (o1 == null) {
				Assert.assertEquals("", o2);
			} else {
				String s1 = o1.toString();
				String s2 = o2.toString();
				Assert.assertEquals(s1, s2);
			}
		}
	}
}
