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

import java.util.Arrays;

import junit.framework.TestCase;
import org.apache.metamodel.util.DateUtils;
import org.apache.metamodel.util.Month;

public class DateDiffTransformerTest extends TestCase {

	public void testTransform() throws Exception {
		DateDiffTransformer t = new DateDiffTransformer();

		t.setDays(true);
		t.setHours(true);

		assertEquals("[1, 24]",
				Arrays.toString(t.transform(DateUtils.get(2010, Month.JANUARY, 1), DateUtils.get(2010, Month.JANUARY, 2))));

		t.setMinutes(true);

		assertEquals("[2, 48, 2880]",
				Arrays.toString(t.transform(DateUtils.get(2010, Month.JANUARY, 1), DateUtils.get(2010, Month.JANUARY, 3))));

		t.setSeconds(true);
		t.setMilliseconds(true);

		assertEquals("[4017, 96408, 5784480, 347068800, 347068800000]",
				Arrays.toString(t.transform(DateUtils.get(2000, Month.JANUARY, 1), DateUtils.get(2010, Month.DECEMBER, 31))));

		assertEquals("[null, null, null, null, null]",
				Arrays.toString(t.transform(null, DateUtils.get(2000, Month.JANUARY, 1))));
		assertEquals("[null, null, null, null, null]",
				Arrays.toString(t.transform(DateUtils.get(2000, Month.JANUARY, 1), null)));
		assertEquals("[null, null, null, null, null]", Arrays.toString(t.transform(null, null)));

		t.setDays(false);
		t.setHours(false);
		t.setMinutes(false);
		t.setSeconds(false);
		t.setMilliseconds(false);

		// when all measures are turned off, millis will remain
		assertEquals("[347068800000]",
				Arrays.toString(t.transform(DateUtils.get(2000, Month.JANUARY, 1), DateUtils.get(2010, Month.DECEMBER, 31))));
	}
}
