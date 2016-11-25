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
package org.datacleaner.beans.dategap;

import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.TestCase;

public class TimeIntervalTest extends TestCase {

    public void testCompareTo() throws Exception {
        final TimeInterval ti1 = new TimeInterval(100L, 200L);
        final TimeInterval ti2 = new TimeInterval(100L, 300L);
        final TimeInterval ti3 = new TimeInterval(200L, 300L);

        assertTrue(ti1.before(ti2));
        assertTrue(ti1.before(ti3));
        assertTrue(ti2.before(ti3));

        final SortedSet<TimeInterval> sortedSet = new TreeSet<>();
        sortedSet.add(ti1);
        sortedSet.add(ti2);
        sortedSet.add(ti3);

        assertEquals("[TimeInterval[100->200], TimeInterval[100->300], TimeInterval[200->300]]", sortedSet.toString());
    }

    public void testMerge() throws Exception {
        TimeInterval interval;

        interval = TimeInterval.merge(new TimeInterval(100L, 200L), new TimeInterval(150L, 250L));
        assertEquals(100L, interval.getFrom());
        assertEquals(250L, interval.getTo());

        interval = TimeInterval.merge(new TimeInterval(200L, 220L), new TimeInterval(150L, 250L));
        assertEquals(150L, interval.getFrom());
        assertEquals(250L, interval.getTo());

        interval = TimeInterval.merge(new TimeInterval(100L, 200L), new TimeInterval(220L, 250L));
        assertEquals(100L, interval.getFrom());
        assertEquals(250L, interval.getTo());
    }

    public void testOverlapsWith() throws Exception {
        final TimeInterval ti1 = new TimeInterval(100L, 200L);
        assertTrue(ti1.overlapsWith(ti1));

        final TimeInterval ti2 = new TimeInterval(150L, 250L);
        assertTrue(ti1.overlapsWith(ti2));
        assertTrue(ti2.overlapsWith(ti1));

        final TimeInterval ti3 = new TimeInterval(250L, 300L);
        assertFalse(ti1.overlapsWith(ti3));
        assertFalse(ti3.overlapsWith(ti1));

        final TimeInterval ti4 = new TimeInterval(100L, 150L);
        assertTrue(ti1.overlapsWith(ti4));
        assertTrue(ti4.overlapsWith(ti1));
    }
}
