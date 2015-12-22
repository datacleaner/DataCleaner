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

import java.util.Arrays;
import java.util.List;

import org.datacleaner.api.InputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class RemoveSubstringTransformerTest {

    private RemoveSubstringTransformer _t;
    private MockInputRow _inputRow;

    @Before
    public void before() {
        final InputColumn<String> baseColumn = new MockInputColumn<>("baseCol", String.class);
        final InputColumn<?> stringColumn = new MockInputColumn<>("stringCol", String.class);
        final InputColumn<?> numberColumn = new MockInputColumn<>("numberCol", Number.class);
        final InputColumn<?> listColumn = new MockInputColumn<>("listCol", List.class);
        // Just to test that other types doesn't sent it spinning
        final InputColumn<?> booleanColumn = new MockInputColumn<>("booleanCol", Boolean.class);

        final InputColumn<?>[] subtractColumns =
                new InputColumn[] { stringColumn, numberColumn, listColumn, booleanColumn };

        _inputRow = new MockInputRow().put(baseColumn, "hello goodbye, 5, a, 2, c, true, false")
                .put(subtractColumns[0], "bye").put(subtractColumns[1], 5).put(subtractColumns[2],
                        Arrays.asList("a", 2, false)).put(subtractColumns[3], true);

        _t = new RemoveSubstringTransformer();
        _t.baseColumn = baseColumn;
        _t.substringColumns = subtractColumns;
    }

    @Test
    public void testGetOutputColumns() {
        Assert.assertEquals(1, _t.getOutputColumns().getColumnCount());
        Assert.assertEquals(String.class, _t.getOutputColumns().getColumnType(0));
        Assert.assertEquals("baseCol (substring removed)", _t.getOutputColumns().getColumnName(0));
    }

    @Test
    public void testTransformSimple() {
        String[] result = _t.transform(_inputRow);
        Assert.assertEquals(1, result.length);
        Assert.assertEquals("hello good, , , , c, , flse", result[0]);
    }

    @Test
    public void testTransformWholeWords() {
        _t.wholeWordsOnly = true;
        String[] result = _t.transform(_inputRow);
        Assert.assertEquals(1, result.length);
        Assert.assertEquals("hello goodbye, , , , c, , ", result[0]);
    }
}