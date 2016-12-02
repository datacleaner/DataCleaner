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
package org.datacleaner.reference;

import junit.framework.TestCase;

public class RegexStringPatternTest extends TestCase {

    public void testEquals() throws Exception {
        final RegexStringPattern rsp1 = new RegexStringPattern("Danish email", "[a-z]+@[a-z]+\\.dk", true);
        RegexStringPattern rsp2 = new RegexStringPattern("Danish email", "[a-z]+@[a-z]+\\.com", true);

        assertFalse(rsp1.equals(rsp2));

        rsp2 = new RegexStringPattern("Danish email", "[a-z]+@[a-z]+\\.dk", true);
        assertTrue(rsp1.equals(rsp1));
    }

    public void testMatchesEntireString() throws Exception {
        final RegexStringPattern rsp = new RegexStringPattern("Danish email", "[a-z]+@[a-z]+\\.dk", true);
        assertEquals("Danish email", rsp.getName());

        try (StringPatternConnection rspConnection = rsp.openConnection(null)) {
            assertTrue(rspConnection.matches("kasper@eobjects.dk"));
            assertFalse(rspConnection.matches("kasper@eobjects.org"));
            assertFalse(rspConnection.matches("kasper[at]eobjects.org"));
            assertFalse(rspConnection.matches("@eobjects.dk"));
            assertFalse(rspConnection.matches(" kasper@eobjects.dk"));
            assertFalse(rspConnection.matches("kasper@eobjects.dk "));
            assertFalse(rspConnection.matches("hello kasper@eobjects.dk world"));
            assertFalse(rspConnection.matches(null));
        }

    }

    public void testMatchesNotEntireString() throws Exception {
        final RegexStringPattern rsp = new RegexStringPattern("Danish email", "[a-z]+@[a-z]+\\.dk", false);
        assertEquals("Danish email", rsp.getName());

        try (StringPatternConnection rspConnection = rsp.openConnection(null)) {
            assertTrue(rspConnection.matches("kasper@eobjects.dk"));
            assertFalse(rspConnection.matches("kasper@eobjects.org"));
            assertFalse(rspConnection.matches("kasper[at]eobjects.org"));
            assertFalse(rspConnection.matches("@eobjects.dk"));
            assertTrue(rspConnection.matches(" kasper@eobjects.dk"));
            assertTrue(rspConnection.matches("kasper@eobjects.dk "));
            assertTrue(rspConnection.matches("hello kasper@eobjects.dk world"));
            assertFalse(rspConnection.matches(null));
        }
    }
}
