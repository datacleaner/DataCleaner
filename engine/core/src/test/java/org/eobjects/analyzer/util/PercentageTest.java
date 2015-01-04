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
package org.eobjects.analyzer.util;

import junit.framework.TestCase;

public class PercentageTest extends TestCase {

	public void testParsePercentage() throws Exception {
		Percentage p;
		p = Percentage.parsePercentage("95%");
		assertEquals(0.95, p.doubleValue());
		p = Percentage.parsePercentage("0%");
		assertEquals(0.0, p.doubleValue());
		p = Percentage.parsePercentage("100%");
		assertEquals(1.0, p.doubleValue());
		p = Percentage.parsePercentage("4%");
		assertEquals(0.04, p.doubleValue());

		try {
			Percentage.parsePercentage("4");
			fail("Exception expected");
		} catch (NumberFormatException e) {
			assertEquals("4", e.getMessage());
		}

		try {
			Percentage.parsePercentage(null);
			fail("Exception expected");
		} catch (NumberFormatException e) {
			assertEquals("cannot parse null", e.getMessage());
		}

		try {
			Percentage.parsePercentage("4 %");
			fail("Exception expected");
		} catch (NumberFormatException e) {
			assertEquals("For input string: \"4 \"", e.getMessage());
		}
	}

	public void testEquals() throws Exception {
		final Percentage p1 = Percentage.parsePercentage("95%");
		final Percentage p2 = new Percentage(95);
		assertEquals(p1, p2);
		assertEquals(p1.hashCode(), p2.hashCode());
		assertEquals("95%", p2.toString());
		
		assertEquals(0, p1.intValue());
		assertEquals(0, p1.longValue());
		assertEquals(0.95f, p1.floatValue());
	}
}
