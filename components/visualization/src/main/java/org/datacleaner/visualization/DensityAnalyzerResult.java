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

import java.awt.Point;
import java.util.Comparator;
import java.util.Map;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;

public class DensityAnalyzerResult implements AnalyzerResult {

    private static final long serialVersionUID = 1L;

    private final InputColumn<Number> _variable1;
    private final InputColumn<Number> _variable2;
    private final RowAnnotationFactory _rowAnnotationFactory;
    private final Map<Point, RowAnnotation> _annotations;

    public DensityAnalyzerResult(Map<Point, RowAnnotation> annotations, InputColumn<Number> variable1,
            InputColumn<Number> variable2, RowAnnotationFactory rowAnnotationFactory) {
        _annotations = annotations;
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

    public Map<Point, RowAnnotation> getRowAnnotations() {
        return _annotations;
    }

    public RowAnnotationFactory getRowAnnotationFactory() {
        return _rowAnnotationFactory;
    }

    public RowAnnotation getRowAnnotation(int x, int y) {
        final Point searchedPoint = new Point(x, y);
        final RowAnnotation rowAnnotation = _annotations.get(searchedPoint);
        return rowAnnotation;
    }

    public Comparator<RowAnnotation> getRowAnnotationComparator() {
        return new Comparator<RowAnnotation>() {

            @Override
            public int compare(RowAnnotation o1, RowAnnotation o2) {
                return o1.getRowCount() - o2.getRowCount();
            }

        };
    }
}
