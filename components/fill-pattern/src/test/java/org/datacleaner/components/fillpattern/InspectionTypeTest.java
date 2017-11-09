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
package org.datacleaner.components.fillpattern;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class InspectionTypeTest {

    @Test
    public void testNullOrFilled() {
        final InspectionType it = InspectionType.NULL_OR_FILLED;
        assertEquals("<null>", it.inspect(null));
        assertEquals("<filled>", it.inspect(1));
        assertEquals("<filled>", it.inspect("hello"));
        assertEquals("<filled>", it.inspect("  "));
        assertEquals("<filled>", it.inspect(""));
    }

    @Test
    public void testNullBlankOrFilled() {
        final InspectionType it = InspectionType.NULL_BLANK_OR_FILLED;
        assertEquals("<null>", it.inspect(null));
        assertEquals("<filled>", it.inspect(1));
        assertEquals("<filled>", it.inspect("hello"));
        assertEquals("<blank>", it.inspect("  "));
        assertEquals("<blank>", it.inspect(""));
    }

    @Test
    public void testNullOrNot() {
        final InspectionType it = InspectionType.DISTINCT_VALUES;
        assertEquals("<null>", it.inspect(null));
        assertEquals(1, it.inspect(1));
        assertEquals("hello", it.inspect("hello"));
        assertEquals("<blank>", it.inspect("  "));
        assertEquals("<blank>", it.inspect(""));
    }
}
