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
package org.datacleaner.components.fuse;

import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.MutableTable;
import org.datacleaner.api.InputColumn;
import org.datacleaner.data.MockInputColumn;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class CoalesceUnitTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testGetOutputDataType() throws Exception {
        MockInputColumn<?> numberCol1 = new MockInputColumn<>("num1", Number.class);
        MockInputColumn<?> numberCol2 = new MockInputColumn<>("num1", Number.class);
        MockInputColumn<?> integerCol1 = new MockInputColumn<>("int1", Integer.class);
        MockInputColumn<?> integerCol2 = new MockInputColumn<>("int2", Integer.class);
        MockInputColumn<?> stringCol1 = new MockInputColumn<>("str1", String.class);
        MockInputColumn<?> stringCol2 = new MockInputColumn<>("str2", String.class);
        MockInputColumn<?> objCol1 = new MockInputColumn<>("obj1", Object.class);

        InputColumn<?>[] allColumns = new InputColumn[] { numberCol1, numberCol2, integerCol1, integerCol2, stringCol1,
                stringCol2, objCol1 };

        // common ancestors
        assertEquals(String.class,
                new CoalesceUnit(stringCol1, stringCol2).updateInputColumns(allColumns).getOutputDataType());
        assertEquals(Number.class,
                new CoalesceUnit(numberCol1, numberCol2).updateInputColumns(allColumns).getOutputDataType());
        assertEquals(Integer.class,
                new CoalesceUnit("int1", "int2").updateInputColumns(allColumns).getOutputDataType());
        assertEquals(Number.class, new CoalesceUnit(numberCol1, integerCol2, integerCol1).updateInputColumns(allColumns)
                .getOutputDataType());
        assertEquals(Number.class, new CoalesceUnit(integerCol2, integerCol1, numberCol1).updateInputColumns(allColumns)
                .getOutputDataType());

        // no common ancestors
        assertEquals(Object.class, new CoalesceUnit(stringCol1, stringCol1, integerCol1).updateInputColumns(allColumns).getOutputDataType());
        assertEquals(Object.class, new CoalesceUnit(stringCol1, stringCol1, objCol1).updateInputColumns(allColumns).getOutputDataType());
        assertEquals(Object.class, new CoalesceUnit(objCol1).updateInputColumns(allColumns).getOutputDataType());
    }

    @Test
    public void testRefreshInputColumns() throws Exception {
        // original columns - col1 and col2
        InputColumn<?> col1 = new MockInputColumn<>("foo1", String.class);
        InputColumn<?> col2 = new MockInputColumn<>("foo2", String.class);
        InputColumn<?>[] allInputColumns1 = new InputColumn[] { col1, col2 };

        // cloned columns - col3 and col4
        InputColumn<?> col3 = new MockInputColumn<>("foo1", String.class);
        InputColumn<?> col4 = new MockInputColumn<>("foo2", String.class);
        InputColumn<?>[] allInputColumns2 = new InputColumn[] { col3, col4 };

        CoalesceUnit unit = new CoalesceUnit(allInputColumns1);
        Assert.assertArrayEquals(allInputColumns1, unit.getInputColumns());

        unit = unit.updateInputColumns(allInputColumns2);
        Assert.assertArrayEquals(allInputColumns2, unit.getInputColumns());
    }

    private <E> MockInputColumn<? extends E> createColumn(String tableName, String columnName, Class<E> clazz){
        return new MockInputColumn<>(columnName, clazz, new MutableColumn(columnName, new MutableTable(tableName)));
    }

    @Test
    public void testPathsVsNames() throws Exception {
        InputColumn<?>[] colsA =
                { createColumn("a", "foo1", String.class), createColumn("a", "foo2", String.class) };
        InputColumn<?>[] colsB =
                { createColumn("b", "foo1", String.class), createColumn("b", "foo2", String.class) };
        InputColumn<?>[] mixedCols = { colsA[0], colsA[1], colsB[0], colsB[1]};

        CoalesceUnit namesCoalesceUnit = new CoalesceUnit("foo1", "foo2");
        CoalesceUnit pathsCoalesceUnit = new CoalesceUnit("b.foo1", "b.foo2");

        // Pure legacy.
        Assert.assertArrayEquals(colsA, namesCoalesceUnit.updateInputColumns(colsA).getInputColumns());

        // Pure path-based.
        Assert.assertArrayEquals(colsB, pathsCoalesceUnit.updateInputColumns(colsB).getInputColumns());

        // Path columns on legacy names.
        Assert.assertArrayEquals(colsB, namesCoalesceUnit.updateInputColumns(colsB).getInputColumns());

        // Mixed columns with correct path as well as incorrect path. Makes sure that the correct path is found.
        Assert.assertArrayEquals(colsB, namesCoalesceUnit.updateInputColumns(mixedCols).getInputColumns());

        expectedException.expect(IllegalStateException.class);
        pathsCoalesceUnit.updateInputColumns(colsA);
    }
}
