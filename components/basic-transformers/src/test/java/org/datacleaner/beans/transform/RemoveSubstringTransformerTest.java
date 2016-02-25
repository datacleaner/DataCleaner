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

    private RemoveSubstringTransformer _transformer;
    private MockInputRow _inputRow;
    private InputColumn<String> _stringColumn;

    @Before
    public void before() {
        final InputColumn<String> baseColumn = new MockInputColumn<>("baseCol", String.class);
        _stringColumn = new MockInputColumn<>("stringCol", String.class);
        final InputColumn<?> numberColumn = new MockInputColumn<>("numberCol", Number.class);
        final InputColumn<?> listColumn = new MockInputColumn<>("listCol", List.class);
        // Just to test that other types doesn't sent it spinning
        final InputColumn<?> booleanColumn = new MockInputColumn<>("booleanCol", Boolean.class);

        final InputColumn<?>[] subtractColumns = new InputColumn[] { _stringColumn, numberColumn, listColumn,
                booleanColumn };

        _inputRow = new MockInputRow().put(baseColumn, "hello goodbye, 5, a, 2, c, true, false").put(_stringColumn,
                "bye").put(numberColumn, 5).put(listColumn, Arrays.asList("a", 2, false)).put(booleanColumn, true);

        _transformer = new RemoveSubstringTransformer();
        _transformer.baseColumn = baseColumn;
        _transformer.substringColumns = subtractColumns;
    }

    @Test
    public void testGetOutputColumns() {
        Assert.assertEquals(1, _transformer.getOutputColumns().getColumnCount());
        Assert.assertEquals(String.class, _transformer.getOutputColumns().getColumnType(0));
        Assert.assertEquals("baseCol (substring removed)", _transformer.getOutputColumns().getColumnName(0));
    }

    @Test
    public void testTransformSimpleRemoval() {
        String[] result = _transformer.transform(_inputRow);
        Assert.assertEquals(1, result.length);
        Assert.assertEquals("hello good, , , , c, , flse", result[0]);
    }

    @Test
    public void testTransformSimpleCaseSensitiveNonMatch() {
        String[] result = _transformer.transform(new MockInputRow().put(_transformer.baseColumn,
                "GOOD BYE CASE SENSITIVE GUY"));
        Assert.assertEquals(1, result.length);
        Assert.assertEquals("GOOD BYE CASE SENSITIVE GUY", result[0]);
    }

    @Test
    public void testTransformSimpleNonRemoval() {
        String[] result = _transformer.transform(new MockInputRow().put(_transformer.baseColumn,
                "nothing of interest here"));
        Assert.assertEquals(1, result.length);
        Assert.assertEquals("nothing of interest here", result[0]);
    }

    @Test
    public void testTransformNullBaseColumn() {
        String[] result = _transformer.transform(new MockInputRow().put(_transformer.baseColumn, null));
        Assert.assertEquals(1, result.length);
        Assert.assertEquals(null, result[0]);
    }

    @Test
    public void testTransformNullReplacementColumn() {
        String[] result = _transformer.transform(new MockInputRow().put(_transformer.baseColumn, "foo"));
        Assert.assertEquals(1, result.length);
        Assert.assertEquals("foo", result[0]);
    }

    @Test
    public void testTransformWholeWords() {
        _transformer.wholeWordsOnly = true;
        String[] result = _transformer.transform(_inputRow);
        Assert.assertEquals(1, result.length);
        Assert.assertEquals("hello goodbye, , , , c, , ", result[0]);
    }

    @Test
    public void testTransformCaseInsensitive() throws Exception {
        _transformer.caseSensitive = false;
        _transformer.wholeWordsOnly = false;

        String[] result = _transformer.transform(new MockInputRow().put(_transformer.baseColumn, "HELLO GOODBYE DUDE")
                .put(_stringColumn, "bye"));
        Assert.assertEquals(1, result.length);
        Assert.assertEquals("HELLO GOOD DUDE", result[0]);
    }

    @Test
    public void testTransformCaseInsensitiveWholeWords() throws Exception {
        _transformer.caseSensitive = false;
        _transformer.wholeWordsOnly = true;

        String[] result1 = _transformer.transform(new MockInputRow().put(_transformer.baseColumn, "HELLO GOODBYE DUDE")
                .put(_stringColumn, "bye"));
        Assert.assertEquals(1, result1.length);
        Assert.assertEquals("HELLO GOODBYE DUDE", result1[0]);

        String[] result2 = _transformer.transform(new MockInputRow().put(_transformer.baseColumn, "HELLO GOOD BYE DUDE")
                .put(_stringColumn, "bye"));
        Assert.assertEquals(1, result2.length);
        Assert.assertEquals("HELLO GOOD  DUDE", result2[0]);
    }
}