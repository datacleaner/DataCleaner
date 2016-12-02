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
package org.datacleaner.beans.filter;

import java.util.Date;

import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.components.categories.FilterCategory;
import org.datacleaner.util.convert.NowDate;
import org.datacleaner.util.convert.ShiftedToday;
import org.datacleaner.util.convert.TodayDate;
import org.datacleaner.util.convert.YesterdayDate;

@Named("Date range")
@Description("A filter that filters out rows where a date value is outside a specified range")
@Categorized(FilterCategory.class)
public class DateRangeFilter extends AbstractQueryOptimizedRangeFilter<Date> {

    @Configured(order = 0)
    InputColumn<Date> column;

    @Configured(order = 1)
    Date lowestValue;

    @Configured(order = 2)
    Date highestValue;

    public DateRangeFilter(final Date lowestValue, final Date highestValue) {
        this.lowestValue = lowestValue;
        this.highestValue = highestValue;
    }

    public DateRangeFilter() {
        this(null, null);
    }

    @Override
    public Date getHighestValue() {
        return getDynamicValue(highestValue);
    }

    @Override
    public Date getLowestValue() {
        return getDynamicValue(lowestValue);
    }

    @Override
    public InputColumn<Date> getColumn() {
        return column;
    }

    @Override
    public int compare(final Date o1, final Date o2) {
        return o1.compareTo(o2);
    }

    @Override
    public String getSuggestedLabel() {
        final Date highestValue = getHighestValue();
        final Date lowestValue = getLowestValue();

        if (highestValue == null || lowestValue == null) {
            return null;
        }

        final InputColumn<Date> column = getColumn();

        if (column == null) {
            return null;
        }

        return getDateLabel(lowestValue) + " =< " + column.getName() + " =< " + getDateLabel(highestValue);
    }

    private Date getDynamicValue(final Date date) {
        if (date instanceof NowDate) {
            return new NowDate();
        } else if (date instanceof TodayDate) {
            return new TodayDate();
        } else if (date instanceof YesterdayDate) {
            return new YesterdayDate();
        } else if (date instanceof ShiftedToday) {
            return new ShiftedToday(((ShiftedToday) date).getInput());
        } else {
            return date;
        }
    }

    private String getDateLabel(final Date date) {
        if (date instanceof NowDate) {
            return "now";
        } else if (date instanceof TodayDate) {
            return "today";
        } else if (date instanceof YesterdayDate) {
            return "yesterday";
        } else if (date instanceof ShiftedToday) {
            return "today plus [" + ((ShiftedToday) date).getInput() + "]";
        } else {
            return date.toString();
        }
    }
}
