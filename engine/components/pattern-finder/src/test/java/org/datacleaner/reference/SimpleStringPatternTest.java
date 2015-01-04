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

public class SimpleStringPatternTest extends TestCase {

    public void testMatches() throws Exception {
        SimpleStringPattern ssp = new SimpleStringPattern("email", "aaaa@aaaa.aa");

        assertTrue(ssp.matches("kasper@eobjects.dk"));
        assertTrue(ssp.matches("kasper@eobjects.org"));
        assertFalse(ssp.matches("kasper[at]eobjects.dk"));
        assertFalse(ssp.matches("@eobjects.dk"));
        assertFalse(ssp.matches("kasper @eobjects.dk"));
        assertFalse(ssp.matches(" kasper@eobjects.dk"));
        assertFalse(ssp.matches(null));
    }

    public void testNullPattern() throws Exception {
        SimpleStringPattern ssp = new SimpleStringPattern("email", "<null>");

        assertFalse(ssp.matches("foobar"));
        assertTrue(ssp.matches(null));
        assertFalse(ssp.matches("<null>"));
        assertFalse(ssp.matches("<blank>"));
        
        assertFalse(ssp.matches(""));
        assertFalse(ssp.matches(" "));
    }
    

    public void testBlankPattern() throws Exception {
        SimpleStringPattern ssp = new SimpleStringPattern("email", "<blank>");

        assertFalse(ssp.matches("foobar"));
        assertFalse(ssp.matches(null));
        assertFalse(ssp.matches("<null>"));
        assertFalse(ssp.matches("<blank>"));
        
        assertTrue(ssp.matches(""));
        assertFalse(ssp.matches(" "));
    }
}
