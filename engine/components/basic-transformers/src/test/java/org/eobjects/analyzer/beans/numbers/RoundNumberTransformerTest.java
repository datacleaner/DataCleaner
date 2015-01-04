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
package org.eobjects.analyzer.beans.numbers;

import java.util.Arrays;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

import junit.framework.TestCase;

public class RoundNumberTransformerTest extends TestCase {

    private RoundNumberTransformer transformer;
    private InputColumn<Number> col;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        transformer = new RoundNumberTransformer();
        col = new MockInputColumn<Number>("number");
        transformer._number = col;
    }

    public void testNullValues() throws Exception {
        transformer._roundFactor = 1;
        assertEquals("[null]", Arrays.toString(transformer.transform(new MockInputRow().put(col, null))));
    }

    public void testNegativeValues() throws Exception {
        transformer._roundFactor = 1;
        assertEquals("[-10]", Arrays.toString(transformer.transform(new MockInputRow().put(col, -10))));
    }

    public void testRoundToInteger() throws Exception {
        transformer._roundFactor = 1;
        assertEquals("[1]", Arrays.toString(transformer.transform(new MockInputRow().put(col, 0.7))));
        assertEquals("[1]", Arrays.toString(transformer.transform(new MockInputRow().put(col, 0.5))));
        assertEquals("[10]", Arrays.toString(transformer.transform(new MockInputRow().put(col, 10))));
        assertEquals("[100]", Arrays.toString(transformer.transform(new MockInputRow().put(col, 100))));
        assertEquals("[0]", Arrays.toString(transformer.transform(new MockInputRow().put(col, 0.4))));
    }
    
    public void testRoundToSeventh() throws Exception {
        transformer._roundFactor = 7;
        assertEquals("[98]", Arrays.toString(transformer.transform(new MockInputRow().put(col, 100))));        
        assertEquals("[70]", Arrays.toString(transformer.transform(new MockInputRow().put(col, 71))));
        assertEquals("[63]", Arrays.toString(transformer.transform(new MockInputRow().put(col, 65))));
    }

    public void testRoundToHundreds() throws Exception {
        transformer._roundFactor = 100;
        assertEquals("[0]", Arrays.toString(transformer.transform(new MockInputRow().put(col, 0.7))));
        assertEquals("[0]", Arrays.toString(transformer.transform(new MockInputRow().put(col, 0.5))));
        assertEquals("[0]", Arrays.toString(transformer.transform(new MockInputRow().put(col, 10))));
        assertEquals("[100]", Arrays.toString(transformer.transform(new MockInputRow().put(col, 100))));
        assertEquals("[100]", Arrays.toString(transformer.transform(new MockInputRow().put(col, 60))));
        assertEquals("[200]", Arrays.toString(transformer.transform(new MockInputRow().put(col, 160))));
        assertEquals("[-200]", Arrays.toString(transformer.transform(new MockInputRow().put(col, -160))));
    }
}
