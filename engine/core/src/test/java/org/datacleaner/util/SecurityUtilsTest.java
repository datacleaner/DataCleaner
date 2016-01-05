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
package org.datacleaner.util;

import junit.framework.TestCase;

public class SecurityUtilsTest extends TestCase {
    private static final String PLAIN_TEXT_VALUE = "my secret";
    private static final String ENCODED_VALUE = "yK+2geb+XAdUYlWNuHMD6A==";
    private static final String ENCODED_VALUE_WITH_PREFIX = SecurityUtils.PREFIX + ENCODED_VALUE;

    public void testNullValues() throws Exception {
        assertNull(SecurityUtils.encodePassword((char[]) null));
        assertNull(SecurityUtils.encodePassword((String) null));
        assertNull(SecurityUtils.decodePassword(null));
    }

    public void testEmptyStringValues() throws Exception {
        String encoded = SecurityUtils.encodePassword("".toCharArray());
        assertNotNull(encoded);

        String decoded = SecurityUtils.decodePassword(encoded);
        assertNotNull(decoded);
        assertEquals("", decoded);

        assertNotNull(SecurityUtils.decodePassword(""));
    }

    public void testEncodeAndDecode() throws Exception {
        String encoded = SecurityUtils.encodePassword(PLAIN_TEXT_VALUE.toCharArray());
        assertEquals(ENCODED_VALUE, encoded);

        String decoded = SecurityUtils.decodePassword(encoded);
        assertEquals(PLAIN_TEXT_VALUE, decoded);
    }

    public void testEncodeAndDecodeWithPrefix() throws Exception {
        String encodedWithPrefix = SecurityUtils.encodePasswordWithPrefix(PLAIN_TEXT_VALUE);
        assertEquals(ENCODED_VALUE_WITH_PREFIX, encodedWithPrefix);

        String decoded = SecurityUtils.decodePasswordWithPrefix(encodedWithPrefix);
        assertEquals(PLAIN_TEXT_VALUE, decoded);

        decoded = SecurityUtils.decodePasswordWithPrefix(PLAIN_TEXT_VALUE);
        assertEquals(PLAIN_TEXT_VALUE, decoded);
    }
}
