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
package org.datacleaner.beans;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Named;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.Provided;
import org.datacleaner.result.AnnotatedRowsResult;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabDimension;
import org.datacleaner.result.CrosstabNavigator;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;
import org.datacleaner.storage.RowAnnotations;
import org.datacleaner.util.ValueCombination;

@Named("Boolean analyzer")
@Description("Inspect your boolean values. How is the distribution of true/false? Are there null values?")
public class BooleanAnalyzer implements Analyzer<BooleanAnalyzerResult> {

    public static final String MEASURE_LEAST_FREQUENT = "Least frequent";
    public static final String MEASURE_MOST_FREQUENT = "Most frequent";
    public static final String VALUE_COMBINATION_COLUMN_FREQUENCY = "Frequency";
    public static final String MEASURE_FALSE_COUNT = "False count";
    public static final String MEASURE_TRUE_COUNT = "True count";
    public static final String MEASURE_NULL_COUNT = "Null count";
    public static final String MEASURE_ROW_COUNT = "Row count";
    public static final String DIMENSION_COLUMN = "Column";
    public static final String DIMENSION_MEASURE = "Measure";
    public static final String DIMENSION_COMBINATION_PREFIX = "Combination ";

    // comparator used to sort entries, getting the most frequent value
    // combinations to the top
    @SuppressWarnings("checkstyle:Indentation")
    private static final Comparator<Map.Entry<ValueCombination<Boolean>, RowAnnotation>>
            frequentValueCombinationComparator = (o1, o2) -> {
        int result = o2.getValue().getRowCount() - o1.getValue().getRowCount();
        if (result == 0) {
            result = o2.getKey().compareTo(o1.getKey());
        }
        return result;
    };

    private final Map<InputColumn<Boolean>, BooleanAnalyzerColumnDelegate> _columnDelegates = new HashMap<>();
    private final Map<ValueCombination<Boolean>, RowAnnotation> _valueCombinations = new HashMap<>();

    @Configured
    InputColumn<Boolean>[] _columns;

    @Provided
    RowAnnotationFactory _annotationFactory;

    public BooleanAnalyzer(final InputColumn<Boolean>[] columns) {
        _columns = columns;
        _annotationFactory = RowAnnotations.getDefaultFactory();
    }

    public BooleanAnalyzer() {
    }

    @Initialize
    public void init() {
        for (final InputColumn<Boolean> col : _columns) {
            _columnDelegates.put(col, new BooleanAnalyzerColumnDelegate(_annotationFactory));
        }
    }

    @Override
    public void run(final InputRow row, final int distinctCount) {
        final Boolean[] values = new Boolean[_columns.length];
        for (int i = 0; i < values.length; i++) {
            final InputColumn<Boolean> col = _columns[i];
            final Boolean value = row.getValue(col);
            final BooleanAnalyzerColumnDelegate delegate = _columnDelegates.get(col);
            values[i] = value;
            delegate.run(value, row, distinctCount);
        }

        // collect all combinations of booleans
        if (_columns.length > 1) {
            final ValueCombination<Boolean> valueCombination = new ValueCombination<>(values);
            RowAnnotation annotation = _valueCombinations.get(valueCombination);
            if (annotation == null) {
                annotation = _annotationFactory.createAnnotation();
                _valueCombinations.put(valueCombination, annotation);
            }
            _annotationFactory.annotate(row, distinctCount, annotation);
        }
    }

