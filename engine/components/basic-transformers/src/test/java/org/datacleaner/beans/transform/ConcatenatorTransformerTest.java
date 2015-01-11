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

import junit.framework.TestCase;

import org.datacleaner.api.InputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;

public class ConcatenatorTransformerTest extends TestCase {

    public void testConcat() throws Exception {
        InputColumn<String> col1 = new MockInputColumn<String>("str", String.class);
        InputColumn<Boolean> col2 = new MockInputColumn<Boolean>("bool", Boolean.class);

        ConcatenatorTransformer t = new ConcatenatorTransformer(" + ", new InputColumn[] { col1, col2 });

        assertEquals(1, t.getOutputColumns().getColumnCount());
        assertEquals("Concat of str,bool", t.getOutputColumns().getColumnName(0));

        String[] result = t.transform(new MockInputRow().put(col1, "hello").put(col2, true));
        assertEquals(1, result.length);
        assertEquals("hello + true", result[0]);

        result = t.transform(new MockInputRow().put(col1, "hi").put(col2, ""));
        assertEquals(1, result.length);
        assertEquals("hi", result[0]);

        result = t.transform(new MockInputRow().put(col1, "hi").put(col2, null));
        assertEquals(1, result.length);
        assertEquals("hi", result[0]);

        result = t.transform(new MockInputRow().put(col1, null).put(col2, true));
        assertEquals(1, result.length);
        assertEquals("true", result[0]);

        result = t.transform(new MockInputRow().put(col1, null).put(col2, null));
        assertEquals(1, result.length);
        assertEquals("", result[0]);
    }
}
