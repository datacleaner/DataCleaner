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
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.datacleaner.api.InputColumn;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;

public class JavaDensityAnalyzerResult implements IDensityAnalyzerResult {
    private static final long serialVersionUID = 1L;

    private final InputColumn<Number> _variable1;
    private final InputColumn<Number> _variable2;
    private final RowAnnotationFactory _rowAnnotationFactory;
    private final Map<Pair<Integer, Integer>, RowAnnotation> _annotations;

    public JavaDensityAnalyzerResult(Map<Pair<Integer, Integer>, RowAnnotation> annotations, InputColumn<Number> variable1,
            InputColumn<Number> variable2, RowAnnotationFactory rowAnnotationFactory) {
        // Make sure that Scala wrapper is lost.
        _annotations = new HashMap<>();
        _annotations.putAll(annotations);
        _variable1 = variable1;
        _variable2 = variable2;
        _rowAnnotationFactory = rowAnnotationFactory;
    }

    public InputColumn<Number> getVariable1() {
        return _variable1;
    }

    public InputColumn<Number> getVariable2() {
        return _variable2;
    }

    public Map<Pair<Integer, Integer>, RowAnnotation> getRowAnnotations() {
        return _annotations;
    }

    public RowAnnotationFactory getRowAnnotationFactory() {
        return _rowAnnotationFactory;
    }

    public RowAnnotation getRowAnnotation(int x, int y) {
        final Pair<Integer, Integer> searchedPoint = new ImmutablePair<>(x, y);
        return _annotations.get(searchedPoint);
    }
}