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
package org.datacleaner.extension.networktools;

import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;

import junit.framework.TestCase;

public class NumberToIpConverterTest extends TestCase {

    public void testConvertToNumber() throws Exception {
        final NumberToIpConverter trans = new NumberToIpConverter();
        trans.ipColumn = new MockInputColumn<>("my ip number", Number.class);

        String[] result;

        result = trans.transform(new MockInputRow().put(trans.ipColumn, null));
        assertEquals(1, result.length);
        assertNull(result[0]);

        result = trans.transform(new MockInputRow().put(trans.ipColumn, -1));
        assertEquals(1, result.length);
        assertNull(result[0]);

        result = trans.transform(new MockInputRow().put(trans.ipColumn, 2130706433L));
        assertEquals(1, result.length);
        assertEquals("127.0.0.1", result[0]);

        result = trans.transform(new MockInputRow().put(trans.ipColumn, 4294967295L));
        assertEquals(1, result.length);
        assertEquals("255.255.255.255", result[0]);

        result = trans.transform(new MockInputRow().put(trans.ipColumn, 2825259556L));
        assertEquals(1, result.length);
        assertEquals("168.102.10.36", result[0]);
    }
}
