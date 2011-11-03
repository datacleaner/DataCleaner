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
package org.eobjects.datacleaner.extension.networktools;

import junit.framework.TestCase;

import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

public class IpToNumberConverterTest extends TestCase {

	public void testConvertToNumber() throws Exception {
		IpToNumberConverter trans = new IpToNumberConverter();
		trans.ipColumn = new MockInputColumn<String>("my ip", String.class);

		Number[] result;

		result = trans.transform(new MockInputRow().put(trans.ipColumn, ""));
		assertEquals(1, result.length);
		assertNull(result[0]);

		result = trans.transform(new MockInputRow().put(trans.ipColumn,
				"foobar"));
		assertEquals(1, result.length);
		assertNull(result[0]);

		result = trans.transform(new MockInputRow().put(trans.ipColumn,
				"4324.42342.43242.43"));
		assertEquals(1, result.length);
		assertNull(result[0]);

		result = trans.transform(new MockInputRow().put(trans.ipColumn,
				"127.0.0.1"));
		assertEquals(1, result.length);
		assertEquals(2130706433l, result[0].longValue());

		result = trans.transform(new MockInputRow().put(trans.ipColumn,
				"255.255.255.255"));
		assertEquals(1, result.length);
		assertEquals(4294967295l, result[0].longValue());

		result = trans.transform(new MockInputRow().put(trans.ipColumn,
				"168.102.10.36"));
		assertEquals(1, result.length);
		assertEquals(2825259556l, result[0].longValue());
	}
}
