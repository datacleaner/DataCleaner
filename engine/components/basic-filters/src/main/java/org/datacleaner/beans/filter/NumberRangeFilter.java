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

import org.datacleaner.beans.api.Categorized;
import org.datacleaner.beans.api.Configured;
import org.datacleaner.beans.api.Description;
import org.datacleaner.beans.api.FilterBean;
import org.datacleaner.beans.categories.FilterCategory;
import org.datacleaner.data.InputColumn;

@FilterBean("Number range")
@Description("A filter that filters out rows where a number value is outside a specified range")
@Categorized(FilterCategory.class)
public class NumberRangeFilter extends AbstractQueryOptimizedRangeFilter<Number> {

    @Configured(order = 0)
    InputColumn<Number> column;

    @Configured(order = 1)
    Double lowestValue;

    @Configured(order = 2)
    Double highestValue;

    public NumberRangeFilter(double lowestValue, double highestValue) {
        this.lowestValue = lowestValue;
        this.highestValue = highestValue;
    }

    public NumberRangeFilter() {
        this(0d, 10d);
    }

    @Override
    public Double getHighestValue() {
        return highestValue;
    }

    @Override
    public Double getLowestValue() {
        return lowestValue;
    }

    @Override
    public InputColumn<? extends Number> getColumn() {
        return column;
    }

    @Override
    public int compare(Number o1, Number o2) {
        double diff = o1.doubleValue() - o2.doubleValue();
        if (diff == 0) {
            return 0;
        } else if (diff > 0) {
            return 1;
        } else {
            return -1;
        }
    }
}
