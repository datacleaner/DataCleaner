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

import java.util.Comparator;

import org.datacleaner.beans.api.Distributed;
import org.datacleaner.beans.api.QueryOptimizedFilter;
import org.datacleaner.beans.api.Validate;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.InputRow;
import org.datacleaner.util.HasLabelAdvice;
import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.query.OperatorType;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.Column;

/**
 * Abstract {@link QueryOptimizedFilter} which implementation for range filters
 * which demarcate valid value bounds.
 */
@Distributed(true)
abstract class AbstractQueryOptimizedRangeFilter<E> implements QueryOptimizedFilter<RangeFilterCategory>,
        Comparator<E>, HasLabelAdvice {

    @Validate
    public void validate() {
        if (compare(getLowestValue(), getHighestValue()) > 0) {
            throw new IllegalStateException("Lowest value is greater than the highest value");
        }
    }

    public abstract InputColumn<? extends E> getColumn();

    public abstract E getHighestValue();

    public abstract E getLowestValue();

    @Override
    public RangeFilterCategory categorize(InputRow inputRow) {
        E value = inputRow.getValue(getColumn());
        return categorize(value);
    }

    protected RangeFilterCategory categorize(E value) {
        if (value == null) {
            return RangeFilterCategory.LOWER;
        }
        if (compare(value, getLowestValue()) < 0) {
            return RangeFilterCategory.LOWER;
        }
        if (compare(value, getHighestValue()) > 0) {
            return RangeFilterCategory.HIGHER;
        }

        return RangeFilterCategory.VALID;
    }

    @Override
    public boolean isOptimizable(RangeFilterCategory category) {
        return true;
    }

    @Override
    public String getSuggestedLabel() {
        final E highestValue = getHighestValue();
        final E lowestValue = getLowestValue();
        if (highestValue == null || lowestValue == null) {
            return null;
        }
        final InputColumn<? extends E> column = getColumn();
        if (column == null) {
            return null;
        }
        return lowestValue + " =< " + column.getName() + " =< " + highestValue;
    }

    @Override
    public Query optimizeQuery(Query q, RangeFilterCategory category) {
        final Column col = getColumn().getPhysicalColumn();
        final SelectItem selectItem = new SelectItem(col);
        switch (category) {
        case LOWER:
            // special case, null is also considered "lower"
            final FilterItem isNullFilter = new FilterItem(selectItem, OperatorType.EQUALS_TO, null);
            final FilterItem isLowerThanFilter = new FilterItem(selectItem, OperatorType.LESS_THAN, getLowestValue());
            q.where(new FilterItem(isNullFilter, isLowerThanFilter));
            return q;
        case HIGHER:
            q.where(selectItem, OperatorType.GREATER_THAN, getHighestValue());
            return q;
        case VALID:
            final E lowestValue = getLowestValue();
            final E highestValue = getHighestValue();
            if (compare(lowestValue, highestValue) == 0) {
                // special case where highest and lowest value are equal
                q.where(col, OperatorType.EQUALS_TO, lowestValue);
                return q;
            }

            final FilterItem orFilter1;
            {
                final FilterItem f1 = new FilterItem(selectItem, OperatorType.GREATER_THAN, lowestValue);
                final FilterItem f2 = new FilterItem(selectItem, OperatorType.EQUALS_TO, lowestValue);
                orFilter1 = new FilterItem(f1, f2);
            }

            final FilterItem orFilter2;
            {
                final FilterItem f3 = new FilterItem(selectItem, OperatorType.LESS_THAN, highestValue);
                final FilterItem f4 = new FilterItem(selectItem, OperatorType.EQUALS_TO, highestValue);
                orFilter2 = new FilterItem(f3, f4);
            }

            q.where(orFilter1);
            q.where(orFilter2);
            return q;
        default:
            throw new UnsupportedOperationException();
        }
    }
}
