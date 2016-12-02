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
package org.datacleaner.beans.codec;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.datacleaner.api.InputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.junit.Before;
import org.junit.Test;

public class HashTransformerTest {
    private static final String INPUT = "This is the input value...";

    private MockInputColumn<String> _column;

    @Before
    public void before() throws Exception {
        _column = new MockInputColumn<>("mock", String.class);
    }

    @Test
    public void testSampleOutput() throws Exception {
        compare(HashTransformer.Algorithm.SHA_512, INPUT, "06E7380FF452E4A0CB7DE10132EE9DC266D17D27C2DE1A90BDDD34BF3"
                + "EF6838FDDF7B96CBD962AF802471BDD5326802F9F2D7427F253CDF9D2E47ACB27E68A6C");
    }

    private void compare(final HashTransformer.Algorithm algorithm, final String input, final String expectedOutput) {
        final HashTransformer transformer = new HashTransformer(new InputColumn[] { _column }, algorithm);
        final Map<InputColumn<?>, Object> values = new HashMap<>();
        values.put(_column, input);
        final MockInputRow row = new MockInputRow(values);
        final String[] output = transformer.transform(row);
        assertEquals(String.valueOf(input.length()), output[1]);
        assertEquals(expectedOutput, output[0]);
    }

    @Test
    public void testGetOutputColumns() throws Exception {
        final HashTransformer transformer = new HashTransformer(new InputColumn[] { _column },
                HashTransformer.Algorithm.SHA_512);
        assertEquals(2, transformer.getOutputColumns().getColumnCount());
        assertEquals(String.class, transformer.getOutputColumns().getColumnType(0));
        assertEquals("Input length", transformer.getOutputColumns().getColumnName(1));
        assertTrue(transformer.getOutputColumns().getColumnName(0).startsWith("Hash of"));
    }
}
