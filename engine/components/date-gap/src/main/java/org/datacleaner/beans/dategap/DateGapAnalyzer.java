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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.inject.Named;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.beans.categories.DateAndTimeCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("Date gap analyzer")
@Description("Analyze the periodic gaps between FROM and TO dates.")
@Categorized(DateAndTimeCategory.class)
public class DateGapAnalyzer implements Analyzer<DateGapAnalyzerResult> {

	private static final Logger logger = LoggerFactory.getLogger(DateGapAnalyzer.class);

	@Configured(order = 1)
	InputColumn<Date> fromColumn;

	@Configured(order = 2)
	InputColumn<Date> toColumn;

	@Configured(order = 3, required = false)
	@Description("Optional column to group timelines by, if the table contains multiple timelines")
	InputColumn<String> groupColumn;

	@Configured(order = 4, required = false, value = "Count intersecting from and to dates as overlaps")
	Boolean singleDateOverlaps = false;

	@Configured(order = 5, value = "Fault tolerant switch from/to dates", required = false)
	@Description("Turn on/off automatic switching of FROM and TO dates, if FROM has a higher value than TO.")
	boolean faultTolerantDateSwitch = true;

	private final Map<String, TimeLine> timelines = new HashMap<String, TimeLine>();

	public DateGapAnalyzer() {
	}

	public DateGapAnalyzer(InputColumn<Date> fromColumn, InputColumn<Date> toColumn, InputColumn<String> groupColumn) {
		this.fromColumn = fromColumn;
		this.toColumn = toColumn;
		this.groupColumn = groupColumn;
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		Date from = row.getValue(fromColumn);
		Date to = row.getValue(toColumn);

		if (from != null && to != null) {
			String groupName = null;
			if (groupColumn != null) {
				groupName = row.getValue(groupColumn);
			}

			if (faultTolerantDateSwitch && from.compareTo(to) > 0) {
				logger.debug("Switching around from and to, because {} is higher than {} (row: {})", new Object[] { from, to,
						row });
				put(groupName, new TimeInterval(to, from));
			} else {
				put(groupName, new TimeInterval(from, to));
			}
		} else {
			logger.debug("Encountered row where from column or to column was null, ignoring");
		}
	}

	protected void put(String groupName, TimeInterval interval) {
		TimeLine timeline = timelines.get(groupName);
		if (timeline == null) {
			timeline = new TimeLine();
			timelines.put(groupName, timeline);
		}
		timeline.addInterval(interval);
	}

	@Override
	public DateGapAnalyzerResult getResult() {
		boolean includeSingleTimeInstanceIntervals = false;
		if (singleDateOverlaps != null) {
			includeSingleTimeInstanceIntervals = singleDateOverlaps.booleanValue();
		}
		final Map<String, TimeInterval> completeIntervals = new HashMap<String, TimeInterval>();
		final Map<String, SortedSet<TimeInterval>> gaps = new HashMap<String, SortedSet<TimeInterval>>();
		final Map<String, SortedSet<TimeInterval>> overlaps = new HashMap<String, SortedSet<TimeInterval>>();
		final Set<String> groupNames = timelines.keySet();
		for (String name : groupNames) {
			TimeLine timeline = timelines.get(name);
			SortedSet<TimeInterval> timelineGaps = timeline.getTimeGapIntervals();
			SortedSet<TimeInterval> timelineOverlaps = timeline.getOverlappingIntervals(includeSingleTimeInstanceIntervals);

			completeIntervals.put(name, new TimeInterval(timeline.getFrom(), timeline.getTo()));
			gaps.put(name, timelineGaps);
			overlaps.put(name, timelineOverlaps);
		}

		final String groupColumnName = groupColumn == null ? null : groupColumn.getName();
		return new DateGapAnalyzerResult(fromColumn.getName(), toColumn.getName(), groupColumnName, completeIntervals, gaps,
				overlaps);
	}

	public void setFromColumn(InputColumn<Date> fromColumn) {
		this.fromColumn = fromColumn;
	}

	public void setGroupColumn(InputColumn<String> groupColumn) {
		this.groupColumn = groupColumn;
	}

	public void setSingleDateOverlaps(Boolean singleDateOverlaps) {
		this.singleDateOverlaps = singleDateOverlaps;
	}

	public void setToColumn(InputColumn<Date> toColumn) {
		this.toColumn = toColumn;
	}
}
