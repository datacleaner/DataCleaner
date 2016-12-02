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

import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.query.OperatorType;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.Column;
import org.datacleaner.api.Distributed;
import org.datacleaner.api.HasLabelAdvice;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.QueryOptimizedFilter;
import org.datacleaner.api.Validate;

/**
 * Abstract {@link QueryOptimizedFilter} which implementation for range filters
 * which demarcate valid value bounds.
 */
@Distributed(true)
abstract class AbstractQueryOptimizedRangeFilter<E>
        implements QueryOptimizedFilter<RangeFilterCategory>, Comparator<E>, HasLabelAdvice {

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
    public RangeFilterCategory categorize(final InputRow inputRow) {
        final E value = inputRow.getValue(getColumn());
        return categorize(value);
    }

    protected RangeFilterCategory categorize(final E value) {
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
    public boolean isOptimizable(final RangeFilterCategory category) {
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
    public Query optimizeQuery(final Query q, final RangeFilterCategory category) {
        final Column col = getColumn().getPhysicalColumn();
        final SelectItem selectItem = new SelectItem(col);
        switch (category) {
        case LOWER:
            return lowerQuery(q, selectItem);
        case HIGHER:
            return higherQuery(q, selectItem);
        case VALID:
            return validQuery(q, col, selectItem);
        default:
            throw new UnsupportedOperationException();
        }
    }

    private Query validQuery(final Query query, final Column col, final SelectItem selectItem) {
        final E lowestValue = getLowestValue();
        final E highestValue = getHighestValue();
        if (compare(lowestValue, highestValue) == 0) {
            // special case where highest and lowest value are equal
            query.where(col, OperatorType.EQUALS_TO, lowestValue);
            return query;
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

        query.where(orFilter1);
        query.where(orFilter2);
        return query;
    }

    private Query higherQuery(final Query query, final SelectItem selectItem) {
        query.where(selectItem, OperatorType.GREATER_THAN, getHighestValue());
        return query;
    }

    private Query lowerQuery(final Query query, final SelectItem selectItem) {
        // special case, null is also considered "lower"
        final FilterItem isNullFilter = new FilterItem(selectItem, OperatorType.EQUALS_TO, null);
        final FilterItem isLowerThanFilter = new FilterItem(selectItem, OperatorType.LESS_THAN, getLowestValue());
        query.where(new FilterItem(isNullFilter, isLowerThanFilter));
        return query;
    }
}
