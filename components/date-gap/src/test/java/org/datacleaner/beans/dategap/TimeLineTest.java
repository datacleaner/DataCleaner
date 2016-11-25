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

import junit.framework.TestCase;

public class TimeLineTest extends TestCase {

    public void testGetFirstAndLast() throws Exception {
        final TimeLine timeline = new TimeLine();

        timeline.addInterval(new TimeInterval(500L, 600L));
        timeline.addInterval(new TimeInterval(600L, 700L));
        timeline.addInterval(new TimeInterval(100L, 200L));
        timeline.addInterval(new TimeInterval(700L, 800L));
        timeline.addInterval(new TimeInterval(200L, 300L));

        assertEquals(100L, timeline.getFrom().getTime());
        assertEquals(800L, timeline.getTo().getTime());
    }

    public void testGetFlattenedIntervals() throws Exception {
        final TimeLine timeline = new TimeLine();
        SortedSet<TimeInterval> result;

        result = timeline.getFlattenedIntervals();
        assertEquals("[]", result.toString());

        timeline.addInterval(new TimeInterval(500L, 600L));
        timeline.addInterval(new TimeInterval(600L, 700L));
        timeline.addInterval(new TimeInterval(100L, 200L));
        timeline.addInterval(new TimeInterval(700L, 800L));
        timeline.addInterval(new TimeInterval(200L, 300L));
        timeline.addInterval(new TimeInterval(250L, 280L));

        result = timeline.getFlattenedIntervals();
        assertEquals("[TimeInterval[100->300], TimeInterval[500->800]]", result.toString());
    }

    public void testGetOverlappingIntervals() throws Exception {
        final TimeLine timeline = new TimeLine();
        timeline.addInterval(new TimeInterval(500L, 600L));
        timeline.addInterval(new TimeInterval(600L, 700L));
        assertEquals(0, timeline.getOverlappingIntervals(false).size());

        SortedSet<TimeInterval> overlappingIntervals = timeline.getOverlappingIntervals(true);
        assertEquals(1, overlappingIntervals.size());
        assertEquals("[TimeInterval[600->600]]", overlappingIntervals.toString());

        timeline.addInterval(new TimeInterval(600L, 650L));

        overlappingIntervals = timeline.getOverlappingIntervals(true);
        assertEquals(1, overlappingIntervals.size());
        assertEquals("[TimeInterval[600->650]]", overlappingIntervals.toString());

        timeline.addInterval(new TimeInterval(900L, 950L));
        timeline.addInterval(new TimeInterval(920L, 1000L));

        overlappingIntervals = timeline.getOverlappingIntervals(true);
        assertEquals(2, overlappingIntervals.size());
        assertEquals("[TimeInterval[600->650], TimeInterval[920->950]]", overlappingIntervals.toString());
    }
}
