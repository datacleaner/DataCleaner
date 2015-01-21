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

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Represents a timeline of some entity. A timeline contains several intervals.
 * 
 * 
 */
public class TimeLine implements Serializable {

	private static final long serialVersionUID = 1L;

	private NavigableSet<TimeInterval> intervals = new TreeSet<TimeInterval>();

	public TimeLine() {
	}

	public void addInterval(TimeInterval i) {
		intervals.add(i);
	}

	/**
	 * Gets the intervals registered in this timeline
	 * 
	 * @return
	 */
	public SortedSet<TimeInterval> getIntervals() {
		return Collections.unmodifiableSortedSet(intervals);
	}

	/**
	 * Gets a set of intervals without any overlaps
	 * 
	 * @return
	 */
	public SortedSet<TimeInterval> getFlattenedIntervals() {
		return getFlattenedIntervals(intervals);
	}

	private SortedSet<TimeInterval> getFlattenedIntervals(SortedSet<TimeInterval> intervals) {
		SortedSet<TimeInterval> result = new TreeSet<TimeInterval>();
		for (TimeInterval interval : intervals) {
			for (Iterator<TimeInterval> it = result.iterator(); it.hasNext();) {
				TimeInterval ti = it.next();
				if (ti.overlapsWith(interval)) {
					it.remove();
					interval = TimeInterval.merge(ti, interval);
				}
			}
			result.add(interval);
		}

		return result;
	}

	/**
	 * Gets a set of intervals representing the times where there are more than
	 * one interval overlaps.
	 * 
	 * @param includeSingleTimeInstanceIntervals
	 *            whether or not to include intervals if only the ends of two
	 *            (or more) intervals are overlapping. If, for example, there
	 *            are two intervals, A-to-B and B-to-C. Should "B-to-B" be
	 *            included because B actually overlaps?
	 * 
	 * @return
	 */
	public SortedSet<TimeInterval> getOverlappingIntervals(boolean includeSingleTimeInstanceIntervals) {
		SortedSet<TimeInterval> result = new TreeSet<TimeInterval>();
		for (TimeInterval interval1 : intervals) {
			for (TimeInterval interval2 : intervals) {
				if (interval1 != interval2) {
					TimeInterval overlap = interval1.getOverlap(interval2);
					if (overlap != null) {
						result.add(overlap);
					}
				}
			}
		}

		result = getFlattenedIntervals(result);

		if (!includeSingleTimeInstanceIntervals) {
			for (Iterator<TimeInterval> it = result.iterator(); it.hasNext();) {
				TimeInterval timeInterval = it.next();
				if (timeInterval.isSingleTimeInstance()) {
					it.remove();
				}
			}
		}

		return result;
	}

	/**
	 * Gets a set of intervals representing the times that are NOT represented
	 * in this timeline.
	 * 
	 * @return
	 */
	public SortedSet<TimeInterval> getTimeGapIntervals() {
		SortedSet<TimeInterval> flattenedIntervals = getFlattenedIntervals();
		SortedSet<TimeInterval> gaps = new TreeSet<TimeInterval>();

		TimeInterval previous = null;
		for (TimeInterval timeInterval : flattenedIntervals) {
			if (previous != null) {
				long from = previous.getTo();
				long to = timeInterval.getFrom();
				TimeInterval gap = new TimeInterval(from, to);
				gaps.add(gap);
			}
			previous = timeInterval;
		}

		return gaps;
	}

	/**
	 * Gets the first date in this timeline
	 * 
	 * @return
	 */
	public Date getFrom() {
		TimeInterval first = intervals.first();
		if (first != null) {
			return new Date(first.getFrom());
		}
		return null;
	}

	/**
	 * Gets the last date in this timeline
	 * 
	 * @return
	 */
	public Date getTo() {
		Long to = null;
		for (TimeInterval interval : intervals) {
			if (to == null) {
				to = interval.getTo();
			} else {
				to = Math.max(interval.getTo(), to);
			}
		}
		if (to != null) {
			return new Date(to);
		}
		return null;
	}
}
