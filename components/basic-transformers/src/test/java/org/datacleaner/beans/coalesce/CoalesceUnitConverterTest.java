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

import java.util.Arrays;

import junit.framework.TestCase;

import org.datacleaner.api.InputColumn;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.InjectionManagerImpl;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.util.convert.StringConverter;

public class CoalesceUnitConverterTest extends TestCase {

    private final MockInputColumn<?> numberCol1 = new MockInputColumn<Number>("num1", Number.class);
    private final MockInputColumn<?> numberCol2 = new MockInputColumn<Number>("num1", Number.class);
    private final MockInputColumn<?> integerCol1 = new MockInputColumn<Integer>("int1", Integer.class);
    private final MockInputColumn<?> integerCol2 = new MockInputColumn<Integer>("int2", Integer.class);
    private final MockInputColumn<?> stringCol1 = new MockInputColumn<String>("str1", String.class);
    private final MockInputColumn<?> stringCol2 = new MockInputColumn<String>("str2", String.class);
    private final MockInputColumn<?> stringCommaCol1 = new MockInputColumn<String>("str1,a", String.class);
    private final MockInputColumn<?> stringCommaCol2 = new MockInputColumn<String>("str2,b", String.class);
    private final MockInputColumn<?> objCol1 = new MockInputColumn<Object>("obj1", Object.class);

    public void testGetOutputDataType() throws Exception {
        CoalesceUnitConverter converter = new CoalesceUnitConverter();

        InputColumn<?>[] allColumns = new InputColumn[] { numberCol1, numberCol2, integerCol1, integerCol2, stringCol1,
                stringCol2, objCol1 };

        CoalesceUnit unit1 = new CoalesceUnit(stringCol1, stringCol2);
        String str = converter.toString(unit1);
        assertEquals("[str1,str2]", str);

        CoalesceUnit unit2 = converter.fromString(CoalesceUnit.class, str);
        assertEquals("[str1, str2]", Arrays.toString(unit2.getInputColumnNames()));
        assertEquals(String.class, unit2.getOutputDataType(allColumns));
    }

    public void testDiscoverAndResolveConverter() throws Exception {
        StringConverter stringConverter = new StringConverter(new InjectionManagerImpl(
                new DataCleanerConfigurationImpl()));

        CoalesceUnit unit1 = new CoalesceUnit(stringCol1, stringCol2);
        String str = stringConverter.serialize(unit1);
        assertEquals("&#91;str1&#44;str2&#93;", str);

        CoalesceUnit[] array = new CoalesceUnit[] { unit1, unit1 };
        str = stringConverter.serialize(array);
        assertEquals("[&#91;str1&#44;str2&#93;,&#91;str1&#44;str2&#93;]", str);
        
        CoalesceUnit[] units = stringConverter.deserialize(str, CoalesceUnit[].class);
        assertEquals(2, units.length);
        assertEquals("CoalesceUnit[inputColumnNames=[str1, str2]]", units[0].toString());
        assertEquals("CoalesceUnit[inputColumnNames=[str1, str2]]", units[1].toString());
    }
    
    public void testConvertCommaNames() throws Exception {
        StringConverter stringConverter = new StringConverter(new InjectionManagerImpl(
                new DataCleanerConfigurationImpl()));

        CoalesceUnit unitIn = new CoalesceUnit(stringCommaCol1, stringCommaCol2);
        String str = stringConverter.serialize(unitIn);
        assertEquals("&#91;str1&amp;#44;a&#44;str2&amp;#44;b&#93;", str);
        
        CoalesceUnit unitOut = stringConverter.deserialize(str, CoalesceUnit.class);
        assertEquals("CoalesceUnit[inputColumnNames=[str1,a, str2,b]]", unitOut.toString());
    }
}
