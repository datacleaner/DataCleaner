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
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Named;

import org.apache.metamodel.util.Month;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Concurrent;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Distributed;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.beans.categories.DateAndTimeCategory;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabDimension;
import org.datacleaner.result.CrosstabNavigator;
import org.datacleaner.result.CrosstabResult;

@Named("Month distribution")
@Description("Finds the distribution of months from Date values.")
@Concurrent(true)
@Categorized(DateAndTimeCategory.class)
@Distributed(reducer=MonthDistributionResultReducer.class)
public class MonthDistributionAnalyzer implements Analyzer<CrosstabResult> {

    private final Map<InputColumn<Date>, ConcurrentMap<Integer, AtomicInteger>> distributionMap;

    @Configured
    InputColumn<Date>[] dateColumns;

    public MonthDistributionAnalyzer() {
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
                c.setTime(value);
                final int monthConstant = c.get(Calendar.MONTH);
                final ConcurrentMap<Integer, AtomicInteger> countMap = distributionMap.get(col);
                final AtomicInteger previousCount = countMap.putIfAbsent(monthConstant,
                        new AtomicInteger(distinctCount));
                if (previousCount != null) {
                    previousCount.addAndGet(distinctCount);
                }
            }
        }
    }

    @Override
    public CrosstabResult getResult() {
        final CrosstabDimension columnDimension = new CrosstabDimension("Column");
        final CrosstabDimension monthDimension = new CrosstabDimension("Month");

        for (Month month : Month.values()) {
            final String monthName = toMonthName(month);
            monthDimension.addCategory(monthName);
        }

        final Crosstab<Integer> crosstab = new Crosstab<Integer>(Integer.class, columnDimension, monthDimension);
        for (InputColumn<Date> col : dateColumns) {
            columnDimension.addCategory(col.getName());
            final CrosstabNavigator<Integer> nav = crosstab.where(columnDimension, col.getName());

            final Map<Integer, AtomicInteger> countMap = distributionMap.get(col);

            for (Entry<Integer, AtomicInteger> entry : countMap.entrySet()) {
                final Integer monthConstant = entry.getKey();
                final Month month = Month.getByCalendarConstant(monthConstant);
                final AtomicInteger count = entry.getValue();
                final String monthName = toMonthName(month);
                nav.where(monthDimension, monthName).put(count.intValue());
            }
        }

        return new CrosstabResult(crosstab);
    }

    private String toMonthName(Month month) {
        final String upperCaseMonthName = month.toString();
        final String monthName = upperCaseMonthName.charAt(0) + upperCaseMonthName.substring(1).toLowerCase();
        return monthName;
    }

    // used only for unittesting
    public void setDateColumns(InputColumn<Date>[] dateColumns) {
        this.dateColumns = dateColumns;
    }
}
