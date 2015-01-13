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
package org.datacleaner.beans.valuematch;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Named;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Concurrent;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.HasLabelAdvice;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.Provided;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;
import org.datacleaner.util.StringUtils;

@Named("Value matcher")
@Description("Matches actual values against a set of expected values.\nUse this analyzer as a way to narrow down unexpected values, spelling mistakes, missing values and errors.")
@Concurrent(true)
public class ValueMatchAnalyzer implements Analyzer<ValueMatchAnalyzerResult>, HasLabelAdvice {

    @Inject
    @Configured(order = 10)
    InputColumn<?> column;

    @Inject
    @Configured(order = 20)
    String[] expectedValues;

    @Inject
    @Configured(order = 30)
    boolean caseSensitiveMatching = true;

    @Inject
    @Configured(order = 31)
    boolean whiteSpaceSensitiveMatching = true;

    @Inject
    @Provided
    RowAnnotationFactory _rowAnnotationFactory;

    @Inject
    @Provided
    RowAnnotation _nullAnnotation;

    @Inject
    @Provided
    RowAnnotation _nonMatchingValuesAnnotation;

    private Map<String, RowAnnotation> _valueAnnotations;
    private AtomicInteger _totalCount;

    @Initialize
    public void init() {
        _totalCount = new AtomicInteger();
        _valueAnnotations = new ConcurrentHashMap<String, RowAnnotation>();
        for (String value : expectedValues) {
            final RowAnnotation annotation = _rowAnnotationFactory.createAnnotation();
            String lookupValue = getLookupValue(value);
            _valueAnnotations.put(lookupValue, annotation);
        }
    }

    private String getLookupValue(String value) {
        if (!caseSensitiveMatching) {
            value = value.toLowerCase();
        }
        if (!whiteSpaceSensitiveMatching) {
            value = StringUtils.replaceWhitespaces(value, "");
        }
        return value;
    }

    @Override
    public void run(InputRow row, int distinctCount) {
        _totalCount.addAndGet(distinctCount);
        Object value = row.getValue(column);
        if (value == null) {
            _rowAnnotationFactory.annotate(row, distinctCount, _nullAnnotation);
        } else {
            final String stringValue = value.toString();
            final String lookupValue = getLookupValue(stringValue);
            RowAnnotation annotation = _valueAnnotations.get(lookupValue);
            if (annotation == null) {
                _rowAnnotationFactory.annotate(row, distinctCount, _nonMatchingValuesAnnotation);
            } else {
                _rowAnnotationFactory.annotate(row, distinctCount, annotation);
            }
        }
    }

    @Override
    public ValueMatchAnalyzerResult getResult() {
        // build a map which doesn't contain "lookup values" but the real
        // values, linked/sorted in the original order.
        final Map<String, RowAnnotation> valueAnnotations = new LinkedHashMap<String, RowAnnotation>();
        for (String value : expectedValues) {
            final String lookupValue = getLookupValue(value);
            final RowAnnotation annotation = _valueAnnotations.get(lookupValue);
            valueAnnotations.put(value, annotation);
        }

        return new ValueMatchAnalyzerResult(column, _rowAnnotationFactory, valueAnnotations, _nullAnnotation,
                _nonMatchingValuesAnnotation, _totalCount.get());
    }

    @Override
    public String getSuggestedLabel() {
        if (column == null) {
            return null;
        }
        return "Value matcher: " + column.getName();
    }

}
