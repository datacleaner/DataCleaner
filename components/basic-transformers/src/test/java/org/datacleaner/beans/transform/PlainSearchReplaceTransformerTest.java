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

import org.datacleaner.api.InputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.junit.Test;

public class PlainSearchReplaceTransformerTest {

    private final InputColumn<String> input = new MockInputColumn<>("input");

    @Test
    public void testNullAndEmptyValues() throws Exception {
        PlainSearchReplaceTransformer t = new PlainSearchReplaceTransformer();
        t.valueColumn = input;
        t.replaceEntireString = false;
        t.replacements = new LinkedHashMap<String, String>();
        t.replacements.put("foo", "hello");
        t.validate();

        assertEquals("[]", Arrays.toString(t.transform(new MockInputRow().put(input, ""))));
        assertEquals("[null]", Arrays.toString(t.transform(new MockInputRow().put(input, null))));
    }

    @Test
    public void testVanillaReplacements() throws Exception {
        PlainSearchReplaceTransformer t = new PlainSearchReplaceTransformer();
        t.valueColumn = input;
        t.replaceEntireString = false;
        t.replacements = new LinkedHashMap<String, String>();
        t.replacements.put("foo", "hello");
        t.replacements.put("bar", "world");
        t.validate();

        assertEquals("[hello world]", Arrays.toString(t.transform(new MockInputRow().put(input, "foo bar"))));
        assertEquals("[hello hello world hello]", Arrays.toString(t.transform(new MockInputRow().put(input,
                "hello foo world foo"))));
        assertEquals("[hello world]", Arrays.toString(t.transform(new MockInputRow().put(input, "hello world"))));
    }

    @Test
    public void testReplacementEntireString() throws Exception {
        PlainSearchReplaceTransformer t = new PlainSearchReplaceTransformer();
        t.valueColumn = input;
        t.replaceEntireString = true;
        t.replacements = new LinkedHashMap<String, String>();
        t.replacements.put("foo", "hello");
        t.replacements.put("bar", "world");
        t.validate();

        assertEquals("[hello]", Arrays.toString(t.transform(new MockInputRow().put(input, "foo bar"))));
        assertEquals("[hello]", Arrays.toString(t.transform(new MockInputRow().put(input, "hello bar world foo"))));
        assertEquals("[hello world]", Arrays.toString(t.transform(new MockInputRow().put(input, "hello world"))));
    }

    @Test
    public void testReplacementOnReplacement() throws Exception {
        PlainSearchReplaceTransformer t = new PlainSearchReplaceTransformer();
        t.valueColumn = input;
        t.replaceEntireString = false;
        t.replacements = new LinkedHashMap<String, String>();
        t.replacements.put("foo", "bar bar bar");
        t.replacements.put("bar", "world");
        t.validate();

        assertEquals("[world world world world]", Arrays.toString(t.transform(new MockInputRow().put(input,
                "foo bar"))));
    }

    @Test
    public void testEmptyReplacements() throws Exception {
        PlainSearchReplaceTransformer t = new PlainSearchReplaceTransformer();
        t.valueColumn = input;
        t.replaceEntireString = false;
        t.replacements = new LinkedHashMap<String, String>();
        t.replacements.put("foo", "");
        t.validate();

        assertEquals("[ bar]", Arrays.toString(t.transform(new MockInputRow().put(input, "foo bar"))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptySearchKey() throws Exception {
        PlainSearchReplaceTransformer t = new PlainSearchReplaceTransformer();
        t.valueColumn = input;
        t.replaceEntireString = false;
        t.replacements = new LinkedHashMap<String, String>();
        t.replacements.put("", "foo");
        t.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRecursiveReplacement() throws Exception {
        PlainSearchReplaceTransformer t = new PlainSearchReplaceTransformer();
        t.valueColumn = input;
        t.replaceEntireString = false;
        t.replacements = new LinkedHashMap<String, String>();
        t.replacements.put("foo", "foo bar foo");
        t.validate();
    }
}
