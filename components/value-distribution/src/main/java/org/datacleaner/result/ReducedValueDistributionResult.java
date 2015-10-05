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
package org.datacleaner.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.datacleaner.beans.valuedist.ValueDistributionAnalyzerResult;
import org.datacleaner.beans.valuedist.ValueDistributionAnalyzerResultReducer;

/**
 * A simple and light-weight implementation of
 * {@link ValueDistributionAnalyzerResult} produced by the
 * {@link ValueDistributionAnalyzerResultReducer}.
 */
public class ReducedValueDistributionResult extends ValueDistributionAnalyzerResult {

    private static final long serialVersionUID = 1L;

    private final String _name;
    private final Map<String, Integer> _valueCountsMap;
    private final int _nullCount;

    public ReducedValueDistributionResult(final String name, Map<String, Integer> valueCountsMap, int nullCount) {
        _name = name;
        _valueCountsMap = valueCountsMap;
        _nullCount = nullCount;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public Collection<ValueFrequency> getValueCounts() {
        List<ValueFrequency> valueCounts = new ArrayList<>(); 
        for (Map.Entry<String, Integer> valueCount : _valueCountsMap.entrySet()) {
            ValueFrequency valueFrequency = new SingleValueFrequency(valueCount.getKey(), valueCount.getValue());
            valueCounts.add(valueFrequency);
        }
        return valueCounts;
    }

    @Override
    public Integer getCount(String value) {
        Integer result = _valueCountsMap.get(value);
        if (result == null) {
            return 0;
        }
        return result;
    }

    @Override
    public Integer getUnexpectedValueCount() {
        return 0;
    }

    @Override
    public boolean hasAnnotatedRows(String value) {
        return false;
    }

    @Override
    public AnnotatedRowsResult getAnnotatedRowsForValue(String value) {
        return null;
    }

    @Override
    public AnnotatedRowsResult getAnnotatedRowsForNull() {
        return null;
    }

    @Override
    public AnnotatedRowsResult getAnnotatedRowsForUnexpectedValues() {
        return null;
    }

    @Override
    public Collection<String> getUniqueValues() {
        final Collection<String> result = new ArrayList<>();
        final Set<Entry<String, Integer>> entries = _valueCountsMap.entrySet();
        for (Entry<String, Integer> entry : entries) {
            if (entry.getValue().intValue() == 1) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    @Override
    public int getTotalCount() {
        int sum = 0;
        final Set<Entry<String, Integer>> entries = _valueCountsMap.entrySet();
        for (Entry<String, Integer> entry : entries) {
            sum = sum + entry.getValue().intValue();
        }
        return sum + _nullCount;
    }

    @Override
    public int getNullCount() {
        return _nullCount;
    }

    @Override
    public Integer getUniqueCount() {
        int sum = 0;
        final Set<Entry<String, Integer>> entries = _valueCountsMap.entrySet();
        for (Entry<String, Integer> entry : entries) {
            if (entry.getValue().intValue() == 1) {
                sum++;
            }
        }
        return sum;
    }

    @Override
    public Integer getDistinctCount() {
        if (_nullCount == 0) {
            return _valueCountsMap.size();
        }
        return _valueCountsMap.size() + 1;
    }

    public ValueCountList getTopValues() {
        ValueCountListImpl valueCountList = ValueCountListImpl.createTopList(2);
        for (ValueFrequency valueFrequency : getValueCounts()) {
            valueCountList.register(valueFrequency);
        }
        return valueCountList;
    }

}
