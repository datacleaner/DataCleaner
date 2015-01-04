/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.beans.transform;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import junit.framework.TestCase;

import org.datacleaner.beans.api.OutputColumns;
import org.datacleaner.beans.transform.DatePartTransformer.WeekDay;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.apache.metamodel.util.DateUtils;
import org.apache.metamodel.util.Month;

public class DatePartTransformerTest extends TestCase {

	public void testTransformDefaultDateConfiguration() throws Exception {
		DatePartTransformer transformer = new DatePartTransformer();
		MockInputColumn<Date> column = new MockInputColumn<Date>("my date", Date.class);
		transformer.column = column;

		OutputColumns outputColumns = transformer.getOutputColumns();
		assertEquals(3, outputColumns.getColumnCount());
		assertEquals("my date (year)", outputColumns.getColumnName(0));
		assertEquals("my date (month)", outputColumns.getColumnName(1));
		assertEquals("my date (day of month)", outputColumns.getColumnName(2));

		Date date = DateUtils.get(2011, Month.MARCH, 16);
		Number[] result = transformer.transform(new MockInputRow().put(column, date));
		assertEquals(3, result.length);
		assertEquals(2011, result[0]);
		assertEquals(3, result[1]);
		assertEquals(16, result[2]);
	}

	public void testMondayIs1AndSundayIs7() throws Exception {
		DatePartTransformer transformer = new DatePartTransformer();
		MockInputColumn<Date> column = new MockInputColumn<Date>("my date", Date.class);
		transformer.column = column;
		transformer.year = false;
		transformer.month = false;
		transformer.dayOfMonth = false;
		transformer.dayOfWeek = true;
		transformer.weekNumber = true;
		transformer.firstDayOfWeek = WeekDay.MONDAY;
		transformer.minimalDaysInFirstWeek = 4;
		
		transformer.init();

		OutputColumns outputColumns = transformer.getOutputColumns();
		assertEquals(2, outputColumns.getColumnCount());
		assertEquals("my date (day of week)", outputColumns.getColumnName(0));
		assertEquals("my date (week number)", outputColumns.getColumnName(1));
		
		Date monday = DateUtils.get(2011, Month.NOVEMBER, 7);
		Date sunday = DateUtils.get(2011, Month.NOVEMBER, 6);
		
		Number[] mondayResult = transformer.transform(new MockInputRow().put(column, monday));
		assertEquals("[1, 45]", Arrays.toString(mondayResult));
		Number[] sundayResult = transformer.transform(new MockInputRow().put(column, sunday));
		assertEquals("[7, 44]", Arrays.toString(sundayResult));
	}
	
	public void testCompareSundayAndMonday() throws Exception {
		WeekDay sunday = DatePartTransformer.WeekDay.SUNDAY;
		WeekDay monday = DatePartTransformer.WeekDay.MONDAY;
		assertEquals(0, sunday.compareTo(sunday));
		assertEquals(6, sunday.compareTo(monday));
		assertEquals(-6, monday.compareTo(sunday));
	}

	public void testNullDate() throws Exception {
		DatePartTransformer transformer = new DatePartTransformer();
		MockInputColumn<Date> column = new MockInputColumn<Date>("my date", Date.class);
		transformer.column = column;

		Number[] result = transformer.transform(new MockInputRow().put(column, null));
		assertEquals(3, result.length);
		assertEquals(null, result[0]);
		assertEquals(null, result[1]);
		assertEquals(null, result[2]);
	}

	public void testTransformTime() throws Exception {
		DatePartTransformer transformer = new DatePartTransformer();
		MockInputColumn<Date> column = new MockInputColumn<Date>("my time", Date.class);
		transformer.column = column;
		transformer.year = false;
		transformer.month = false;
		transformer.dayOfMonth = false;
		transformer.hour = true;
		transformer.minute = true;
		transformer.second = true;

		OutputColumns outputColumns = transformer.getOutputColumns();
		assertEquals(3, outputColumns.getColumnCount());
		assertEquals("my time (hour)", outputColumns.getColumnName(0));
		assertEquals("my time (minute)", outputColumns.getColumnName(1));
		assertEquals("my time (second)", outputColumns.getColumnName(2));

		Date date = new SimpleDateFormat("HH:mm:ss").parse("13:21:55");
		Number[] result = transformer.transform(date);
		assertEquals(3, result.length);
		assertEquals(13, result[0]);
		assertEquals(21, result[1]);
		assertEquals(55, result[2]);
	}
}
