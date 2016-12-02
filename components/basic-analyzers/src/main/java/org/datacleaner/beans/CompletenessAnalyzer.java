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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.metamodel.query.Query;
import org.apache.metamodel.util.HasName;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.HasOutputDataStreams;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.MappedProperty;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.api.OutputRowCollector;
import org.datacleaner.job.output.OutputDataStreamBuilder;
import org.datacleaner.job.output.OutputDataStreams;
import org.datacleaner.storage.InMemoryRowAnnotationFactory2;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;

import com.google.common.base.Strings;

@Named("Completeness analyzer")
@Description("Asserts the completeness of your data by ensuring that all required fields are filled.")
public class CompletenessAnalyzer implements Analyzer<CompletenessAnalyzerResult>, HasOutputDataStreams {
    public enum Condition implements HasName {

        NOT_BLANK_OR_NULL("Not <blank> or <null>", Condition::isNotNullOrEmpty),

        NOT_NULL("Not <null>", e -> (e != null)),

        NULL("<null> required", e -> (e == null)),

        BLANK_OR_NULL("<blank> or <null> required", Condition::isNullOrEmpty);

        private final String _name;
        private final Predicate<Object> _predicate;

        Condition(final String name, final Predicate<Object> predicate) {
            _name = name;
            _predicate = predicate;
        }

        private static boolean isNotNullOrEmpty(final Object value) {
            return !isNullOrEmpty(value);
        }

        private static boolean isNullOrEmpty(final Object value) {
            if (value instanceof String) {
                return Strings.isNullOrEmpty((String) value);
            }
            return value == null;
        }

        @Override
        public String getName() {
            return _name;
        }

        public boolean isValid(final Object argument) {
            return _predicate.test(argument);
        }
    }

    public enum EvaluationMode implements HasName {
        ALL_FIELDS("When all fields are incomplete, the record is incomplete"),
        ANY_FIELD("When any field is incomplete, the record is incomplete");

        private final String _name;

        EvaluationMode(final String name) {
            _name = name;
        }

        @Override
        public String getName() {
            return _name;
        }
    }

    public static final String OUTPUT_STREAM_COMPLETE = "Complete rows";
    public static final String OUTPUT_STREAM_INCOMPLETE = "Incomplete rows";
    public static final String PROPERTY_VALUES = "Values";
    public static final String PROPERTY_CONDITIONS = "Conditions";
    public static final String PROPERTY_EVALUATION_MODE = "Evaluation mode";
    public static final String PROPERTY_ADDITIONAL_OUTPUT_VALUES = "Additional output values";
    // Do not inject the shared RowAnnotations, available rows are always
    // needed.
    private final RowAnnotationFactory _annotationFactory = new InMemoryRowAnnotationFactory2();
    private final RowAnnotation _invalidRecords = _annotationFactory.createAnnotation();
    private final AtomicInteger _rowCount = new AtomicInteger();
    @Inject
    @Configured(order = 1, value = PROPERTY_VALUES)
    @Description("Values to check for completeness")
    InputColumn<?>[] _valueColumns;
    @Inject
    @Configured(order = 2, value = PROPERTY_CONDITIONS)
    @Description("The conditions of which a value is determined to be filled or not")
    @MappedProperty(PROPERTY_VALUES)
    Condition[] _conditions;
    @Inject
    @Configured(order = 3, value = PROPERTY_EVALUATION_MODE)
    EvaluationMode _evaluationMode = EvaluationMode.ANY_FIELD;
    @Inject
    @Configured(order = 100, value = PROPERTY_ADDITIONAL_OUTPUT_VALUES, required = false)
    @Description("Optional additional values to add to output data streams")
    InputColumn<?>[] _additionalOutputValueColumns;
    private OutputRowCollector _completeRowCollector;
    private OutputRowCollector _incompleteRowCollector;
    private List<InputColumn<?>> _outputDataStreamColumns;

    @Initialize
    public void init() {
        _rowCount.set(0);
        _outputDataStreamColumns = createOutputDataStreamColumns();
    }

    private List<InputColumn<?>> createOutputDataStreamColumns() {
        final List<InputColumn<?>> outputDataStreamColumns = new ArrayList<>();
        Collections.addAll(outputDataStreamColumns, _valueColumns);
        if (_additionalOutputValueColumns != null) {
            for (final InputColumn<?> inputColumn : _additionalOutputValueColumns) {
                if (!outputDataStreamColumns.contains(inputColumn)) {
                    outputDataStreamColumns.add(inputColumn);
                }
            }
        }
        return outputDataStreamColumns;
    }

    @Override
    public void run(final InputRow row, final int distinctCount) {
        _rowCount.addAndGet(distinctCount);
        boolean allInvalid = true;
        for (int i = 0; i < _valueColumns.length; i++) {
            final Object value = row.getValue(_valueColumns[i]);
            final boolean valid = _conditions[i].isValid(value);
            if (_evaluationMode == EvaluationMode.ANY_FIELD && !valid) {
                _annotationFactory.annotate(row, distinctCount, _invalidRecords);
                if (_incompleteRowCollector != null) {
                    _incompleteRowCollector.putValues(row.getValues(_outputDataStreamColumns).toArray());
                }
                return;
            }

            if (valid) {
                allInvalid = false;
            }
        }
        if (_evaluationMode == EvaluationMode.ALL_FIELDS && allInvalid) {
            _annotationFactory.annotate(row, distinctCount, _invalidRecords);
            if (_incompleteRowCollector != null) {
                _incompleteRowCollector.putValues(row.getValues(_outputDataStreamColumns).toArray());
            }
            return;
        }

        if (_completeRowCollector != null) {
            _completeRowCollector.putValues(row.getValues(_outputDataStreamColumns).toArray());
        }
    }

    @Override
    public CompletenessAnalyzerResult getResult() {
        return new CompletenessAnalyzerResult(_rowCount.get(), _invalidRecords, _annotationFactory, _valueColumns);
    }

    public void setConditions(final Condition[] conditions) {
        _conditions = conditions;
    }

    public void setValueColumns(final InputColumn<?>[] valueColumns) {
        _valueColumns = valueColumns;
    }

    /**
     * Shortcut method to fill all conditions (of existing columns) to a single
     * condition.
     *
     * @param condition
     */
    public void fillAllConditions(final Condition condition) {
        if (_valueColumns != null) {
            final Condition[] conditions = new Condition[_valueColumns.length];
            Arrays.fill(conditions, condition);
            _conditions = conditions;
        }
    }

    @Override
    public OutputDataStream[] getOutputDataStreams() {
        final OutputDataStreamBuilder completeStreamBuilder = OutputDataStreams.pushDataStream(OUTPUT_STREAM_COMPLETE);
        final OutputDataStreamBuilder incompleteStreamBuilder =
                OutputDataStreams.pushDataStream(OUTPUT_STREAM_INCOMPLETE);

        for (final InputColumn<?> column : createOutputDataStreamColumns()) {
            completeStreamBuilder.withColumnLike(column);
            incompleteStreamBuilder.withColumnLike(column);
        }

        return new OutputDataStream[] { completeStreamBuilder.toOutputDataStream(),
                incompleteStreamBuilder.toOutputDataStream() };
    }

    @Override
    public void initializeOutputDataStream(final OutputDataStream outputDataStream, final Query query,
            final OutputRowCollector outputRowCollector) {
        if (outputDataStream.getName().equals(OUTPUT_STREAM_COMPLETE)) {
            _completeRowCollector = outputRowCollector;
        } else {
            _incompleteRowCollector = outputRowCollector;
        }
    }
}
