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
package org.eobjects.analyzer.beans.filter;

import java.util.Date;

import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.beans.categories.FilterCategory;
import org.eobjects.analyzer.data.InputColumn;

@FilterBean("Date range")
@Description("A filter that filters out rows where a number value is outside a specified range")
@Categorized(FilterCategory.class)
public class DateRangeFilter extends AbstractQueryOptimizedRangeFilter<Date> {

    @Configured(order = 0)
    InputColumn<Date> column;

    @Configured(order = 1)
    Date lowestValue;

    @Configured(order = 2)
    Date highestValue;

    public DateRangeFilter(Date lowestValue, Date highestValue) {
        this.lowestValue = lowestValue;
        this.highestValue = highestValue;
    }

    public DateRangeFilter() {
        this(null, null);
    }

    @Override
    public Date getHighestValue() {
        return highestValue;
    }

    @Override
    public Date getLowestValue() {
        return lowestValue;
    }

    @Override
    public InputColumn<Date> getColumn() {
        return column;
    }

    @Override
    public int compare(Date o1, Date o2) {
        return o1.compareTo(o2);
    }
}
