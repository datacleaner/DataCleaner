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

import java.util.Calendar;
import java.util.Date;

import org.eobjects.analyzer.beans.transform.DateToAgeTransformer;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

import junit.framework.TestCase;

public class DateToAgeTransformerTest extends TestCase {

	public void testDiffs() throws Exception {
		MockInputColumn<Date> col = new MockInputColumn<Date>("foobar",
				Date.class);

		DateToAgeTransformer t = new DateToAgeTransformer();
		t.setDateColumn(col);

		// 2010-01-01
		Calendar c1 = Calendar.getInstance();
		c1.setTimeInMillis(0l);
		c1.set(Calendar.YEAR, 2010);
		c1.set(Calendar.MONTH, Calendar.JANUARY);
		c1.set(Calendar.DAY_OF_MONTH, 1);
		t.setToday(c1.getTime());

		// 2009-01-02
		Calendar c2 = Calendar.getInstance();
		c2.setTimeInMillis(0l);
		c2.set(Calendar.YEAR, 2009);
		c2.set(Calendar.MONTH, Calendar.JANUARY);
		c2.set(Calendar.DAY_OF_MONTH, 2);

		Integer[] result = t
				.transform(new MockInputRow().put(col, c2.getTime()));
		assertEquals(2, result.length);
		assertEquals(364, result[0].intValue());
		assertEquals(0, result[1].intValue());

		c2.set(Calendar.DAY_OF_MONTH, 1);
		result = t.transform(new MockInputRow().put(col, c2.getTime()));
		assertEquals(2, result.length);
		assertEquals(365, result[0].intValue());
		assertEquals(1, result[1].intValue());

		// 1982-03-11
		c2.set(Calendar.YEAR, 1980);
		c2.set(Calendar.MONTH, Calendar.DECEMBER);
		c2.set(Calendar.DAY_OF_MONTH, 31);

		result = t.transform(new MockInputRow().put(col, c2.getTime()));
		assertEquals(2, result.length);
		assertEquals(10593, result[0].intValue());
		assertEquals(29, result[1].intValue());
	}
}
