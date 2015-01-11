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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.Metric;
import org.datacleaner.api.ParameterizableMetric;
import org.datacleaner.util.NullTolerableComparator;

public class DateGapAnalyzerResult implements AnalyzerResult {

    private static final long serialVersionUID = 1L;

    private final String _fromColumnName;
    private final String _toColumnName;
    private final String _groupColumnName;
    private final Map<String, SortedSet<TimeInterval>> _gaps;
    private final Map<String, SortedSet<TimeInterval>> _overlaps;
    private final Map<String, TimeInterval> _completeDurations;

    public DateGapAnalyzerResult(String fromColumnName, String toColumnName, String groupColumnName,
            Map<String, TimeInterval> completeIntervals, Map<String, SortedSet<TimeInterval>> gaps,
            Map<String, SortedSet<TimeInterval>> overlaps) {
        _fromColumnName = fromColumnName;
        _toColumnName = toColumnName;
        _groupColumnName = groupColumnName;

        _completeDurations = (completeIntervals == null ? Collections.<String, TimeInterval> emptyMap()
                : completeIntervals);
        _gaps = (gaps == null ? Collections.<String, SortedSet<TimeInterval>> emptyMap() : gaps);
        _overlaps = (overlaps == null ? Collections.<String, SortedSet<TimeInterval>> emptyMap() : overlaps);
    }

    /**
     * @return the names of the recorded groups of records that have gaps and/or
     *         overlaps
     */
    public Set<String> getGroupNames() {
        return _gaps.keySet();
    }

    @Metric("Total date gap count")
    public int getTotalGapCount() {
        int count = 0;
        Collection<SortedSet<TimeInterval>> gapSets = _gaps.values();
        for (SortedSet<TimeInterval> gapSet : gapSets) {
            count += gapSet.size();
        }
        return count;
    }

    @Metric("Date gap count")
    public ParameterizableMetric getGapCount() {
        return new ParameterizableMetric() {
            @Override
            public Number getValue(String parameter) {
                SortedSet<TimeInterval> gapSet = _gaps.get(parameter);
                if (gapSet == null) {
                    return 0;
                }
                return gapSet.size();
            }

            @Override
            public Collection<String> getParameterSuggestions() {
                return _gaps.keySet();
            }
        };
    }

    /**
     * Gets the complete duration/interval over which a group has been recorded.
     * This duration will always include both gaps and overlaps.
     * 
     * @param groupName
     * @return the complete duration (ie. first and last recorded date) of a
     *         group as a single interval.
     */
    public TimeInterval getCompleteDuration(String groupName) {
        return _completeDurations.get(groupName);
    }

    /**
     * @param groupName
     * @return the intervals that represents gaps in the complete duration
     */
    public SortedSet<TimeInterval> getGaps(String groupName) {
        return _gaps.get(groupName);
    }

    /**
     * @param groupName
     * @return the intervals where there are overlapping entries in the complete
     *         duration
     */
    public SortedSet<TimeInterval> getOverlaps(String groupName) {
        return _overlaps.get(groupName);
    }

    public String getFromColumnName() {
        return _fromColumnName;
    }

    public String getToColumnName() {
        return _toColumnName;
    }

    public String getGroupColumnName() {
        return _groupColumnName;
    }

    @Override
    public String toString() {
        final SortedMap<String, SortedSet<TimeInterval>> gaps;
        if (_gaps instanceof SortedMap) {
            gaps = (SortedMap<String, SortedSet<TimeInterval>>) _gaps;
        } else {
            gaps = new TreeMap<>(NullTolerableComparator.get(String.class));
            gaps.putAll(_gaps);
        }
        return "DateGapAnalyzerResult[gaps=" + gaps + "]";
    }
}
