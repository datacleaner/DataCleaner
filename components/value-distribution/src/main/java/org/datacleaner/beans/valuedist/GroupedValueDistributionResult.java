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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.datacleaner.api.InputColumn;
import org.datacleaner.result.AnnotatedRowsResult;
import org.datacleaner.result.GroupedValueCountingAnalyzerResult;
import org.datacleaner.result.ValueFrequency;
import org.datacleaner.result.ValueCountingAnalyzerResult;

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
@JsonAutoDetect(
        fieldVisibility= JsonAutoDetect.Visibility.ANY,
        getterVisibility= JsonAutoDetect.Visibility.NONE,
        isGetterVisibility=JsonAutoDetect.Visibility.NONE,
        setterVisibility=JsonAutoDetect.Visibility.NONE)
public class GroupedValueDistributionResult extends ValueDistributionAnalyzerResult implements
        GroupedValueCountingAnalyzerResult {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private final InputColumn<?> _column;
    @JsonIgnore
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
        return getSingleValueDistributionResult().getDistinctCount();
    }

    @Override
    public int getNullCount() {
        return getSingleValueDistributionResult().getNullCount();
    }

    @Override
    public int getTotalCount() {
        return getSingleValueDistributionResult().getTotalCount();
    }

    @Override
    public Integer getCount(String value) {
        return getSingleValueDistributionResult().getCount(value);
    }

    @Override
    public Integer getUniqueCount() {
        return getSingleValueDistributionResult().getUniqueCount();
    }

    @Override
    public Collection<ValueFrequency> getValueCounts() {
        return getSingleValueDistributionResult().getValueCounts();
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
        return getSingleValueDistributionResult().getAnnotatedRowsForValue(value);
    }

    @Override
    public AnnotatedRowsResult getAnnotatedRowsForNull() {
        return getSingleValueDistributionResult().getAnnotatedRowsForNull();
    }

    @Override
    public AnnotatedRowsResult getAnnotatedRowsForUnexpectedValues() {
        // not applicable
        return null;
    }

    @Override
    public Collection<String> getUniqueValues() {
        return getSingleValueDistributionResult().getUniqueValues();
    }

    @Override
    public boolean hasAnnotatedRows(String value) {
        return getSingleValueDistributionResult().hasAnnotatedRows(value);
    }

    @Override
    public Integer getUnexpectedValueCount() {
        // not applicable
        return null;
    }
}
