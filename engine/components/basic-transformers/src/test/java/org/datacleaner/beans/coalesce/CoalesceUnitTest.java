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
package org.datacleaner.beans.coalesce;

import junit.framework.TestCase;

import org.datacleaner.data.InputColumn;
import org.datacleaner.data.MockInputColumn;

public class CoalesceUnitTest extends TestCase {

    public void testGetOutputDataType() throws Exception {
        MockInputColumn<?> numberCol1 = new MockInputColumn<Number>("num1", Number.class);
        MockInputColumn<?> numberCol2 = new MockInputColumn<Number>("num1", Number.class);
        MockInputColumn<?> integerCol1 = new MockInputColumn<Integer>("int1", Integer.class);
        MockInputColumn<?> integerCol2 = new MockInputColumn<Integer>("int2", Integer.class);
        MockInputColumn<?> stringCol1 = new MockInputColumn<String>("str1", String.class);
        MockInputColumn<?> stringCol2 = new MockInputColumn<String>("str2", String.class);
        MockInputColumn<?> objCol1 = new MockInputColumn<Object>("obj1", Object.class);

        InputColumn<?>[] allColumns = new InputColumn[] { numberCol1, numberCol2, integerCol1, integerCol2, stringCol1,
                stringCol2, objCol1 };

        // common ancestors
        assertEquals(String.class, new CoalesceUnit(stringCol1, stringCol2).getOutputDataType(allColumns));
        assertEquals(Number.class, new CoalesceUnit(numberCol1, numberCol2).getOutputDataType(allColumns));
        assertEquals(Integer.class, new CoalesceUnit("int1", "int2").getOutputDataType(allColumns));
        assertEquals(Number.class, new CoalesceUnit(numberCol1, integerCol2, integerCol1).getOutputDataType(allColumns));
        assertEquals(Number.class, new CoalesceUnit(integerCol2, integerCol1, numberCol1).getOutputDataType(allColumns));

        // no common ancestors
        assertEquals(Object.class, new CoalesceUnit(stringCol1, stringCol1, integerCol1).getOutputDataType(allColumns));
        assertEquals(Object.class, new CoalesceUnit(stringCol1, stringCol1, objCol1).getOutputDataType(allColumns));
        assertEquals(Object.class, new CoalesceUnit(objCol1).getOutputDataType(allColumns));
    }
}
