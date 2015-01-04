/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.beans.valuedist;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Concurrent;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Distributed;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.categories.DateAndTimeCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.CrosstabNavigator;
import org.eobjects.analyzer.result.CrosstabResult;

@AnalyzerBean("Weekday distribution")
@Description("Finds the distribution of weekdays from Date values.")
@Concurrent(true)
@Categorized(DateAndTimeCategory.class)
@Distributed(reducer=WeekdayDistributionResultReducer.class)
public class WeekdayDistributionAnalyzer implements Analyzer<CrosstabResult> {

	private final Map<InputColumn<Date>, Map<Integer, AtomicInteger>> distributionMap;

	@Configured
	InputColumn<Date>[] dateColumns;

	public WeekdayDistributionAnalyzer() {
		distributionMap = new HashMap<InputColumn<Date>, Map<Integer, AtomicInteger>>();
	}

	@Initialize
	public void init() {
		for (InputColumn<Date> col : dateColumns) {
			Map<Integer, AtomicInteger> countMap = new HashMap<Integer, AtomicInteger>(7);
			for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
				// put a count of 0 for each day of the week
				countMap.put(i, new AtomicInteger(0));
			}
			distributionMap.put(col, countMap);
		}
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		for (InputColumn<Date> col : dateColumns) {
			Date value = row.getValue(col);
			if (value != null) {
				Calendar c = Calendar.getInstance();
				c.setTime(value);
				int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
				Map<Integer, AtomicInteger> countMap = distributionMap.get(col);
				AtomicInteger count = countMap.get(dayOfWeek);
				count.addAndGet(distinctCount);
			}
		}
	}

	@Override
	public CrosstabResult getResult() {
		CrosstabDimension columnDimension = new CrosstabDimension("Column");
		CrosstabDimension weekdayDimension = new CrosstabDimension("Weekday");
		weekdayDimension.addCategory("Sunday").addCategory("Monday").addCategory("Tuesday").addCategory("Wednesday")
				.addCategory("Thursday").addCategory("Friday").addCategory("Saturday");
		Crosstab<Integer> crosstab = new Crosstab<Integer>(Integer.class, columnDimension, weekdayDimension);
		for (InputColumn<Date> col : dateColumns) {
			columnDimension.addCategory(col.getName());
			CrosstabNavigator<Integer> nav = crosstab.where(columnDimension, col.getName());
			Map<Integer, AtomicInteger> countMap = distributionMap.get(col);
			nav.where(weekdayDimension, "Sunday").put(countMap.get(Calendar.SUNDAY).get());
			nav.where(weekdayDimension, "Monday").put(countMap.get(Calendar.MONDAY).get());
			nav.where(weekdayDimension, "Tuesday").put(countMap.get(Calendar.TUESDAY).get());
			nav.where(weekdayDimension, "Wednesday").put(countMap.get(Calendar.WEDNESDAY).get());
			nav.where(weekdayDimension, "Thursday").put(countMap.get(Calendar.THURSDAY).get());
			nav.where(weekdayDimension, "Friday").put(countMap.get(Calendar.FRIDAY).get());
			nav.where(weekdayDimension, "Saturday").put(countMap.get(Calendar.SATURDAY).get());
		}

		return new CrosstabResult(crosstab);
	}

	// used only for unittesting
	public void setDateColumns(InputColumn<Date>[] dateColumns) {
		this.dateColumns = dateColumns;
	}
}
