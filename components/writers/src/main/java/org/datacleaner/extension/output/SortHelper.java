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
package org.datacleaner.extension.output;

import java.util.Comparator;

import org.apache.metamodel.data.Row;
import org.apache.metamodel.util.NumberComparator;
import org.datacleaner.api.InputColumn;
import org.datacleaner.components.convert.ConvertToDateTransformer;
import org.datacleaner.components.convert.ConvertToNumberTransformer;
import org.datacleaner.util.CompareUtils;
import org.datacleaner.util.ReflectionUtils;

public class SortHelper {

    /**
     * Creates a comparator for the {@link InputColumn} based on its type
     * (sorting numerically or lexicographically).
     * 
     * @param column
     *            the column to sort on
     * @param columnIndex
     *            the index of the column in the input {@link Row}
     * @return the comparator instance
     */
    public static Comparator<Row> createComparator(InputColumn<?> column, final int columnIndex) {
        final Class<?> dataType = column.getDataType();
        final boolean isNumber = dataType != null && ReflectionUtils.isNumber(dataType);
        final boolean isDate = dataType != null && ReflectionUtils.isDate(dataType);

        return new Comparator<Row>() {
            @Override
            public int compare(Row row1, Row row2) {
                final Comparable<?> value1 = getComparableValue(row1, columnIndex, isNumber, isDate);
                final Comparable<?> value2 = getComparableValue(row2, columnIndex, isNumber, isDate);
                int comparableResult = CompareUtils.compareUnbound(value1, value2);
                if (comparableResult != 0) {
                    return comparableResult;
                } else {
                    // The values of the data at the row, and column to be
                    // sorted on are
                    // exactly the same. Now look at other values of all the
                    // columns to
                    // find if the two rows are same.
                    int numberOfSelectItems = row1.getSelectItems().length;
                    for (int i = 0; i < numberOfSelectItems; i++) {
                        final String rowValue1 = (String) row1.getValue(i);
                        final String rowValue2 = (String) row2.getValue(i);
                        final String rowValue1LowerCased = rowValue1.toLowerCase();
                        final String rowValue2LowerCased = rowValue2.toLowerCase();
                        if (CompareUtils.compare(rowValue1LowerCased, rowValue2LowerCased) == 0) {
                            continue;
                        } else {
                            return CompareUtils.compare(rowValue1LowerCased, rowValue2LowerCased);
                        }
                    }
                }

                return comparableResult;
            }
        };
    }

    private static Comparable<?> getComparableValue(Row row, int columnIndex, boolean isNumber, boolean isDate) {
        final String value = (String) row.getValue(columnIndex);
        if (isNumber) {
            final Number result = ConvertToNumberTransformer.transformValue(value);
            if (result instanceof Comparable) {
                return (Comparable<?>) result;
            }
            return NumberComparator.getComparable(result);
        }
        if (isDate) {
            return ConvertToDateTransformer.getInternalInstance().transformValue(value);
        }
        return value.toLowerCase();
    }
}
