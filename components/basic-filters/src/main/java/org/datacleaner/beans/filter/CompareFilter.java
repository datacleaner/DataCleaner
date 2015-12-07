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

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.metamodel.data.DefaultRow;
import org.apache.metamodel.data.SimpleDataSetHeader;
import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.query.OperatorType;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnTypeImpl;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.util.HasName;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Distributed;
import org.datacleaner.api.HasLabelAdvice;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.QueryOptimizedFilter;
import org.datacleaner.api.Validate;
import org.datacleaner.components.categories.FilterCategory;
import org.datacleaner.components.convert.ConvertToBooleanTransformer;
import org.datacleaner.components.convert.ConvertToDateTransformer;
import org.datacleaner.components.convert.ConvertToNumberTransformer;
import org.datacleaner.components.convert.ConvertToStringTransformer;
import org.datacleaner.util.ReflectionUtils;

@Named("Compare")
@Description("Compare two values using an operator of your choice. The options available in this filter resemble those of a SQL WHERE clause - you can specify columns, fixed values and use operators including the LIKE operator.")
@Categorized(FilterCategory.class)
@Distributed(true)
public class CompareFilter implements QueryOptimizedFilter<CompareFilter.Category>, HasLabelAdvice {

    public static enum Category {
        TRUE, FALSE;
    }

    public static enum Operator implements HasName {
        LESS_THAN("Less than", OperatorType.LESS_THAN),

        LESS_THAN_OR_EQUAL("Less than or equal", OperatorType.LESS_THAN_OR_EQUAL),

        EQUALS_TO("Equal", OperatorType.EQUALS_TO),

        LIKE("Like", OperatorType.LIKE),

        DIFFERENT_FROM("Not equal", OperatorType.DIFFERENT_FROM),

        GREATER_THAN_OR_EQUAL("Greater than or equal", OperatorType.GREATER_THAN_OR_EQUAL),

        GREATER_THAN("Greater than", OperatorType.GREATER_THAN);

        private final OperatorType _operatorType;
        private final String _name;

        private Operator(String name, OperatorType operatorType) {
            _name = name;
            _operatorType = operatorType;
        }

        public OperatorType getOperatorType() {
            return _operatorType;
        }

        @Override
        public String getName() {
            return _name;
        }
    }

    @Inject
    @Configured(order = 1)
    @Description("The column to compare values of")
    InputColumn<?> inputColumn;

    @Inject
    @Configured(order = 2)
    Operator operator;

    @Inject
    @Configured(order = 21, required = false)
    @Description("Value to compare with")
    String compareValue;

    @Inject
    @Configured(order = 22, required = false)
    @Description("Column holding value to compare with")
    InputColumn<?> compareColumn;

    private Object compareValueAsOperand;
    private SelectItem compareSelectItem;

    public CompareFilter() {
    }

    public CompareFilter(InputColumn<?> column, Operator operator, String compareValue) {
        this();
        this.inputColumn = column;
        this.operator = operator;
        this.compareValue = compareValue;
        init();
    }

    public CompareFilter(InputColumn<?> inputColumn, Operator operator, InputColumn<?> compareColumn) {
        this();
        this.inputColumn = inputColumn;
        this.operator = operator;
        this.compareColumn = compareColumn;
        init();
    }

    public void setCompareColumn(InputColumn<?> compareColumn) {
        this.compareColumn = compareColumn;
    }

    public void setCompareValue(String compareValue) {
        this.compareValue = compareValue;
    }

    @Validate
    public void validate() {
        if (compareColumn == null && compareValue == null) {
            throw new IllegalStateException("Either 'Compare value' or 'Compare column' needs to be specified.");
        }
    }

    @Initialize
    public void init() {
        compareValueAsOperand = toOperand(compareValue);
        compareSelectItem = new SelectItem(
                new MutableColumn("my column", ColumnTypeImpl.convertColumnType(inputColumn.getDataType())));
    }

    @Override
    public String getSuggestedLabel() {
        if (inputColumn == null || operator == null) {
            return null;
        }

        if (compareColumn == null && compareValue == null) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        sb.append(inputColumn.getName());
        sb.append(' ');
        sb.append(operator.getOperatorType().toSql());
        sb.append(' ');

        if (compareColumn != null) {
            sb.append(compareColumn.getName());
        } else if (compareValue != null) {
            final Object operand = toOperand(compareValue);
            if (operand instanceof String) {
                sb.append('\'');
                sb.append(operand);
                sb.append('\'');
            } else {
                sb.append(operand);
            }
        }

        return sb.toString();
    }

    private Object toOperand(Object value) {
        if (value == null) {
            return null;
        }
        final Class<?> dataType = inputColumn.getDataType();
        if (ReflectionUtils.isBoolean(dataType)) {
            return ConvertToBooleanTransformer.transformValue(value, ConvertToBooleanTransformer.DEFAULT_TRUE_TOKENS,
                    ConvertToBooleanTransformer.DEFAULT_FALSE_TOKENS);
        } else if (ReflectionUtils.isDate(dataType)) {
            return ConvertToDateTransformer.getInternalInstance().transformValue(value);
        } else if (ReflectionUtils.isNumber(dataType)) {
            return ConvertToNumberTransformer.transformValue(value);
        } else if (ReflectionUtils.isString(dataType)) {
            return ConvertToStringTransformer.transformValue(value);
        } else {
            return value;
        }
    }

    @Override
    public CompareFilter.Category categorize(InputRow inputRow) {
        final Object inputValue = inputRow.getValue(inputColumn);

        final Object operand;
        if (compareColumn != null) {
            final Object compareColumnValue = inputRow.getValue(compareColumn);
            operand = toOperand(compareColumnValue);
        } else {
            operand = compareValueAsOperand;
        }

        return filter(inputValue, operator, operand);
    }

    public CompareFilter.Category filter(final Object v, final Operator operator, final Object operand) {
        // use MetaModel FilterItem to do the evaluation - it's a bit of a
        // detour, but there's a ton of operator/operand combinations to take
        // care of which is already done there.
        final FilterItem item = new FilterItem(compareSelectItem, operator.getOperatorType(), operand);
        final boolean evaluation = item.evaluate(
                new DefaultRow(new SimpleDataSetHeader(Arrays.asList(compareSelectItem)), new Object[] { v }));

        if (evaluation) {
            return Category.TRUE;
        } else {
            return Category.FALSE;
        }
    }

    @Override
    public boolean isOptimizable(Category category) {
        return category == Category.TRUE;
    }

    @Override
    public Query optimizeQuery(Query q, Category category) {
        if (category == Category.TRUE) {
            final Column inputPhysicalColumn = inputColumn.getPhysicalColumn();

            final Object operand;
            if (compareColumn != null) {
                final Column physicalCompareColumn = compareColumn.getPhysicalColumn();
                operand = new SelectItem(physicalCompareColumn);
            } else {
                operand = toOperand(compareValue);
            }

            q.where(inputPhysicalColumn, operator.getOperatorType(), operand);
            return q;
        }
        throw new UnsupportedOperationException();
    }
}
