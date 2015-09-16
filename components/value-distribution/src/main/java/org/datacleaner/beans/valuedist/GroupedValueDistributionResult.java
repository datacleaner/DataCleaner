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

import java.util.Collection;

import org.datacleaner.api.InputColumn;
import org.datacleaner.result.AnnotatedRowsResult;
import org.datacleaner.result.GroupedValueCountingAnalyzerResult;
import org.datacleaner.result.ValueCountingAnalyzerResult;
import org.datacleaner.result.ValueFrequency;

/**
 * Represents the result of the {@link ValueDistributionAnalyzer}.
 * 
 * A value distribution result has two basic forms: Grouped or ungrouped. To
 * find out which type a particular instance has, use the
 * {@link #isGroupingEnabled()} method.
 * 
 * Ungrouped results only contain a single/global value distribution. A grouped
 * result contain multiple value distributions, based on groups.
 * 
 * 
 */
public class GroupedValueDistributionResult extends ValueDistributionAnalyzerResult implements
        GroupedValueCountingAnalyzerResult {

    private static final long serialVersionUID = 1L;

    private final InputColumn<?> _column;
    private final InputColumn<String> _groupColumn;
    private final Collection<? extends ValueCountingAnalyzerResult> _result;

    public GroupedValueDistributionResult(InputColumn<?> column, InputColumn<String> groupColumn,
            Collection<? extends ValueCountingAnalyzerResult> groupedResult) {
        _column = column;
        _groupColumn = groupColumn;
        _result = groupedResult;
    }

    @Override
    public String getGroupDiscriminatorName() {
        if (_groupColumn == null) {
            return null;
        }
        return _groupColumn.getName();
    }

    @Override
    public Integer getDistinctCount() {
        // This operation is not supported on GroupValueDistributionResult, but
        // we don't want to throw exceptions...
        return -1;
    }

    @Override
    public int getNullCount() {
        // This operation is not supported on GroupValueDistributionResult, but
        // we don't want to throw exceptions...
        return -1;
    }

    @Override
    public int getTotalCount() {
        // This operation is not supported on GroupValueDistributionResult, but
        // we don't want to throw exceptions...
        return -1;
    }

    @Override
    public Integer getCount(String value) {
        // This operation is not supported on GroupValueDistributionResult, but
        // we don't want to throw exceptions...
        return -1;
    }

    @Override
    public Integer getUniqueCount() {
        // This operation is not supported on GroupValueDistributionResult, but
        // we don't want to throw exceptions...
        return -1;
    }

    @Override
    public Collection<ValueFrequency> getValueCounts() {
        // This operation is not supported on GroupValueDistributionResult, but
        // we don't want to throw exceptions...
        return null;
    }

    @Override
    public Collection<? extends ValueCountingAnalyzerResult> getGroupResults() {
        return _result;
    }

    public ValueCountingAnalyzerResult getSingleValueDistributionResult() {
        return _result.iterator().next();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Value distribution for column: ");
        sb.append(_column.getName());
        for (ValueCountingAnalyzerResult groupResult : getGroupResults()) {
            if (getGroupDiscriminatorName() != null) {
                sb.append("\n");
                sb.append("\nGroup: ");
            }
            sb.append(groupResult.getName());

            appendToString(sb, groupResult, 4);
        }
        return sb.toString();
    }

    @Override
    public String getName() {
        return _column.getName();
    }

    @Override
    public AnnotatedRowsResult getAnnotatedRowsForValue(String value) {
        // This operation is not supported on GroupValueDistributionResult, but
        // we don't want to throw exceptions...
        return null;
    }

    @Override
    public AnnotatedRowsResult getAnnotatedRowsForNull() {
        // This operation is not supported on GroupValueDistributionResult, but
        // we don't want to throw exceptions...
        return null;
    }

    @Override
    public AnnotatedRowsResult getAnnotatedRowsForUnexpectedValues() {
        // not applicable
        return null;
    }

    @Override
    public Collection<String> getUniqueValues() {
        // This operation is not supported on GroupValueDistributionResult, but
        // we don't want to throw exceptions...
        return null;
    }

    @Override
    public Boolean hasAnnotatedRows(String value) {
        // This operation is not supported on GroupValueDistributionResult, but
        // we don't want to throw exceptions...
        return null; 
    }

    @Override
    public Integer getUnexpectedValueCount() {
        // not applicable
        return null;
    }
    
    public InputColumn<?> getColumn() {
        return _column;
    }
    
    public InputColumn<String> getGroupColumn() {
        return _groupColumn;
    }
}
