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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.datacleaner.api.InputColumn;
import org.datacleaner.result.AnnotatedRowsResult;
import org.datacleaner.result.CompositeValueFrequency;
import org.datacleaner.result.SingleValueFrequency;
import org.datacleaner.result.ValueFrequency;
import org.datacleaner.result.ValueCountList;
import org.datacleaner.result.ValueCountListImpl;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.NullTolerableComparator;
import org.apache.metamodel.util.Ref;
import org.apache.metamodel.util.SerializableRef;

public class SingleValueDistributionResult extends ValueDistributionAnalyzerResult implements
        Comparable<SingleValueDistributionResult> {

    private static final long serialVersionUID = 1L;

    private final ValueCountList _topValues;
    private final ValueCountList _bottomValues;
    private final Collection<String> _uniqueValues;
    private final Map<String, RowAnnotation> _annotations;
    private final RowAnnotation _nullValueAnnotation;
    private final InputColumn<?>[] _highlightedColumns;
    private final int _uniqueValueCount;
    private final String _groupName;
    private final int _nullCount;
    private final int _totalCount;
    private final int _distinctCount;
    private final Ref<RowAnnotationFactory> _annotationFactoryRef;

    public SingleValueDistributionResult(String groupName, ValueCountList topValues, ValueCountList bottomValues,
            Collection<String> uniqueValues, int uniqueValueCount, int distinctCount, int totalCount,
            Map<String, RowAnnotation> annotations, RowAnnotation nullValueAnnotation,
            RowAnnotationFactory annotationFactory, InputColumn<?>[] highlightedColumns) {
        _groupName = groupName;
        _topValues = topValues;
        _bottomValues = bottomValues;
        _uniqueValues = uniqueValues;
        _uniqueValueCount = uniqueValueCount;
        _totalCount = totalCount;
        _distinctCount = distinctCount;
        _nullValueAnnotation = nullValueAnnotation;
        _annotations = annotations;
        _annotationFactoryRef = new SerializableRef<RowAnnotationFactory>(annotationFactory);
        _highlightedColumns = highlightedColumns;
        _nullCount = 0;
    }

    public SingleValueDistributionResult(String groupName, ValueCountList topValues, int uniqueValueCount,
            int distinctCount, int totalCount, Map<String, RowAnnotation> annotations,
            RowAnnotation nullValueAnnotation, RowAnnotationFactory annotationFactory,
            InputColumn<?>[] highlightedColumns) {
        this(groupName, topValues, null, null, uniqueValueCount, distinctCount, totalCount, annotations,
                nullValueAnnotation, annotationFactory, highlightedColumns);
    }

    public SingleValueDistributionResult(String groupName, ValueCountList topValues, Collection<String> uniqueValues,
            int uniqueValueCount, int distinctCount, int totalCount, Map<String, RowAnnotation> annotations,
            RowAnnotation nullValueAnnotation, RowAnnotationFactory annotationFactory,
            InputColumn<?>[] highlightedColumns) {
        this(groupName, topValues, null, uniqueValues, uniqueValueCount, distinctCount, totalCount, annotations,
                nullValueAnnotation, annotationFactory, highlightedColumns);
    }

    public SingleValueDistributionResult(String groupName, ValueCountList topValues, ValueCountList bottomValues,
            int uniqueValueCount, int distinctCount, int totalCount, Map<String, RowAnnotation> annotations,
            RowAnnotation nullValueAnnotation, RowAnnotationFactory annotationFactory,
            InputColumn<?>[] highlightedColumns) {
        this(groupName, topValues, bottomValues, null, uniqueValueCount, distinctCount, totalCount, annotations,
                nullValueAnnotation, annotationFactory, highlightedColumns);
    }

    @Override
    public Boolean hasAnnotatedRows(String value) {
        if (_annotations == null) {
            return false;
        }
        final RowAnnotationFactory annotationFactory = _annotationFactoryRef.get();
        if (annotationFactory == null) {
            return false;
        }

        if (value == null) {
            return _nullValueAnnotation != null;
        }
        final RowAnnotation annotation = _annotations.get(value);
        if (annotation == null) {
            return false;
        }
        
        return annotationFactory.hasSampleRows(annotation);
    }

    @Override
    public AnnotatedRowsResult getAnnotatedRowsForValue(String value) {
        if (_annotations == null) {
            return null;
        }
        final RowAnnotationFactory annotationFactory = _annotationFactoryRef.get();
        if (annotationFactory == null) {
            return null;
        }

        final RowAnnotation annotation;
        if (value == null) {
            annotation = _nullValueAnnotation;
        } else {
            annotation = _annotations.get(value);
        }

        if (annotation == null) {
            return null;
        }

        return new AnnotatedRowsResult(annotation, annotationFactory, _highlightedColumns);
    }

    @Override
    public AnnotatedRowsResult getAnnotatedRowsForNull() {
        if (_nullValueAnnotation == null) {
            return null;
        }

        final RowAnnotationFactory annotationFactory = _annotationFactoryRef.get();
        if (annotationFactory == null) {
            return null;
        }

        return new AnnotatedRowsResult(_nullValueAnnotation, annotationFactory, _highlightedColumns);
    }

    public ValueCountList getTopValues() {
        if (_topValues == null) {
            return ValueCountListImpl.emptyList();
        }
        return _topValues;
    }

    public ValueCountList getBottomValues() {
        if (_bottomValues == null) {
            return ValueCountListImpl.emptyList();
        }
        return _bottomValues;
    }
    
    public InputColumn<?>[] getHighlightedColumns() {
        return _highlightedColumns;
    }

    @Override
    public int getNullCount() {
        if (_nullValueAnnotation == null) {
            return _nullCount;
        }
        return _nullValueAnnotation.getRowCount();
    }

    public boolean isUniqueValuesAvailable() {
        return _uniqueValues != null;
    }

    @Override
    public Integer getUniqueCount() {
        if (_uniqueValues == null) {
            return _uniqueValueCount;
        }
        return _uniqueValues.size();
    }

    @Override
    public Collection<String> getUniqueValues() {
        if (_uniqueValues == null) {
            return Collections.emptyList();
        }
        return _uniqueValues;
    }

    @Override
    public String getName() {
        return _groupName;
    }

    @Override
    public Integer getCount(final String value) {
        if (value == null) {
            return getNullCount();
        }

        if (_topValues != null) {
            List<ValueFrequency> valueCounts = _topValues.getValueCounts();
            for (ValueFrequency valueCount : valueCounts) {
                if (value.equals(valueCount.getValue())) {
                    return valueCount.getCount();
                }
            }
        }

        if (_bottomValues != null) {
            List<ValueFrequency> valueCounts = _bottomValues.getValueCounts();
            for (ValueFrequency valueCount : valueCounts) {
                if (value.equals(valueCount.getValue())) {
                    return valueCount.getCount();
                }
            }
        }

        if (_uniqueValues != null) {
            if (_uniqueValues.contains(value)) {
                return 1;
            }
        }

        return null;
    }

    public Integer getDistinctCount() {
        return _distinctCount;
    }

    @Override
    public int getTotalCount() {
        return _totalCount;
    }

    @Override
    public int hashCode() {
        if (_groupName == null) {
            return -1;
        }
        return _groupName.hashCode();
    }

    @Override
    public Collection<ValueFrequency> getValueCounts() {
        Collection<ValueFrequency> result = new TreeSet<ValueFrequency>();
        if (_topValues != null) {
            result.addAll(_topValues.getValueCounts());
        }
        if (_bottomValues != null) {
            result.addAll(_bottomValues.getValueCounts());
        }
        final int nullCount = getNullCount();
        if (nullCount > 0) {
            result.add(new SingleValueFrequency(null, nullCount));
        }
        if (_uniqueValues != null && !_uniqueValues.isEmpty()) {
            result.add(new CompositeValueFrequency(LabelUtils.UNIQUE_LABEL, _uniqueValues, 1));
        } else if (_uniqueValueCount > 0) {
            result.add(new CompositeValueFrequency(LabelUtils.UNIQUE_LABEL, _uniqueValueCount));
        }
        return result;
    }

    @Override
    public AnnotatedRowsResult getAnnotatedRowsForUnexpectedValues() {
        // not applicable
        return null;
    }

    @Override
    public int compareTo(SingleValueDistributionResult o) {
        return NullTolerableComparator.get(String.class).compare(getName(), o.getName());
    }

    @Override
    public Integer getUnexpectedValueCount() {
        // not applicable
        return null;
    }
}
