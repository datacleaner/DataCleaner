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

import org.datacleaner.data.InputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;

import junit.framework.TestCase;

public class MathFormulaTransformerTest extends TestCase {

    private MockInputColumn<Number> foo;
    private MockInputColumn<Number> bar;
    private MockInputColumn<Number> baz;

    private MathFormulaTransformer trans;

    @Override
    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        foo = new MockInputColumn<Number>("foo");
        bar = new MockInputColumn<Number>("BAR");
        baz = new MockInputColumn<Number>("baz");

        trans = new MathFormulaTransformer();
        trans._input = new InputColumn[] { foo, bar, baz };
    }

    public void testVanilla() throws Exception {
        trans._formula = "(FOO + BAR) / baz";
        trans.init();

        assertFormulaResult(2.0, 10, 10, 10);
        assertFormulaResult(10.0, 10, 10, 2);
        assertFormulaResult(5.0, 0, 10, 2);

        trans._formula = "(FOO + BAR) % baz";
        trans.init();

        assertFormulaResult(0, 0, 10, 2);
        assertFormulaResult(4, 3, 11, 5);
    }

    public void testColumnAliases() throws Exception {
        foo.setName("hello world");
        bar.setName("lorem ipsum");
        trans._formula = "(helloworld + lorem_ipsum) / col3";
        trans.init();

        assertFormulaResult(2.0, 10, 10, 10);
        assertFormulaResult(10.0, 10, 10, 2);
        assertFormulaResult(5.0, 0, 10, 2);
    }

    public void testNullVariables() throws Exception {
        trans._formula = "(FOO + BAR) / baz";
        trans.init();

        assertFormulaResult(1.0, null, 10, 10);
    }

    public void testDivideByZero() throws Exception {
        trans._formula = "(FOO + BAR) / baz";
        trans.init();

        assertFormulaResult(null, 10, 10, 0);
        assertFormulaResult(null, -10, -10, 0);
        assertFormulaResult(null, 0, 0, 0);
    }

    private void assertFormulaResult(Number result, Integer fo, Integer br, Integer bz) {
        Number[] arr = trans.transform(new MockInputRow().put(foo, fo).put(bar, br).put(baz, bz));
        assertNotNull(arr);
        assertEquals(1, arr.length);
        if (result == null) {
            assertNull("Expected null but got: " + arr[0], arr[0]);
        } else {
            if (arr[0] == null) {
                fail("Expected " + result + " but got null!");
            }
            double d1 = result.doubleValue();
            double d2 = arr[0].doubleValue();
            assertEquals(d1, d2);
        }
    }
}