    @Override
    public BooleanAnalyzerResult getResult() {
        CrosstabDimension measureDimension = new CrosstabDimension(DIMENSION_MEASURE);
        measureDimension.addCategory(MEASURE_ROW_COUNT);
        measureDimension.addCategory(MEASURE_NULL_COUNT);
        measureDimension.addCategory(MEASURE_TRUE_COUNT);
        measureDimension.addCategory(MEASURE_FALSE_COUNT);

        CrosstabDimension columnDimension = new CrosstabDimension(DIMENSION_COLUMN);
        for (final InputColumn<Boolean> column : _columns) {
            columnDimension.addCategory(column.getName());
        }

        final Crosstab<Number> crosstab = new Crosstab<>(Number.class, columnDimension, measureDimension);
        for (final InputColumn<Boolean> column : _columns) {
            final CrosstabNavigator<Number> nav = crosstab.navigate().where(columnDimension, column.getName());
            final BooleanAnalyzerColumnDelegate delegate = _columnDelegates.get(column);

            nav.where(measureDimension, MEASURE_ROW_COUNT).put(delegate.getRowCount());

            final int nullCount = delegate.getNullCount();
            nav.where(measureDimension, MEASURE_NULL_COUNT).put(nullCount);
            if (nullCount > 0) {
                nav.attach(new AnnotatedRowsResult(delegate.getNullAnnotation(), _annotationFactory, column));
            }

            RowAnnotation annotation = delegate.getTrueAnnotation();
            nav.where(measureDimension, MEASURE_TRUE_COUNT).put(annotation.getRowCount());
            if (annotation.getRowCount() > 0) {
                nav.attach(new AnnotatedRowsResult(annotation, _annotationFactory, column));
            }

            annotation = delegate.getFalseAnnotation();
            nav.where(measureDimension, MEASURE_FALSE_COUNT).put(annotation.getRowCount());
            if (annotation.getRowCount() > 0) {
                nav.attach(new AnnotatedRowsResult(annotation, _annotationFactory, column));
            }
        }

        final Crosstab<Number> valueCombinationCrosstab;

        if (_columns.length > 1) {
            measureDimension = new CrosstabDimension(DIMENSION_MEASURE);

            columnDimension = new CrosstabDimension(DIMENSION_COLUMN);
            for (final InputColumn<Boolean> column : _columns) {
                columnDimension.addCategory(column.getName());
            }
            columnDimension.addCategory(VALUE_COMBINATION_COLUMN_FREQUENCY);

            valueCombinationCrosstab = new Crosstab<>(Number.class, columnDimension, measureDimension);

            final SortedSet<Entry<ValueCombination<Boolean>, RowAnnotation>> entries =
                    new TreeSet<>(frequentValueCombinationComparator);
            entries.addAll(_valueCombinations.entrySet());

            int row = 0;
            for (final Entry<ValueCombination<Boolean>, RowAnnotation> entry : entries) {

                final String measureName;
                if (row == 0) {
                    measureName = MEASURE_MOST_FREQUENT;
                } else if (row + 1 == entries.size()) {
                    measureName = MEASURE_LEAST_FREQUENT;
                } else {
                    measureName = DIMENSION_COMBINATION_PREFIX + row;
                }
                measureDimension.addCategory(measureName);

                final CrosstabNavigator<Number> nav = valueCombinationCrosstab.where(measureDimension, measureName);

                final ValueCombination<Boolean> valueCombination = entry.getKey();
                final RowAnnotation annotation = entry.getValue();

                nav.where(columnDimension, VALUE_COMBINATION_COLUMN_FREQUENCY);
                nav.put(annotation.getRowCount());
                nav.attach(new AnnotatedRowsResult(annotation, _annotationFactory, _columns));

                for (int i = 0; i < valueCombination.getValueCount(); i++) {
                    final InputColumn<Boolean> column = _columns[i];
                    final Boolean value = valueCombination.getValueAt(i);
                    Byte numberValue = null;
                    if (value != null) {
                        if (value) {
                            numberValue = 1;
                        } else {
                            numberValue = 0;
                        }
                    }

                    nav.where(columnDimension, column.getName());
                    nav.put(numberValue);
                }

                row++;
            }

        } else {
            valueCombinationCrosstab = null;
        }

        return new BooleanAnalyzerResult(crosstab, valueCombinationCrosstab);
    }

}
