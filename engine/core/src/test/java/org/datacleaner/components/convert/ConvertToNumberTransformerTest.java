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
package org.datacleaner.components.convert;

import org.datacleaner.components.convert.ConvertToNumberTransformer;

import junit.framework.TestCase;

public class ConvertToNumberTransformerTest extends TestCase {

	public void testParseWithSpecialSeparators() throws Exception {
		ConvertToNumberTransformer trans = new ConvertToNumberTransformer('|', '-', '!');

		assertEquals(-1000.01, trans.transform("!1-000|01"));
		assertEquals(-1000.01, trans.transform("!1000|01"));

		Number res = trans.transform("1000");
		assertEquals(Long.class, res.getClass());
		assertEquals(1000l, res);
	}
	
	public void testTransformValueJavaSymbols() throws Exception {
		assertEquals(2000.01, ConvertToNumberTransformer.transformValue("2000.01"));
		assertEquals(2000l, ConvertToNumberTransformer.transformValue("2000.0"));
		assertEquals(-2000l, ConvertToNumberTransformer.transformValue("-2,000.0"));
		assertEquals(987654321l, ConvertToNumberTransformer.transformValue("987654321"));
		
		assertEquals(2000l, ConvertToNumberTransformer.transformValue("+ 2 000"));
	}
	
	public void testTransformUntrimmed() throws Exception {
	    assertEquals(2309628.8, ConvertToNumberTransformer.transformValue("    2309628.8"));
    }
}
