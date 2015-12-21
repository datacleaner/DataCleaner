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

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

public class MapConverterTest {

    @Test
    public void testSimpleConvert() throws Exception {
        final Map<String, String> map1 = new LinkedHashMap<>();
        map1.put("foo", "bar");
        map1.put("hello", "");
        map1.put("lorem", "ipsum");
        
        final MapStringToStringConverter c = new MapStringToStringConverter();
        final String str = c.toString(map1);
        final Map<?, ?> map2 = c.fromString(Map.class, str);
        
        assertEquals(map1, map2);
        
        assertEquals("\"foo\"=\"bar\"\n" + 
                "\"hello\"=\"\"\n" + 
                "\"lorem\"=\"ipsum\"\n" + 
                "", str);
    }
    
    @Test
    public void testEscapedCharacters() throws Exception {
        final Map<String, String> map1 = new LinkedHashMap<>();
        map1.put("foo", "bar,baz=foo");
        
        final MapStringToStringConverter c = new MapStringToStringConverter();
        final String str = c.toString(map1);
        assertEquals("\"foo\"=\"bar,baz=foo\"\n", str);

        final Map<?, ?> map2 = c.fromString(Map.class, str);
        
        assertEquals(map1, map2);
    }

    @Test
    public void testHandleNullValues() throws Exception {
        final Map<String, String> map1 = new LinkedHashMap<>();
        map1.put("foo", "bar");
        map1.put("hello", null);
        map1.put("lorem", "ipsum");
        
        final MapStringToStringConverter c = new MapStringToStringConverter();
        final String str = c.toString(map1);
        final Map<?, ?> map2 = c.fromString(Map.class, str);
        
        assertEquals(map1, map2);
    }
    
    @Test
    public void testHandleEmptyMap() throws Exception {
        
    }
}
