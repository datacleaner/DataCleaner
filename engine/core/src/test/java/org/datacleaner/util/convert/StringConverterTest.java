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
package org.datacleaner.util.convert;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.datacleaner.beans.filter.MaxRowsFilter;
import org.datacleaner.beans.transform.TableLookupTransformer;
import org.datacleaner.configuration.AnalyzerBeansConfigurationImpl;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.ReferenceDataCatalogImpl;
import org.datacleaner.reference.SimpleDictionary;
import org.datacleaner.reference.SimpleSynonymCatalog;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.reference.SynonymCatalog;
import org.datacleaner.util.Percentage;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.convert.MyConvertable.SecondaryConverter;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.MutableSchema;
import org.apache.metamodel.schema.MutableTable;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.util.EqualsBuilder;

public class StringConverterTest extends TestCase {

    private final Dictionary dictionary = new SimpleDictionary("my dict");
    private final SynonymCatalog synonymCatalog = new SimpleSynonymCatalog("my synonyms");
    private final ReferenceDataCatalogImpl referenceDataCatalog = new ReferenceDataCatalogImpl(Arrays.asList(dictionary),
            Arrays.asList(synonymCatalog), new ArrayList<StringPattern>());

    private StringConverter stringConverter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        AnalyzerBeansConfigurationImpl conf = new AnalyzerBeansConfigurationImpl().replace(referenceDataCatalog);

