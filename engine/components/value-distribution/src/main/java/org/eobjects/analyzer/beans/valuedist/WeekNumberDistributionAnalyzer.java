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
package org.eobjects.analyzer.beans.valuedist;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Concurrent;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Distributed;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.NumberProperty;
import org.eobjects.analyzer.beans.categories.DateAndTimeCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.CrosstabNavigator;
import org.eobjects.analyzer.result.CrosstabResult;
import org.apache.metamodel.util.Weekday;

@AnalyzerBean("Week number distribution")
@Description("Finds the distribution of week numbers from Date values.")
@Concurrent(true)
@Categorized(DateAndTimeCategory.class)
@Distributed(reducer=DatePartDistributionResultReducer.class)
public class WeekNumberDistributionAnalyzer implements Analyzer<CrosstabResult> {

    @Configured
    InputColumn<Date>[] dateColumns;

    @Configured(order = 10)
    @NumberProperty(negative = false, positive = true)
    int minimalDaysInFirstWeek = Calendar.getInstance().getMinimalDaysInFirstWeek();

    @Configured(order = 11)
    Weekday firstDayOfWeek = Weekday.getByCalendarConstant(Calendar.getInstance().getFirstDayOfWeek());

    private final Map<InputColumn<Date>, ConcurrentMap<Integer, AtomicInteger>> distributionMap;

    public WeekNumberDistributionAnalyzer() {
        distributionMap = new HashMap<InputColumn<Date>, ConcurrentMap<Integer, AtomicInteger>>();
    }

    @Initialize
    public void init() {
        for (InputColumn<Date> col : dateColumns) {
            final ConcurrentMap<Integer, AtomicInteger> countMap = new ConcurrentHashMap<Integer, AtomicInteger>();
            distributionMap.put(col, countMap);
        }
    }

    @Override
    public void run(InputRow row, int distinctCount) {
        for (InputColumn<Date> col : dateColumns) {
            final Date value = row.getValue(col);
            if (value != null) {
                final Calendar c = Calendar.getInstance();
                c.setMinimalDaysInFirstWeek(minimalDaysInFirstWeek);
                c.setFirstDayOfWeek(firstDayOfWeek.getCalendarConstant());
                c.setTime(value);
                final int weekNumber = c.get(Calendar.WEEK_OF_YEAR);
                final ConcurrentMap<Integer, AtomicInteger> countMap = distributionMap.get(col);
                final AtomicInteger previousCount = countMap.putIfAbsent(weekNumber, new AtomicInteger(distinctCount));
                if (previousCount != null) {
                    previousCount.addAndGet(distinctCount);
                }
            }
        }
    }

    @Override
    public CrosstabResult getResult() {
        final CrosstabDimension columnDimension = new CrosstabDimension("Column");
        final CrosstabDimension weekNumberDimension = new CrosstabDimension("Week number");

        final SortedSet<Integer> weekNumbers = new TreeSet<Integer>();
        for (InputColumn<Date> col : dateColumns) {
            final Map<Integer, AtomicInteger> countMap = distributionMap.get(col);
            final Set<Integer> weekNumbersOfColumn = countMap.keySet();
            weekNumbers.addAll(weekNumbersOfColumn);
        }

        for (Integer weekNumber : weekNumbers) {
            weekNumberDimension.addCategory(weekNumber + "");
        }

        final Crosstab<Integer> crosstab = new Crosstab<Integer>(Integer.class, columnDimension, weekNumberDimension);
        for (InputColumn<Date> col : dateColumns) {
            columnDimension.addCategory(col.getName());
            final CrosstabNavigator<Integer> nav = crosstab.where(columnDimension, col.getName());

            final Map<Integer, AtomicInteger> countMap = distributionMap.get(col);

            for (Entry<Integer, AtomicInteger> entry : countMap.entrySet()) {
                final Integer weekNumber = entry.getKey();
                final AtomicInteger count = entry.getValue();
                nav.where(weekNumberDimension, weekNumber + "").put(count.intValue());
            }
        }

        return new CrosstabResult(crosstab);
    }

    // used only for unittesting
    public void setDateColumns(InputColumn<Date>[] dateColumns) {
        this.dateColumns = dateColumns;
    }
}
