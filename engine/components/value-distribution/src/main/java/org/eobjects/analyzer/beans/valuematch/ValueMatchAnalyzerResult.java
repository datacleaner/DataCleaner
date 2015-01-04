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
package org.eobjects.analyzer.beans.valuematch;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.eobjects.analyzer.beans.api.Distributed;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.result.AbstractValueCountingAnalyzerResult;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.result.CompositeValueFrequency;
import org.eobjects.analyzer.result.Metric;
import org.eobjects.analyzer.result.SingleValueFrequency;
import org.eobjects.analyzer.result.ValueFrequency;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;
import org.eobjects.analyzer.util.LabelUtils;
import org.apache.metamodel.util.SerializableRef;

/**
 * Represents the result of the {@link ValueMatchAnalyzer}.
 */
@Distributed(reducer = ValueMatchAnalyzerResultReducer.class)
public class ValueMatchAnalyzerResult extends AbstractValueCountingAnalyzerResult {

    private static final long serialVersionUID = 1L;

    private final InputColumn<?> _column;
    private final Map<String, RowAnnotation> _valueAnnotations;
    private final RowAnnotation _nullAnnotation;
    private final RowAnnotation _nonMatchingValuesAnnotation;
    private final int _totalCount;
    private final SerializableRef<RowAnnotationFactory> _rowAnnotationFactoryRef;

    public ValueMatchAnalyzerResult(InputColumn<?> column, RowAnnotationFactory rowAnnotationFactory,
            Map<String, RowAnnotation> valueAnnotations, RowAnnotation nullAnnotation,
            RowAnnotation nonMatchingValuesAnnotation, int totalCount) {
        _column = column;
        _rowAnnotationFactoryRef = new SerializableRef<RowAnnotationFactory>(rowAnnotationFactory);
        _valueAnnotations = valueAnnotations;
        _nullAnnotation = nullAnnotation;
        _nonMatchingValuesAnnotation = nonMatchingValuesAnnotation;
        _totalCount = totalCount;
    }

    @Override
    public int getTotalCount() {
        return _totalCount;
    }

    @Metric("Null count")
    @Override
    public int getNullCount() {
        return _nullAnnotation.getRowCount();
    }

    @Override
    public Integer getCount(String value) {
        final RowAnnotation annotation = _valueAnnotations.get(value);
        if (annotation == null) {
            return null;
        }
        return annotation.getRowCount();
    }

    @Metric(value = "Unexpected value count")
    @Override
    public Integer getUnexpectedValueCount() {
        return _nonMatchingValuesAnnotation.getRowCount();
    }

    @Override
    public AnnotatedRowsResult getAnnotatedRowsForUnexpectedValues() {
        return new AnnotatedRowsResult(_nonMatchingValuesAnnotation, _rowAnnotationFactoryRef.get(), _column);
    }

    @Override
    public AnnotatedRowsResult getAnnotatedRowsForNull() {
        return new AnnotatedRowsResult(_nullAnnotation, _rowAnnotationFactoryRef.get(), _column);
    }

    @Override
    public AnnotatedRowsResult getAnnotatedRowsForValue(String value) {
        if (value == null) {
            return getAnnotatedRowsForNull();
        }
        if (LabelUtils.UNEXPECTED_LABEL.equals(value)) {
            return getAnnotatedRowsForUnexpectedValues();
        }
        if (_rowAnnotationFactoryRef.get() == null) {
            return null;
        }
        final RowAnnotation annotation = _valueAnnotations.get(value);
        if (annotation == null) {
            return null;
        }
        return new AnnotatedRowsResult(annotation, _rowAnnotationFactoryRef.get(), _column);
    }

    public InputColumn<?> getColumn() {
        return _column;
    }

    @Override
    public String getName() {
        // not applicable
        return _column.getName();
    }

    @Override
    public Collection<ValueFrequency> getValueCounts() {
        final Set<ValueFrequency> result = new TreeSet<ValueFrequency>();
        for (Entry<String, RowAnnotation> entry : _valueAnnotations.entrySet()) {
            result.add(new SingleValueFrequency(entry.getKey(), entry.getValue().getRowCount()));
        }
        final int nullCount = getNullCount();
        if (nullCount > 0) {
            result.add(new SingleValueFrequency(null, nullCount));
        }
        final Integer unexpectedCount = getUnexpectedValueCount();
        if (unexpectedCount > 0) {
            result.add(new CompositeValueFrequency(LabelUtils.UNEXPECTED_LABEL, unexpectedCount));
        }
        return result;
    }

    public Map<String, RowAnnotation> getExpectedValueAnnotations() {
        return _valueAnnotations;
    }

    @Override
    public Integer getDistinctCount() {
        // not applicable
        return null;
    }

    @Override
    public Integer getUniqueCount() {
        // not applicable
        return null;
    }

    @Override
    public Collection<String> getUniqueValues() {
        // not applicable
        return null;
    }

    @Override
    public boolean hasAnnotatedRows(String value) {
        if (_rowAnnotationFactoryRef.get() == null) {
            return false;
        }
        if (value == null) {
            return true;
        }
        if (LabelUtils.UNEXPECTED_LABEL.equals(value)) {
            return true;
        }
        return _valueAnnotations.containsKey(value);
    }
}