        stringConverter = new StringConverter(conf.getInjectionManager(null));
    }

    public void testConvertConvertableType() throws Exception {
        MyConvertable convertable = new MyConvertable();
        convertable.setName("foo");
        convertable.setDescription("bar");

        String serializedForm1 = stringConverter.serialize(convertable);
        assertEquals("foo:bar", serializedForm1);
        String serializedForm2 = stringConverter.serialize(convertable, SecondaryConverter.class);
        assertEquals("foo|bar", serializedForm2);

        {
            MyConvertable copy1 = stringConverter.deserialize(serializedForm1, MyConvertable.class);
            assertTrue(convertable != copy1);
            assertEquals("foo", copy1.getName());
            assertEquals("bar", copy1.getDescription());
        }

        {
            MyConvertable copy2 = stringConverter.deserialize(serializedForm2, MyConvertable.class, SecondaryConverter.class);
            assertTrue(convertable != copy2);
            assertEquals("foo", copy2.getName());
            assertEquals("bar", copy2.getDescription());
        }
    }

    public void testConvertSimpleTypes() throws Exception {
        runTests("hello, [world]", "hello&#44; &#91;world&#93;");
        runTests("hello", "hello");
        runTests(1337, "1337");
        runTests(12l, "12");
        runTests('a', "a");
        runTests(true, "true");
        runTests(false, "false");
        runTests((short) 12, "12");
        runTests((byte) 12, "12");
        runTests(String.class, "java.lang.String");
        runTests(1337.0, "1337.0");
        runTests(1337.0f, "1337.0");

        // this is needed to make sure the unittest is runnable in all locales.
        TimeZone timeZone = TimeZone.getDefault();
        int localeOffset = timeZone.getRawOffset();

        runTests(new Date(1234 - localeOffset), "1970-01-01T00:00:01 234");
        runTests(Calendar.getInstance(), null);
        runTests(new java.sql.Date(1234 - localeOffset), "1970-01-01T00:00:01 234");
    }

    public static class MySerializable implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String myString;
        private final int myInt;

        public MySerializable(String myString, int myInt) {
            this.myString = myString;
            this.myInt = myInt;
        }

        public int getMyInt() {
            return myInt;
        }

        public String getMyString() {
            return myString;
        }
    }

    public void testConvertList() throws Exception {
        List<?> list = stringConverter.deserialize("[foo,bar]", List.class);
        assertEquals(2, list.size());
        assertEquals(String.class, list.get(0).getClass());
        assertEquals(String.class, list.get(1).getClass());
    }

    public void testConvertExpressionDates() throws Exception {
        runTests(new TodayDate(), "today()");
        runTests(new NowDate(), "now()");
        runTests(new YesterdayDate(), "yesterday()");
    }

    public void testConvertSerializable() throws Exception {
        String serialized = stringConverter.serialize(new MySerializable("foobar", 1337));
        assertEquals(
                "&#91;-84&#44;-19&#44;0&#44;5&#44;115&#44;114&#44;0&#44;63&#44;111&#44;114&#44;103&#44;46&#44;100&#44;97&#44;116&#44;97&#44;99&#44;108&#44;101&#44;97&#44;110&#44;101&#44;114&#44;46&#44;117&#44;116&#44;105&#44;108&#44;46&#44;99&#44;111&#44;110&#44;118&#44;101&#44;114&#44;116&#44;46&#44;83&#44;116&#44;114&#44;105&#44;110&#44;103&#44;67&#44;111&#44;110&#44;118&#44;101&#44;114&#44;116&#44;101&#44;114&#44;84&#44;101&#44;115&#44;116&#44;36&#44;77&#44;121&#44;83&#44;101&#44;114&#44;105&#44;97&#44;108&#44;105&#44;122&#44;97&#44;98&#44;108&#44;101&#44;0&#44;0&#44;0&#44;0&#44;0&#44;0&#44;0&#44;1&#44;2&#44;0&#44;2&#44;73&#44;0&#44;5&#44;109&#44;121&#44;73&#44;110&#44;116&#44;76&#44;0&#44;8&#44;109&#44;121&#44;83&#44;116&#44;114&#44;105&#44;110&#44;103&#44;116&#44;0&#44;18&#44;76&#44;106&#44;97&#44;118&#44;97&#44;47&#44;108&#44;97&#44;110&#44;103&#44;47&#44;83&#44;116&#44;114&#44;105&#44;110&#44;103&#44;59&#44;120&#44;112&#44;0&#44;0&#44;5&#44;57&#44;116&#44;0&#44;6&#44;102&#44;111&#44;111&#44;98&#44;97&#44;114&#93;",
                serialized);

        MySerializable deserialized = stringConverter.deserialize(serialized, MySerializable.class);
        assertEquals("foobar", deserialized.getMyString());
        assertEquals(1337, deserialized.getMyInt());
    }

    public void testAbstractNumber() throws Exception {
        Number n = stringConverter.deserialize("1", Number.class);
        assertTrue(n instanceof Long);
        assertEquals(1, n.intValue());

        n = stringConverter.deserialize("1.01", Number.class);
        assertTrue(n instanceof Double);
        assertEquals(1.01, n.doubleValue());
        assertEquals(1, n.intValue());
    }

    public void testEnum() throws Exception {
        String serialized = stringConverter.serialize(MaxRowsFilter.Category.VALID);
        assertEquals("VALID", serialized);

        Object deserialized = stringConverter.deserialize(serialized, MaxRowsFilter.Category.class);
        assertEquals(MaxRowsFilter.Category.VALID, deserialized);

        MaxRowsFilter.Category[] array = new MaxRowsFilter.Category[] { MaxRowsFilter.Category.VALID,
                MaxRowsFilter.Category.INVALID };
        serialized = stringConverter.serialize(array);
        assertEquals("[VALID,INVALID]", serialized);

        deserialized = stringConverter.deserialize(serialized, MaxRowsFilter.Category[].class);
        assertTrue(EqualsBuilder.equals(array, deserialized));
    }

    public void testEnumWithAlias() throws Exception {
        // TableLookupTransformer has multiple aliased enum values

        Object deserialized = stringConverter.deserialize("LEFT", TableLookupTransformer.JoinSemantic.class);
        assertEquals(TableLookupTransformer.JoinSemantic.LEFT_JOIN_MAX_ONE, deserialized);
    }

    public void testFile() throws Exception {
        File file1 = new File("pom.xml");
        File fileAbs = file1.getAbsoluteFile();
        File dir1 = new File("src");

        String serialized = stringConverter.serialize(file1);
        assertEquals("pom.xml", serialized);

        Object deserialized = stringConverter.deserialize(serialized, File.class);
        assertTrue(EqualsBuilder.equals(file1, deserialized));

        serialized = stringConverter.serialize(fileAbs);
        assertEquals(fileAbs.getAbsolutePath(), new File(serialized).getAbsolutePath());

        File[] arr = new File[] { file1, dir1 };

        serialized = stringConverter.serialize(arr);
        assertEquals("[pom.xml,src]", serialized);

        deserialized = stringConverter.deserialize(serialized, File[].class);
        assertTrue(EqualsBuilder.equals(arr, deserialized));
    }

    public void testReferenceDataSerialization() throws Exception {
        assertEquals("my dict", stringConverter.serialize(dictionary));
        assertEquals("my synonyms", stringConverter.serialize(synonymCatalog));

        Dictionary dictionaryResult = stringConverter.deserialize("my dict", Dictionary.class);
        assertSame(dictionaryResult, dictionary);

        try {
            stringConverter.deserialize("foo", Dictionary.class);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
            assertEquals("Dictionary not found: foo", e.getMessage());
        }

        SynonymCatalog synonymCatalogResult = stringConverter.deserialize("my synonyms", SynonymCatalog.class);
        assertSame(synonymCatalogResult, synonymCatalog);
        try {
            stringConverter.deserialize("bar", SynonymCatalog.class);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
            assertEquals("Synonym catalog not found: bar", e.getMessage());
        }
    }

    public void testSerializeUnknownTypes() throws Exception {
        String result = stringConverter.serialize(new Percentage(50));
        assertEquals("50%", result);
    }

    public void testSerializeSchemaElements() throws Exception {
        Schema schema = new MutableSchema("s1");
        assertEquals("s1", stringConverter.serialize(schema));

        MutableTable table = new MutableTable("t1");
        table.setSchema(schema);
        assertEquals("s1.t1", stringConverter.serialize(table));

        MutableColumn column = new MutableColumn("c1");
        column.setTable(table);
        assertEquals("s1.t1.c1", stringConverter.serialize(column));
    }

    public void testNullArgument() throws Exception {
        String s = stringConverter.serialize(null);
        assertEquals("<null>", s);
        assertNull(stringConverter.deserialize(s, String.class));
        assertNull(stringConverter.deserialize(s, Integer.class));
        assertNull(stringConverter.deserialize(s, Date.class));
    }

    public void testArrays() throws Exception {
        runTests(new String[] { "hello,world" }, "[hello&#44;world]");
        runTests(new String[] { "hello", "world" }, "[hello,world]");
        runTests(new String[] { "hello", "[world]" }, "[hello,&#91;world&#93;]");
        runTests(new String[] { "hello, there", "[world]" }, null);
        runTests(new String[] { "hello, there [y0!]", "w00p" }, null);
        runTests(new Double[] { 123.4, 567.8 }, "[123.4,567.8]");
        runTests(new String[0], "[]");
        runTests(new String[3], "[<null>,<null>,<null>]");

        Long[] result = stringConverter.deserialize("123", Long[].class);
        assertEquals(1, result.length);
        assertEquals(123l, result[0].longValue());
    }

    public void testDoubleSidedArray() throws Exception {
        runTests(new String[][] { { "hello", "world" }, { "hi", "there" } }, "[[hello,world],[hi,there]]");
        runTests(new String[][] { { "hello", "world" }, { "howdy" }, { "hi", "there partner", "yiiioowy" } },
                "[[hello,world],[howdy],[hi,there partner,yiiioowy]]");
        runTests(new String[][] { { "hello", "world" }, { "howdy" }, { "hi", "there partner", "yiiioowy" } },
                "[[hello,world],[howdy],[hi,there partner,yiiioowy]]");
    }

    public void testDeepArray() throws Exception {
        runTests(new Integer[][][][] { { { { 1, 2 }, { 3, 4 } }, { { 5, 6 } } } }, "[[[[1,2],[3,4]],[[5,6]]]]");
    }

    private void runTests(final Object o, String expectedStringRepresentation) {
        String s = stringConverter.serialize(o);
        if (expectedStringRepresentation != null) {
            assertEquals(expectedStringRepresentation, s);
        }
        Object o2 = stringConverter.deserialize(s, o.getClass());
        if (ReflectionUtils.isArray(o)) {
            boolean equals = EqualsBuilder.equals(o, o2);
            if (!equals) {
                StringBuilder sb = new StringBuilder();
                sb.append("Not equals!");
                sb.append("\n expected: " + o + ": " + Arrays.toString((Object[]) o));
                sb.append("\n actual:   " + o2 + ": " + Arrays.toString((Object[]) o2));
                fail(sb.toString());
            }
        } else {
            assertEquals(o, o2);
        }
    }

    public void testSerializeList() throws Exception {
        ArrayList<Object> o = new ArrayList<Object>();
        o.add("foo");
        o.add("bar");
        o.add(Arrays.asList("baz", "foobar"));

        StringConverter converter = new StringConverter(null);
        String result = converter.serialize(o);
        assertEquals("[foo,bar,[baz,foobar]]", result);
    }
}
