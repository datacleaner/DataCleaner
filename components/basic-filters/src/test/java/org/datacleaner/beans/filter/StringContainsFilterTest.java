/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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
package org.datacleaner.beans.filter;

import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;

import junit.framework.TestCase;

public class StringContainsFilterTest extends TestCase {

    public void testFilter() throws Exception {
        final MockInputColumn<String> column = new MockInputColumn<>("col", String.class);
        final StringContainsFilter filter = new StringContainsFilter();
        filter.column = column;
        filter.substrings = new String[] { "hello", "World" };

        assertEquals(StringContainsFilter.Category.MATCHED, filter.categorize(new MockInputRow().put(column, "Hi world")));
        assertEquals(StringContainsFilter.Category.MATCHED, filter.categorize(new MockInputRow().put(column, "Hello DC")));
        assertEquals(StringContainsFilter.Category.UNMATCHED, filter.categorize(new MockInputRow().put(column, "Yikes!")));
        assertEquals(StringContainsFilter.Category.UNMATCHED, filter.categorize(new MockInputRow().put(column, "")));
        assertEquals(StringContainsFilter.Category.UNMATCHED, filter.categorize(new MockInputRow().put(column, "   ")));
        assertEquals(StringContainsFilter.Category.UNMATCHED, filter.categorize(new MockInputRow().put(column, null)));
    }
}
