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
package org.eobjects.analyzer.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.util.ReflectionUtilTestHelpClass.ClassA;
import org.eobjects.analyzer.util.ReflectionUtilTestHelpClass.ClassB;
import org.eobjects.analyzer.util.ReflectionUtilTestHelpClass.ClassC;

public class ReflectionUtilsTest extends TestCase {

    public InputColumn<String> stringInputColumn;

    @SuppressWarnings("rawtypes")
    public InputColumn rawInputColumn;

    public InputColumn<?> unspecifiedInputColumn;

    public InputColumn<? extends Number> unspecifiedNumberInputColumn;

    public InputColumn<String>[] stringInputColumns;

    public InputColumn<? super Number>[] unspecifiedNumberSuperclassInputColumns;

    public InputColumn<Comparable<String>> stringComparableInputColumn;

    public void testExplodeCamelCase() throws Exception {
        assertEquals("Foo bar", ReflectionUtils.explodeCamelCase("fooBar", false));
        assertEquals("f", ReflectionUtils.explodeCamelCase("f", false));
        assertEquals("", ReflectionUtils.explodeCamelCase("", false));
        assertEquals("My name is john doe", ReflectionUtils.explodeCamelCase("MyNameIsJohnDoe", false));
        assertEquals("H e l l o", ReflectionUtils.explodeCamelCase("h e l l o", false));

        assertEquals("Name", ReflectionUtils.explodeCamelCase("getName", true));
    }

    public void testInputColumnType() throws Exception {
        Field field = getClass().getField("stringInputColumn");
        assertEquals(1, ReflectionUtils.getTypeParameterCount(field));
        assertEquals(String.class, ReflectionUtils.getTypeParameter(field, 0));

        field = getClass().getField("rawInputColumn");
        assertEquals(0, ReflectionUtils.getTypeParameterCount(field));

        field = getClass().getField("unspecifiedNumberInputColumn");
        assertEquals(1, ReflectionUtils.getTypeParameterCount(field));
        assertEquals(Number.class, ReflectionUtils.getTypeParameter(field, 0));

        field = getClass().getField("stringInputColumns");
        assertEquals(1, ReflectionUtils.getTypeParameterCount(field));
        assertEquals(String.class, ReflectionUtils.getTypeParameter(field, 0));
        assertTrue(field.getType().isArray());

        field = getClass().getField("unspecifiedNumberSuperclassInputColumns");
        assertEquals(1, ReflectionUtils.getTypeParameterCount(field));
        assertEquals(Object.class, ReflectionUtils.getTypeParameter(field, 0));
        assertTrue(field.getType().isArray());

        field = getClass().getField("stringComparableInputColumn");
        assertEquals(1, ReflectionUtils.getTypeParameterCount(field));
        assertEquals(Comparable.class, ReflectionUtils.getTypeParameter(field, 0));
    }

    public void testIsNumber() throws Exception {
        assertTrue(ReflectionUtils.isNumber(Long.class));
        assertTrue(ReflectionUtils.isNumber(Float.class));
        assertFalse(ReflectionUtils.isNumber(String.class));
        assertFalse(ReflectionUtils.isNumber(Object.class));

        assertTrue(ReflectionUtils.isNumber(byte.class));
        assertTrue(ReflectionUtils.isNumber(short.class));
        assertTrue(ReflectionUtils.isNumber(int.class));
        assertTrue(ReflectionUtils.isNumber(long.class));
        assertTrue(ReflectionUtils.isNumber(float.class));
        assertTrue(ReflectionUtils.isNumber(double.class));
        assertFalse(ReflectionUtils.isNumber(double[].class));
        assertFalse(ReflectionUtils.isNumber(Double[].class));
    }

    public void testGetHierarchyDistance() throws Exception {
        assertEquals(0, ReflectionUtils.getHierarchyDistance(String.class, String.class));
        assertEquals(1, ReflectionUtils.getHierarchyDistance(String.class, CharSequence.class));
        assertEquals(1, ReflectionUtils.getHierarchyDistance(String.class, Object.class));
        assertEquals(1, ReflectionUtils.getHierarchyDistance(Number.class, Object.class));
        assertEquals(2, ReflectionUtils.getHierarchyDistance(Integer.class, Object.class));
        assertEquals(1, ReflectionUtils.getHierarchyDistance(Integer.class, Number.class));

        assertEquals(1, ReflectionUtils.getHierarchyDistance(CrosstabResult.class, AnalyzerResult.class));
        assertEquals(1, ReflectionUtils.getHierarchyDistance(AnnotatedRowsResult.class, AnalyzerResult.class));
    }

    public void testGetFields() throws Exception {
        Field[] fields = ReflectionUtils.getFields(ClassA.class);
        assertEquals(1, fields.length);

        assertEquals(ClassA.class, fields[0].getDeclaringClass());
        assertEquals("a", fields[0].getName());

        fields = ReflectionUtils.getFields(ClassB.class);
        assertEquals(2, fields.length);

        assertEquals(ClassB.class, fields[0].getDeclaringClass());
        assertEquals("b", fields[0].getName());
        assertEquals(ClassA.class, fields[1].getDeclaringClass());
        assertEquals("a", fields[1].getName());
    }

    public void testIsArrayAnObject() throws Exception {
        assertTrue(ReflectionUtils.is(byte[].class, Object.class));
    }

    public void testIsPrimitiveByteAnObject() throws Exception {
        assertFalse(ReflectionUtils.is(byte.class, Object.class));
    }

    public void testIsByteWrapperAnObject() throws Exception {
        assertTrue(ReflectionUtils.is(Byte.class, Object.class));
    }

    public void testGetMethods() throws Exception {
        final Method[] methods1 = ReflectionUtils.getMethods(ClassA.class);
        assertEquals(1, methods1.length);
        assertEquals("getA", methods1[0].getName());

        final Method[] methods2 = ReflectionUtils.getMethods(ClassB.class);

        if (ReflectionUtils.isGetMethodsLegacyApproach()) {
            assertEquals(3, methods2.length);
        } else {
            assertEquals(2, methods2.length);
        }

        for (Method method : methods2) {
            switch (method.getName()) {
            case "getA":
            case "getB":
                // ok, no problem
                break;
            default:
                fail("Unexpected method: " + method);
            }
        }

        final Method[] methods3 = ReflectionUtils.getMethods(ClassC.class);
        assertEquals(2, methods3.length);
        for (Method method : methods3) {
            switch (method.getName()) {
            case "getA":
            case "getC":
                // ok, no problem
                break;
            default:
                fail("Unexpected method: " + method);
            }
        }
    }
}
