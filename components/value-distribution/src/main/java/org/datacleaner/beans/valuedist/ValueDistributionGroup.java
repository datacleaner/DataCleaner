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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.result.SingleValueFrequency;
import org.datacleaner.result.ValueCountListImpl;
import org.datacleaner.result.ValueFrequency;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;
import org.datacleaner.storage.RowAnnotationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a value distribution within a {@link ValueDistributionAnalyzer}. A
 * {@link ValueDistributionGroup} contains the counted values within a single
 * group.
 * 
 * 
 */
class ValueDistributionGroup {

    private static final Logger logger = LoggerFactory.getLogger(ValueDistributionGroup.class);

    private final Map<String, RowAnnotation> _annotationMap;
    private final RowAnnotation _nullValueAnnotation;
    private final RowAnnotationFactory _annotationFactory;
    private final String _groupName;
    private final boolean _recordAnnotations;
    private final InputColumn<?>[] _inputColumns;
    private final AtomicInteger _totalCount;

    public ValueDistributionGroup(String groupName, RowAnnotationFactory annotationFactory, boolean recordAnnotations,
            InputColumn<?>[] inputColumns) {
        _groupName = groupName;
        _annotationFactory = annotationFactory;
        _recordAnnotations = recordAnnotations;
        _inputColumns = inputColumns;
        _totalCount = new AtomicInteger();
        _annotationMap = new HashMap<String, RowAnnotation>();
        if (recordAnnotations) {
            _nullValueAnnotation = _annotationFactory.createAnnotation();
        } else {
            _nullValueAnnotation = new RowAnnotationImpl();
        }
    }

    public void run(InputRow row, String value, int distinctCount) {
        if (value == null) {
            if (_recordAnnotations) {
                _annotationFactory.annotate(row, distinctCount, _nullValueAnnotation);
            } else {
                ((RowAnnotationImpl) _nullValueAnnotation).incrementRowCount(distinctCount);
            }
        } else {
            RowAnnotation annotation;
            synchronized (this) {
                annotation = _annotationMap.get(value);
                if (annotation == null) {
                    if (_recordAnnotations) {
                        annotation = _annotationFactory.createAnnotation();
                    } else {
                        annotation = new RowAnnotationImpl();
                    }
                    _annotationMap.put(value, annotation);
                }
            }

            if (_recordAnnotations) {
                _annotationFactory.annotate(row, distinctCount, annotation);
            } else {
                ((RowAnnotationImpl) annotation).incrementRowCount(distinctCount);
            }
        }
        _totalCount.addAndGet(distinctCount);
    }

    public SingleValueDistributionResult createResult(boolean recordUniqueValues) {
        final ValueCountListImpl topValues = ValueCountListImpl.createFullList();

        final List<String> uniqueValues;
        if (recordUniqueValues) {
            uniqueValues = new ArrayList<String>();
        } else {
            uniqueValues = null;
        }

        int uniqueCount = 0;
        final int entryCount = _annotationMap.size();
        final Set<Entry<String, RowAnnotation>> entrySet = _annotationMap.entrySet();

        int i = 0;
        for (Entry<String, RowAnnotation> entry : entrySet) {
            if (i % 100000 == 0 && i != 0) {
                logger.info("Processing unique value entry no. {}", i);
            }
            final String value = entry.getKey();
            final RowAnnotation annotation = entry.getValue();
            final int count = annotation.getRowCount();
            uniqueCount = countValue(recordUniqueValues, topValues, uniqueValues, uniqueCount, value, count);
            i++;
        }

        final int distinctCount;
        if (_nullValueAnnotation.getRowCount() > 0) {
            distinctCount = 1 + entryCount;
        } else {
            distinctCount = entryCount;
        }

        if (recordUniqueValues) {
            return new SingleValueDistributionResult(_groupName, topValues, uniqueValues, uniqueCount, distinctCount,
                    _totalCount.get(), _annotationMap, _nullValueAnnotation, _annotationFactory, _inputColumns);
        } else {
            return new SingleValueDistributionResult(_groupName, topValues, uniqueCount, distinctCount,
                    _totalCount.get(), _annotationMap, _nullValueAnnotation, _annotationFactory, _inputColumns);
        }
    }

    private int countValue(boolean recordUniqueValues, ValueCountListImpl valueCountList,
            final List<String> uniqueValues, int uniqueCount, final String value, final int count) {
        if (count == 1) {
            if (recordUniqueValues) {
                uniqueValues.add(value);
            }
            uniqueCount++;
        } else {
            ValueFrequency vc = new SingleValueFrequency(value, count);
            valueCountList.register(vc);
        }
        return uniqueCount;
    }
}
