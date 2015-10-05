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
package org.datacleaner.beans.writers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;
import org.junit.Test;

public class TypeConverterTest {

    private final Date dateValue = createDate();

    private Date createDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            return sdf.parse("21/12/2012");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Test
    public void shouldReturnAStringTypeForLiteralColumn() {
        Column literalColumn = new MutableColumn("foo", ColumnType.CHAR);

        Object result = TypeConverter.convertType("bla", literalColumn);
        assertTrue(result instanceof String);
        assertEquals("bla", result);

        result = TypeConverter.convertType(24, literalColumn);
        assertTrue(result instanceof String);
        assertEquals("24", result);

        result = TypeConverter.convertType(24.3d, literalColumn);
        assertTrue(result instanceof String);
        assertEquals("24.3", result);

        result = TypeConverter.convertType(dateValue, literalColumn);
        assertTrue(result instanceof String);
        assertEquals(dateValue.toString(), result);

    }

    @Test
    public void shouldReturnNumberForNumberColumn() {
        Column numberColumn = new MutableColumn("foo", ColumnType.NUMERIC);

        Object result = TypeConverter.convertType(24, numberColumn);
        assertTrue(result instanceof Number);
        assertEquals(24, result);

        result = TypeConverter.convertType(24.3d, numberColumn);
        assertTrue(result instanceof Number);
        assertEquals(24.3, result);

        result = TypeConverter.convertType("33", numberColumn);
        assertTrue(result instanceof Number);
        assertEquals(33l, result);

        result = TypeConverter.convertType("33.3", numberColumn);
        assertTrue(result instanceof Number);
        assertEquals(33.3, result);

        result = TypeConverter.convertType(true, numberColumn);
        assertTrue(result instanceof Number);
        assertEquals(1, result);

        result = TypeConverter.convertType(false, numberColumn);
        assertTrue(result instanceof Number);
        assertEquals(0, result);

    }

    @Test
    public void shouldReturnBooleanForBooleanColumn() {
        Column booleanColumn = new MutableColumn("foo", ColumnType.BOOLEAN);

        Object result = TypeConverter.convertType(true, booleanColumn);
        assertTrue(result instanceof Boolean);
        assertEquals(true, result);

        result = TypeConverter.convertType(false, booleanColumn);
        assertTrue(result instanceof Boolean);
        assertEquals(false, result);

    }

    @Test
    public void shouldNotConvertBooleanForLiteralColumn() {
        Column literalColumn = new MutableColumn("foo", ColumnType.CHAR);

        Object result = TypeConverter.convertType(false, literalColumn);
        assertTrue(result instanceof Boolean);
        assertEquals(false, result);

    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowForStringAndNumberColumn() {
        Column numberColumn = new MutableColumn("foo", ColumnType.NUMERIC);
        TypeConverter.convertType("bla", numberColumn);
    }

    public void shouldThrowForDataAndNumberColumn() {
        Column numberColumn = new MutableColumn("foo", ColumnType.NUMERIC);
        TypeConverter.convertType(dateValue, numberColumn);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowForIntegerAndBooleanColumn() {
        Column booleanColumn = new MutableColumn("foo", ColumnType.BOOLEAN);
        TypeConverter.convertType(42, booleanColumn);

    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowForDoubleAndBooleanColumn() {
        Column booleanColumn = new MutableColumn("foo", ColumnType.BOOLEAN);
        TypeConverter.convertType(42.3d, booleanColumn);

    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowForStringAndBooleanColumn() {
        Column booleanColumn = new MutableColumn("foo", ColumnType.BOOLEAN);
        TypeConverter.convertType("bla", booleanColumn);

    }

    public void shouldThrowForDataAndBooleanColumn() {
        Column booleanColumn = new MutableColumn("foo", ColumnType.BOOLEAN);
        TypeConverter.convertType(dateValue, booleanColumn);
    }

}
