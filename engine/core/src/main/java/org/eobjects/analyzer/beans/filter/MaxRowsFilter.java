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
package org.eobjects.analyzer.beans.filter;

import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Distributed;
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.beans.api.NumberProperty;
import org.eobjects.analyzer.beans.api.QueryOptimizedFilter;
import org.eobjects.analyzer.beans.api.Validate;
import org.eobjects.analyzer.beans.categories.FilterCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.util.HasLabelAdvice;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Column;

@FilterBean("Max rows")
@Description("Sets a maximum number of rows to process.")
@Categorized(FilterCategory.class)
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
            q.setMaxRows(maxRows);

            if (firstRow > 1) {
                q.setFirstRow(firstRow);
            }

            if (orderColumn != null) {
                Column physicalColumn = orderColumn.getPhysicalColumn();
                q.orderBy(physicalColumn);
            }
        } else {
            throw new IllegalStateException("Can only optimize the VALID max rows category");
        }
        return q;
    }

}
