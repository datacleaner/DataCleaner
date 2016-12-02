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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.query.OperatorType;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.util.HasName;
import org.datacleaner.api.Alias;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Distributed;
import org.datacleaner.api.HasLabelAdvice;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.QueryOptimizedFilter;
import org.datacleaner.components.categories.FilterCategory;

@Named("Null check")
@Alias("Not null")
@Description("Filter rows that contain null values.")
@Categorized(FilterCategory.class)
@Distributed(true)
public class NullCheckFilter implements QueryOptimizedFilter<NullCheckFilter.NullCheckCategory>, HasLabelAdvice {

    public enum NullCheckCategory {
        @Alias("INVALID")
        NULL,

        @Alias("VALID")
        NOT_NULL
    }

    public enum EvaluationMode implements HasName {
        ALL_FIELDS("When all fields are NULL, the record is considered NULL"),
        ANY_FIELD("When any field is NULL, the record is considered NULL");

        private final String _name;

        EvaluationMode(final String name) {
            _name = name;
        }

        @Override
        public String getName() {
            return _name;
        }

    }

    @Configured
    @Description("Select columns that should NOT have null values")
    InputColumn<?>[] columns;

    @Configured
    @Description("Consider empty strings (\"\") as null also?")
    boolean considerEmptyStringAsNull = false;

    @Configured("Evaluation mode")
    EvaluationMode evaluationMode = EvaluationMode.ANY_FIELD;

    public NullCheckFilter() {
    }

    public NullCheckFilter(final InputColumn<?>[] columns, final boolean considerEmptyStringAsNull) {
        this();
        this.columns = columns;
        this.considerEmptyStringAsNull = considerEmptyStringAsNull;
    }

    public NullCheckFilter(final InputColumn<?>[] columns, final boolean considerEmptyStringAsNull,
            final EvaluationMode evaluationMode) {
        this();
        this.columns = columns;
        this.considerEmptyStringAsNull = considerEmptyStringAsNull;
        this.evaluationMode = evaluationMode;
    }

    @Override
    public String getSuggestedLabel() {
        if (columns == null || columns.length != 1) {
            return null;
        }
        final InputColumn<?> column = columns[0];
        return column.getName() + " is null?";
    }

    public void setConsiderEmptyStringAsNull(final boolean considerEmptyStringAsNull) {
        this.considerEmptyStringAsNull = considerEmptyStringAsNull;
    }

    @Override
    public boolean isOptimizable(final NullCheckCategory category) {
        if (evaluationMode == EvaluationMode.ANY_FIELD) {
            return true;
        }
        // can be further improved but requires changes to optimizeQuery(...)
        return false;
    }

    @Override
    public Query optimizeQuery(final Query q, final NullCheckCategory category) {
        if (category == NullCheckCategory.NOT_NULL) {
            for (final InputColumn<?> col : columns) {
                final Column column = col.getPhysicalColumn();
                if (column == null) {
                    throw new IllegalStateException("Cannot optimize on non-physical column: " + col);
                }
                q.where(column, OperatorType.DIFFERENT_FROM, null);
                if (considerEmptyStringAsNull && col.getDataType() == String.class) {
                    q.where(column, OperatorType.DIFFERENT_FROM, "");
                }
            }
        } else {
            // if NULL all filter items will be OR'ed.
            final List<FilterItem> filterItems = new ArrayList<>();
            for (final InputColumn<?> col : columns) {
                final Column column = col.getPhysicalColumn();
                if (column == null) {
                    throw new IllegalStateException("Cannot optimize on non-physical column: " + col);
                }

                final SelectItem selectItem = new SelectItem(column);
                final FilterItem fi1 = new FilterItem(selectItem, OperatorType.EQUALS_TO, null);
                filterItems.add(fi1);
                if (considerEmptyStringAsNull && col.getDataType() == String.class) {
                    final FilterItem fi2 = new FilterItem(selectItem, OperatorType.EQUALS_TO, "");
                    filterItems.add(fi2);
                }
            }
            q.where(new FilterItem(filterItems.toArray(new FilterItem[filterItems.size()])));
        }
        return q;
    }

    @Override
    public NullCheckCategory categorize(final InputRow inputRow) {
        if (evaluationMode.equals(EvaluationMode.ANY_FIELD)) {
            return categorizeAnyFieldMode(inputRow);
        } else {
            return categorizeAllFieldMode(inputRow);
        }
    }

    private NullCheckCategory categorizeAnyFieldMode(final InputRow inputRow) {
        for (final InputColumn<?> col : columns) {
            final Object value = inputRow.getValue(col);
            if (value == null) {
                return NullCheckCategory.NULL;
            }

            if (considerEmptyStringAsNull && "".equals(value)) {
                return NullCheckCategory.NULL;
            }
        }
        return NullCheckCategory.NOT_NULL;
    }

    private NullCheckCategory categorizeAllFieldMode(final InputRow inputRow) {
        NullCheckCategory result = NullCheckCategory.NULL;
        for (final InputColumn<?> col : columns) {
            final Object value = inputRow.getValue(col);
            if (value != null) {
                if (considerEmptyStringAsNull) {
                    if (!"".equals(value)) {
                        result = NullCheckCategory.NOT_NULL;
                        break;
                    }
                } else {
                    result = NullCheckCategory.NOT_NULL;
                    break;
                }
            }

        }
        return result;
    }
}
