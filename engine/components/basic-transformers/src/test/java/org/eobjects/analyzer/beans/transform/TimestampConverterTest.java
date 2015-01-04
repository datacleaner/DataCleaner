/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.beans.transform;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.eobjects.analyzer.beans.transform.TimestampConverter.Unit;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

import junit.framework.TestCase;

public class TimestampConverterTest extends TestCase {

	private TimeZone _defaultTimeZone;

	protected void setUp() throws Exception {
		_defaultTimeZone = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	};

	@Override
	protected void tearDown() throws Exception {
		TimeZone.setDefault(_defaultTimeZone);
	}

	public void testTransform() throws Exception {
		TimestampConverter trans = new TimestampConverter();
		MockInputColumn<Object> col = new MockInputColumn<Object>(
				"my timestamps", Object.class);
		trans.timestampColumn = col;

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		Date[] result;
		result = trans.transform(new MockInputRow().put(col, "1320244696"));
		assertEquals(1, result.length);
		assertEquals("2011-11-02 14:38", dateFormat.format(result[0]));

		result = trans.transform(new MockInputRow().put(col, 1234));
		assertEquals(1, result.length);
		assertEquals("1970-01-01 00:20", dateFormat.format(result[0]));

		result = trans.transform(new MockInputRow().put(col, null));
		assertEquals(1, result.length);
		assertNull(result[0]);

		result = trans.transform(new MockInputRow().put(col, "foobar"));
		assertEquals(1, result.length);
		assertNull(result[0]);

		trans.unit = Unit.DAYS;
		result = trans.transform(new MockInputRow().put(col, 20));
		assertEquals(1, result.length);
		assertEquals("1970-01-21 00:00", dateFormat.format(result[0]));
	}
}
