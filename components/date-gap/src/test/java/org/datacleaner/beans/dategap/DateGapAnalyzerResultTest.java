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

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.TestCase;

public class DateGapAnalyzerResultTest extends TestCase {

    public void testToStringNullGaps() throws Exception {
        final Map<String, TimeInterval> completeIntervals = null;
        final Map<String, SortedSet<TimeInterval>> overlaps = null;
        final Map<String, SortedSet<TimeInterval>> gaps = null;

        final DateGapAnalyzerResult result =
                new DateGapAnalyzerResult("from", "to", null, completeIntervals, gaps, overlaps);
        assertEquals("DateGapAnalyzerResult[gaps={}]", result.toString());
    }

    public void testToStringEmptyGaps() throws Exception {
        final Map<String, TimeInterval> completeIntervals = null;
        final Map<String, SortedSet<TimeInterval>> overlaps = null;
        final Map<String, SortedSet<TimeInterval>> gaps = new HashMap<>();

        final DateGapAnalyzerResult result =
                new DateGapAnalyzerResult("from", "to", null, completeIntervals, gaps, overlaps);
        assertEquals("DateGapAnalyzerResult[gaps={}]", result.toString());
    }

    public void testToStringNormalGaps() throws Exception {
        final Map<String, TimeInterval> completeIntervals = null;
        final Map<String, SortedSet<TimeInterval>> overlaps = null;
        final Map<String, SortedSet<TimeInterval>> gaps = new HashMap<>();
        final SortedSet<TimeInterval> timeIntervals = new TreeSet<>();
        timeIntervals.add(new TimeInterval(1000 * 1000, 1002 * 1000));
        timeIntervals.add(new TimeInterval(1004 * 1000, 1005 * 1000));
        gaps.put("foo", timeIntervals);

        final DateGapAnalyzerResult result =
                new DateGapAnalyzerResult("from", "to", null, completeIntervals, gaps, overlaps);
        assertEquals(
                "DateGapAnalyzerResult[gaps={foo=[TimeInterval[1000000->1002000], TimeInterval[1004000->1005000]]}]",
                result.toString());
    }

    public void testToStringGapsWithNullLabel() throws Exception {
        final Map<String, TimeInterval> completeIntervals = null;
        final Map<String, SortedSet<TimeInterval>> overlaps = null;
        final Map<String, SortedSet<TimeInterval>> gaps = new HashMap<>();
        final SortedSet<TimeInterval> timeIntervals = new TreeSet<>();
        timeIntervals.add(new TimeInterval(1000 * 1000, 1002 * 1000));
        timeIntervals.add(new TimeInterval(1004 * 1000, 1005 * 1000));
        gaps.put(null, timeIntervals);

        final DateGapAnalyzerResult result =
                new DateGapAnalyzerResult("from", "to", null, completeIntervals, gaps, overlaps);
        assertEquals(
                "DateGapAnalyzerResult[gaps={null=[TimeInterval[1000000->1002000], TimeInterval[1004000->1005000]]}]",
                result.toString());
    }
}
