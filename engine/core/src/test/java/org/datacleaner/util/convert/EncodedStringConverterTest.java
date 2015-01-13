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

import junit.framework.TestCase;

public class EncodedStringConverterTest extends TestCase {

     public void testFromAndToString() throws Exception {
        EncodedStringConverter converter = new EncodedStringConverter();
        String encoded = converter.toString("My secret 1234");
        assertEquals("xWP8CYUpivVDX6l31xBvdg==", encoded);
        
        String result = converter.fromString(String.class, encoded);
        assertEquals("My secret 1234", result);
    }
     
     public void testFromAndToNull() throws Exception {
         EncodedStringConverter converter = new EncodedStringConverter();
         String encoded = converter.toString(null);
         assertEquals(null, encoded);
         
         String result = converter.fromString(String.class, null);
         assertEquals(null, result);
    }
}
