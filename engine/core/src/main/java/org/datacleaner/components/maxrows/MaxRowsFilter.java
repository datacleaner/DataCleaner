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
package org.datacleaner.components.maxrows;

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Named;

import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Column;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Distributed;
import org.datacleaner.api.HasLabelAdvice;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.NumberProperty;
import org.datacleaner.api.QueryOptimizedFilter;
import org.datacleaner.api.Validate;
import org.datacleaner.components.categories.FilterCategory;

@Named("Max rows")
@Description("Sets a maximum number of rows to process.")
@Categorized(value = FilterCategory.class)
@Distributed(false)
public class MaxRowsFilter implements QueryOptimizedFilter<MaxRowsFilter.Category>, HasLabelAdvice {

    public static enum Category {
        VALID, INVALID
    }

    @Configured
    @NumberProperty(negative = false, zero = false)
    @Description("The maximum number of rows to process.")
    int maxRows = 1000;

    @Configured
    @NumberProperty(negative = false, zero = false)
    @Description("The first row (aka 'offset') to process.")
    int firstRow = 1;

    @Configured(required = false)
    @Description("Optional column to use for specifying dataset ordering. Use if consistent pagination is needed.")
    InputColumn<?> orderColumn;

    private final AtomicInteger counter = new AtomicInteger();

    public MaxRowsFilter() {
    }

    public MaxRowsFilter(int firstRow, int maxRows) {
        this();
        this.firstRow = firstRow;
        this.maxRows = maxRows;
    }

    @Override
    public String getSuggestedLabel() {
        return "Max " + getMaxRows() + " rows";
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public int getFirstRow() {
        return firstRow;
    }

    public void setFirstRow(int firstRow) {
        this.firstRow = firstRow;
    }

    public InputColumn<?> getOrderColumn() {
        return orderColumn;
    }

    public void setOrderColumn(InputColumn<?> orderColumn) {
        this.orderColumn = orderColumn;
    }

    @Validate
    public void validate() {
        if (maxRows <= 0) {
            throw new IllegalStateException("Max rows value must be a positive integer");
        }
        if (firstRow <= 0) {
            throw new IllegalStateException("First row value must be a positive integer");
        }
    }

    @Override
    public Category categorize(InputRow inputRow) {
        int count = counter.incrementAndGet();
        if (count < firstRow || count >= maxRows + firstRow) {
            return Category.INVALID;
        }
        return Category.VALID;
    }

    @Override
    public boolean isOptimizable(Category category) {
        // can only optimize the valid records
        return category == Category.VALID;
    }

    @Override
    public Query optimizeQuery(Query q, Category category) {
        if (category == Category.VALID) {
            final Integer previousMaxRows = q.getMaxRows();
            final Integer previousFirstRow = q.getFirstRow();

            if (firstRow > 1) {
                if (previousFirstRow == null) {
                    q.setFirstRow(firstRow);
                } else {
                    final int newFirstRow = previousFirstRow.intValue() + firstRow;
                    q.setFirstRow(newFirstRow);
                }
            }

            if (previousMaxRows == null) {
                q.setMaxRows(maxRows);
            } else {
                int newMaxRows = Math.min(previousMaxRows.intValue(), maxRows);
                if (previousFirstRow != null) {
                    final Integer newFirstRow = q.getFirstRow();
                    final int maxWindowSizeFrombefore = previousFirstRow.intValue() + previousMaxRows.intValue()
                            - newFirstRow;
                    newMaxRows = Math.min(newMaxRows, maxWindowSizeFrombefore);
                }

                // avoid negative max rows
                newMaxRows = Math.max(0, newMaxRows);

                q.setMaxRows(newMaxRows);
            }

            if (orderColumn != null) {
                final Column physicalColumn = orderColumn.getPhysicalColumn();
                q.orderBy(physicalColumn);
            }
        } else {
            throw new IllegalStateException("Can only optimize the VALID max rows category");
        }
        return q;
    }

}
