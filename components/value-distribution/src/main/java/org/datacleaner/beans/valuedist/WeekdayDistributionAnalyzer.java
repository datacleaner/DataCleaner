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
package org.datacleaner.beans.valuedist;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Named;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Concurrent;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Distributed;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.components.categories.DateAndTimeCategory;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabDimension;
import org.datacleaner.result.CrosstabNavigator;
import org.datacleaner.result.CrosstabResult;

@Named("Weekday distribution")
@Description("Finds the distribution of weekdays from Date values.")
@Concurrent(true)
@Categorized(DateAndTimeCategory.class)
@Distributed(reducer = WeekdayDistributionResultReducer.class)
public class WeekdayDistributionAnalyzer implements Analyzer<CrosstabResult> {

    private final Map<InputColumn<Date>, Map<Integer, AtomicInteger>> distributionMap;

    @Configured
    InputColumn<Date>[] dateColumns;

    public WeekdayDistributionAnalyzer() {
        distributionMap = new HashMap<>();
    }

    @Initialize
    public void init() {
        for (final InputColumn<Date> col : dateColumns) {
            final Map<Integer, AtomicInteger> countMap = new HashMap<>(7);
            for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
                // put a count of 0 for each day of the week
                countMap.put(i, new AtomicInteger(0));
            }
            distributionMap.put(col, countMap);
        }
    }

    @Override
    public void run(final InputRow row, final int distinctCount) {
        for (final InputColumn<Date> col : dateColumns) {
            final Date value = row.getValue(col);
            if (value != null) {
                final Calendar c = Calendar.getInstance();
                c.setTime(value);
                final int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
                final Map<Integer, AtomicInteger> countMap = distributionMap.get(col);
                final AtomicInteger count = countMap.get(dayOfWeek);
                count.addAndGet(distinctCount);
            }
        }
    }

    @Override
    public CrosstabResult getResult() {
        final CrosstabDimension columnDimension = new CrosstabDimension("Column");
        final CrosstabDimension weekdayDimension = new CrosstabDimension("Weekday");
        weekdayDimension.addCategory("Sunday").addCategory("Monday").addCategory("Tuesday").addCategory("Wednesday")
                .addCategory("Thursday").addCategory("Friday").addCategory("Saturday");
        final Crosstab<Integer> crosstab = new Crosstab<>(Integer.class, columnDimension, weekdayDimension);
        for (final InputColumn<Date> col : dateColumns) {
            columnDimension.addCategory(col.getName());
            final CrosstabNavigator<Integer> nav = crosstab.where(columnDimension, col.getName());
            final Map<Integer, AtomicInteger> countMap = distributionMap.get(col);
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
    public void setDateColumns(final InputColumn<Date>[] dateColumns) {
        this.dateColumns = dateColumns;
    }
}
