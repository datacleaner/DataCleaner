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

    private HashTransformer _transformer;
    private MockInputColumn<String> _column;

    @Before
    public void before() throws Exception {
        _column = new MockInputColumn<>("mock", String.class);
        _transformer = new HashTransformer(new InputColumn[] { _column }, HashTransformer.Algorithm.SHA_512);
    }

    @Test
    public void testSampleOutput() throws Exception {
        final Map<InputColumn<?>, Object> values = new HashMap<>();
        final String input = "This is the input value...";
        values.put(_column, input);
        final MockInputRow row = new MockInputRow(values);
        final String[] output = _transformer.transform(row);
        assertEquals(""+input.length(), output[1]);
        assertEquals(
                "6E7380FF452E4A0CB7DE10132EE9DC266D17D27C2DE1A90BDDD34BF3EF6838FDDF7B96CBD962AF802471BDD5326802F9F2D7427F253CDF9D2E47ACB27E68A6C",
                output[0]);
    }

    @Test
    public void testGetOutputColumns() throws Exception {
        assertEquals(2, _transformer.getOutputColumns().getColumnCount());
        assertEquals(String.class, _transformer.getOutputColumns().getColumnType(0));
        assertEquals("Input length", _transformer.getOutputColumns().getColumnName(1));
        assertTrue(_transformer.getOutputColumns().getColumnName(0).startsWith("Hash of"));
    }
}