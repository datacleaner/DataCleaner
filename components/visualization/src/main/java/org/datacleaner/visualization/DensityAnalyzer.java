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
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.Provided;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;
import org.datacleaner.visualization.VisualizationCategory;

@Named("Density plot")
@Description("java Plots the occurences of two number variables in a density plot chart. A useful visualization for identifying freqencies of combinations in numeric data relationships.")
@Categorized(VisualizationCategory.class)
public class DensityAnalyzer implements Analyzer<DensityAnalyzerResult> {

    private final String PROPERTY_VARIABLE1 = "Variable1";
    private final String PROPERTY_VARIABLE2 = "Variable2";

    @Inject
    @Configured(value = PROPERTY_VARIABLE1)
    @Description("The field with the first variable. Will be plotted on the horizontal X-axis.")
    InputColumn<Number> variable1 = null;

    @Inject
    @Configured(value = PROPERTY_VARIABLE2)
    @Description("The field with the second variable. Will be plotted on the vertical Y-axis.")
    InputColumn<Number> variable2 = null;

    @Inject
    @Provided
    RowAnnotationFactory _rowAnnotationFactory = null;

    private final Map<Point, RowAnnotation> _annotations = new HashMap<>();

    public RowAnnotation annotations(Point point) {
        if (_annotations.containsKey(point)) {
            return _annotations.get(point);
        } else {
            final RowAnnotation annotation = _rowAnnotationFactory.createAnnotation();
            _annotations.put(point, annotation);
            return annotation;
        }
    }

    @Override
    public void run(InputRow row, int distinctCount) {
        final Number value1 = row.getValue(variable1);
        final Number value2 = row.getValue(variable2);
        if (value1 != null && value2 != null) {
            final Point point = new Point(value1.intValue(), value2.intValue());
            final RowAnnotation annotation = annotations(point);
            _rowAnnotationFactory.annotate(row, distinctCount, annotation);
        }
    }

    @Override
    public DensityAnalyzerResult getResult() {
        return new DensityAnalyzerResult(_annotations, variable1, variable2, _rowAnnotationFactory);
    }

}
