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
package org.datacleaner.beans.numbers;

import java.util.Arrays;

import org.datacleaner.api.InputColumn;
import org.datacleaner.beans.numbers.GenerateIdTransformer.IdType;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;

import junit.framework.TestCase;

public class GenerateIdTransformerTest extends TestCase {

    private GenerateIdTransformer transformer;
    private InputColumn<Number> col;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        transformer = new GenerateIdTransformer();
        col = new MockInputColumn<Number>("number");
        transformer.columnInScope = col;
    }

    public void testDefaultIdType() throws Exception {
        // Not setting any IdType explicitly
        assertEquals("[1]", Arrays.toString(transformer.transform(new MockInputRow().put(col, null))));
        assertEquals("[2]", Arrays.toString(transformer.transform(new MockInputRow().put(col, null))));
        assertEquals("[3]", Arrays.toString(transformer.transform(new MockInputRow().put(col, null))));
        assertEquals("[4]", Arrays.toString(transformer.transform(new MockInputRow().put(col, null))));
    }
    
    public void testSequentialId() throws Exception {
        transformer.idType = IdType.SEQUENCE;
        assertEquals("[1]", Arrays.toString(transformer.transform(new MockInputRow().put(col, null))));
        assertEquals("[2]", Arrays.toString(transformer.transform(new MockInputRow().put(col, null))));
        assertEquals("[3]", Arrays.toString(transformer.transform(new MockInputRow().put(col, null))));
        assertEquals("[4]", Arrays.toString(transformer.transform(new MockInputRow().put(col, null))));
    }
    
    public void testRowId() throws Exception {
        transformer.idType = IdType.ROWID;
        
        final Integer[] result1 = transformer.transform(new MockInputRow().put(col, null));
        final Integer[] result2 = transformer.transform(new MockInputRow().put(col, null));
        final Integer[] result3 = transformer.transform(new MockInputRow().put(col, null));
        final Integer[] result4 = transformer.transform(new MockInputRow().put(col, null));
        
        final int id1 = result1[0];
        final int id2 = result2[0];
        final int id3 = result3[0];
        final int id4 = result4[0];
        
        assertTrue(id1 < id2);
        assertTrue(id2 < id3);
        assertTrue(id3 < id4);
    }

}
