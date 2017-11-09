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
package org.datacleaner.components.fillpattern;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.datacleaner.api.InputRow;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;

public class FillPatternsBuilder {

    private final Map<List<Object>, RowAnnotation> _observations;
    private final RowAnnotationFactory _rowAnnotationFactory;

    public FillPatternsBuilder(RowAnnotationFactory rowAnnotationFactory) {
        _rowAnnotationFactory = rowAnnotationFactory;
        _observations = new HashMap<List<Object>, RowAnnotation>();
    }

    public void addObservation(InputRow row, List<Object> inspectionOutcomes) {
        RowAnnotation annotation = _observations.get(inspectionOutcomes);
        if (annotation == null) {
            synchronized (this) {
                annotation = _observations.get(inspectionOutcomes);
                if (annotation == null) {
                    annotation = _rowAnnotationFactory.createAnnotation();
                    _observations.put(inspectionOutcomes, annotation);
                }
            }
        }
        _rowAnnotationFactory.annotate(row, annotation);
    }

    public FillPatternGroup build(String groupName) {
        final List<FillPattern> sortedPatterns = _observations.entrySet().stream().map(e -> new FillPattern(e.getKey(),
                e.getValue())).sorted().collect(Collectors.toList());
        return new FillPatternGroup(groupName, sortedPatterns);
    }
}
