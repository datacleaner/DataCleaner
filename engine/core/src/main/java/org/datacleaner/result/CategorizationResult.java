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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.metamodel.util.Ref;
import org.apache.metamodel.util.SerializableRef;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.Metric;
import org.datacleaner.api.ParameterizableMetric;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;

/**
 * A simple {@link AnalyzerResult} that exposes a set of categories/annotations
 */
public class CategorizationResult implements AnalyzerResult {

    private static final long serialVersionUID = 1L;

    private final Ref<RowAnnotationFactory> _annotationFactoryRef;
    private final Map<String, RowAnnotation> _categories;

    public CategorizationResult(RowAnnotationFactory annotationFactory,
            Collection<Entry<String, RowAnnotation>> categories) {
        _annotationFactoryRef = new SerializableRef<RowAnnotationFactory>(annotationFactory);
        _categories = new LinkedHashMap<>();
        for (Entry<String, RowAnnotation> entry : categories) {
            _categories.put(entry.getKey(), entry.getValue());
        }
    }

    public CategorizationResult(RowAnnotationFactory annotationFactory, Map<String, RowAnnotation> categories) {
        _annotationFactoryRef = new SerializableRef<RowAnnotationFactory>(annotationFactory);
        _categories = categories;
    }

    @Metric("Category count")
    public ParameterizableMetric getCategoryCount() {
        return new ParameterizableMetric() {

            @Override
            public Number getValue(String parameter) {
                return getCategoryCount(parameter);
            }

            @Override
            public Collection<String> getParameterSuggestions() {
                return getCategoryNames();
            }
        };
    }

    public Collection<String> getCategoryNames() {
        return _categories.keySet();
    }

    public Integer getCategoryCount(String category) {
        RowAnnotation annotation = _categories.get(category);
        if (annotation == null) {
            return 0;
        }
        return annotation.getRowCount();
    }

    public AnnotatedRowsResult getCategoryRowSample(String category) {
        final RowAnnotationFactory rowAnnotationFactory = _annotationFactoryRef.get();
        if (rowAnnotationFactory == null) {
            return null;
        }
        final RowAnnotation annotation = _categories.get(category);
        if (annotation == null) {
            return null;
        }
        return new AnnotatedRowsResult(annotation, rowAnnotationFactory);
    }

}
