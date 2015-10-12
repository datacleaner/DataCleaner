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

import org.apache.metamodel.query.Query;
import org.datacleaner.components.maxrows.MaxRowsFilter;
import org.datacleaner.components.maxrows.MaxRowsFilter.Category;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.FilterDescriptor;

public class MaxRowsFilterTest extends TestCase {

    public void testDescriptor() throws Exception {
        final FilterDescriptor<MaxRowsFilter, MaxRowsFilter.Category> desc = Descriptors.ofFilter(MaxRowsFilter.class);

        assertEquals("Max rows", desc.getDisplayName());
    }

    public void testCounter1() throws Exception {
        final MaxRowsFilter f = new MaxRowsFilter(1, 3);
        assertEquals(MaxRowsFilter.Category.VALID, f.categorize(new MockInputRow()));
        assertEquals(MaxRowsFilter.Category.VALID, f.categorize(new MockInputRow()));
        assertEquals(MaxRowsFilter.Category.VALID, f.categorize(new MockInputRow()));
        assertEquals(MaxRowsFilter.Category.INVALID, f.categorize(new MockInputRow()));
    }

    public void testCounter2() throws Exception {
        final MaxRowsFilter f = new MaxRowsFilter(2, 3);
        assertEquals(MaxRowsFilter.Category.INVALID, f.categorize(new MockInputRow()));
        assertEquals(MaxRowsFilter.Category.VALID, f.categorize(new MockInputRow()));
        assertEquals(MaxRowsFilter.Category.VALID, f.categorize(new MockInputRow()));
        assertEquals(MaxRowsFilter.Category.VALID, f.categorize(new MockInputRow()));
        assertEquals(MaxRowsFilter.Category.INVALID, f.categorize(new MockInputRow()));
    }

    public void testOptimizeTwiceSubset() throws Exception {
        // offset 10, limit 20 (so rec. 10-30)
        final MaxRowsFilter f1 = new MaxRowsFilter(10, 20);

        // offset 15, limit 1 (so rec. 15-16, which becomes then 25-16)
        final MaxRowsFilter f2 = new MaxRowsFilter(15, 1);

        final Query q = new Query();
        f1.optimizeQuery(q, Category.VALID);
        f2.optimizeQuery(q, Category.VALID);
        assertEquals(25, q.getFirstRow().intValue());
        assertEquals(1, q.getMaxRows().intValue());
    }

    public void testOptimizeTwiceOverlappingIntervals() throws Exception {
        // offset 10, limit 20 (so rec. 10-30)
        final MaxRowsFilter f1 = new MaxRowsFilter(10, 20);

        // offset 15, limit 30 (so rec. 15-45, which becomes then 25-45, but
        // since 45 is beyond the limit of the first filter, it should finally
        // become 25-30)
        final MaxRowsFilter f2 = new MaxRowsFilter(15, 30);

        final Query q = new Query();
        f1.optimizeQuery(q, Category.VALID);
        f2.optimizeQuery(q, Category.VALID);
        assertEquals(25, q.getFirstRow().intValue());
        assertEquals(5, q.getMaxRows().intValue());
    }

    public void testOptimizeTwiceImpossibleIntervals() throws Exception {
        // offset 10, limit 20 (so rec. 10-30)
        final MaxRowsFilter f1 = new MaxRowsFilter(10, 20);

        // offset 45, limit 30 (so rec. 45-75, which is a totally different
        // range than f1's range)
        final MaxRowsFilter f2 = new MaxRowsFilter(45, 30);

        final Query q = new Query();
        f1.optimizeQuery(q, Category.VALID);
        f2.optimizeQuery(q, Category.VALID);
        assertEquals(55, q.getFirstRow().intValue());
        assertEquals(0, q.getMaxRows().intValue());
    }
}
