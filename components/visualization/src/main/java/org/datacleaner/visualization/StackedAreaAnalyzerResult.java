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
package org.datacleaner.visualization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.util.ReflectionUtils;

public class StackedAreaAnalyzerResult implements AnalyzerResult {

    private static final long serialVersionUID = 1L;
    private final InputColumn<?> _categoryColumn;
    private final InputColumn<Number>[] _measureColumns;
    private final Map<Object, List<Number>> _measureMap = new HashMap<>();

    public StackedAreaAnalyzerResult(InputColumn<?> categoryColumn, InputColumn<Number>[] measureColumns) {
        _categoryColumn = categoryColumn;
        _measureColumns = measureColumns;
    }

    public boolean isTimeCategory() {
        return ReflectionUtils.isDate(_categoryColumn.getDataType());
    }

    public boolean isNumberCategory() {
        return ReflectionUtils.isNumber(_categoryColumn.getDataType());
    }

    public InputColumn<?> getCategoryColumn() {
        return _categoryColumn;
    }

    public int getCategoryCount() {
        return _measureMap.size();
    }

    public List<?> getCategories() {
        final Stream<?> stream = _measureMap.keySet().stream();
        final List<?> categories = stream.sorted().collect(Collectors.toList());
        return categories;
    }

    public InputColumn<Number>[] getMeasureColumns() {
        return _measureColumns;
    }

    public List<Number> getMeasures(Object category) {
        return _measureMap.get(category);
    }

    public void addMeasures(Object category, List<Number> measures) {
        final List<Number> oldMeasures = _measureMap.get(category);
        if (oldMeasures != null) {
            for (int i = 0; i < oldMeasures.size(); i++) {
                final Number oldValue = oldMeasures.get(i);
                final Number addition = measures.get(i);
                oldMeasures.remove(i);
                oldMeasures.add(i, sum(oldValue, addition));
            }
        } else {
            _measureMap.put(category, measures);
        }
    }

    public Number sum(Number x, Number y) {
        if (x == null) {
            return y;
        }
        if (y == null) {
            return x;
        }
        return x.doubleValue() + y.doubleValue();
    }
}
