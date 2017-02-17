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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.datacleaner.api.InputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.junit.Test;

public class PlainSearchReplaceTransformerTest {

    private final InputColumn<String> input = new MockInputColumn<>("input");

    @Test
    public void testCaseSensitivityEntireString() throws Exception {
        final PlainSearchReplaceTransformer transformer = createTransformer();
        transformer.replacements.put("foo", "bar");
        transformer.replaceEntireString = true;
        transformer.caseSensitive = false;

        final Map<String, String> testData = new LinkedHashMap<>();
        testData.put("this is foo value", "[bar]");
        testData.put("This Is Foo Value", "[bar]");
        testData.put("THIS IS FOO VALUE", "[bar]");

        assertExpectedValues(transformer, testData);
    }

    @Test
    public void testCaseSensitivity() throws Exception {
        final PlainSearchReplaceTransformer transformer = createTransformer();
        transformer.replacements.put("foo", "bar");
        transformer.caseSensitive = false;

        final Map<String, String> testData = new LinkedHashMap<>();
        testData.put("foo", "[bar]");
        testData.put("Foo", "[bar]");
        testData.put("FOO", "[bar]");

        assertExpectedValues(transformer, testData);
    }

    @Test
    public void testNullAndEmptyValues() throws Exception {
        final PlainSearchReplaceTransformer transformer = createTransformer();
        transformer.replacements.put("foo", "hello");

        final Map<String, String> testData = new LinkedHashMap<>();
        testData.put("", "[]");
        testData.put(null, "[null]");

        assertExpectedValues(transformer, testData);
    }

    @Test
    public void testVanillaReplacements() throws Exception {
        final PlainSearchReplaceTransformer transformer = createTransformer();
        transformer.replacements.put("foo", "hello");
        transformer.replacements.put("bar", "world");

        final Map<String, String> testData = new LinkedHashMap<>();
        testData.put("foo bar", "[hello world]");
        testData.put("hello foo world foo", "[hello hello world hello]");
        testData.put("hello world", "[hello world]");

        assertExpectedValues(transformer, testData);
    }

    @Test
    public void testReplacementEntireString() throws Exception {
        final PlainSearchReplaceTransformer transformer = createTransformer();
        transformer.setReplaceEntireString(true);
        transformer.replacements.put("foo", "hello");
        transformer.replacements.put("bar", "world");

        final Map<String, String> testData = new LinkedHashMap<>();
        testData.put("foo bar", "[hello]");
        testData.put("hello bar world foo", "[hello]");
        testData.put("hello world", "[hello world]");

        assertExpectedValues(transformer, testData);
    }

    @Test
    public void testReplacementOnReplacement() throws Exception {
        final PlainSearchReplaceTransformer transformer = createTransformer();
        transformer.replacements.put("foo", "bar bar bar");
        transformer.replacements.put("bar", "world");

        final Map<String, String> testData = new LinkedHashMap<>();
        testData.put("foo bar", "[world world world world]");

        assertExpectedValues(transformer, testData);
    }

    @Test
    public void testEmptyReplacements() throws Exception {
        final PlainSearchReplaceTransformer transformer = createTransformer();
        transformer.replacements.put("foo", "");

        final Map<String, String> testData = new LinkedHashMap<>();
        testData.put("foo bar", "[ bar]");

        assertExpectedValues(transformer, testData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptySearchKey() throws Exception {
        final PlainSearchReplaceTransformer transformer = createTransformer();
        transformer.replacements.put("", "foo");
        transformer.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRecursiveReplacement() throws Exception {
        final PlainSearchReplaceTransformer transformer = createTransformer();
        transformer.replacements.put("foo", "foo bar foo");
        transformer.validate();
    }

    private PlainSearchReplaceTransformer createTransformer() {
        final PlainSearchReplaceTransformer transformer = new PlainSearchReplaceTransformer();
        transformer.valueColumn = input;
        transformer.replaceEntireString = false;
        transformer.replacements = new LinkedHashMap<>();

        return transformer;
    }

    private void assertExpectedValues(final PlainSearchReplaceTransformer transformer,
            final Map<String, String> testData) {
        transformer.validate();

        for (final String key : testData.keySet()) {
            assertEquals(testData.get(key), Arrays.toString(transformer.transform(new MockInputRow().put(input, key))));
        }
    }
}
