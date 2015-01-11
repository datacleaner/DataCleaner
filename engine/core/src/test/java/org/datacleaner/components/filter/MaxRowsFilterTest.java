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
package org.datacleaner.components.filter;

import junit.framework.TestCase;

import org.datacleaner.components.maxrows.MaxRowsFilter;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.FilterBeanDescriptor;

public class MaxRowsFilterTest extends TestCase {

	public void testDescriptor() throws Exception {
		FilterBeanDescriptor<MaxRowsFilter, MaxRowsFilter.Category> desc = Descriptors.ofFilter(MaxRowsFilter.class);

		assertEquals("Max rows", desc.getDisplayName());
	}

	public void testCounter1() throws Exception {
		MaxRowsFilter f = new MaxRowsFilter(1, 3);
		assertEquals(MaxRowsFilter.Category.VALID, f.categorize(new MockInputRow()));
		assertEquals(MaxRowsFilter.Category.VALID, f.categorize(new MockInputRow()));
		assertEquals(MaxRowsFilter.Category.VALID, f.categorize(new MockInputRow()));
		assertEquals(MaxRowsFilter.Category.INVALID, f.categorize(new MockInputRow()));
	}
	
	public void testCounter2() throws Exception {
        MaxRowsFilter f = new MaxRowsFilter(2, 3);
        assertEquals(MaxRowsFilter.Category.INVALID, f.categorize(new MockInputRow()));
        assertEquals(MaxRowsFilter.Category.VALID, f.categorize(new MockInputRow()));
        assertEquals(MaxRowsFilter.Category.VALID, f.categorize(new MockInputRow()));
        assertEquals(MaxRowsFilter.Category.VALID, f.categorize(new MockInputRow()));
        assertEquals(MaxRowsFilter.Category.INVALID, f.categorize(new MockInputRow()));
    }
}
