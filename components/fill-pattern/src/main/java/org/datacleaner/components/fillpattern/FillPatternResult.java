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

import java.util.Collections;
import java.util.List;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.storage.RowAnnotationFactory;

public class FillPatternResult implements AnalyzerResult {

    private static final long serialVersionUID = 1L;

    private final List<FillPatternGroup> _fillPatternGroups;
    private final List<InputColumn<?>> _inspectedColumns;
    private final RowAnnotationFactory _rowAnnotationFactory;

    public FillPatternResult(RowAnnotationFactory rowAnnotationFactory, List<InputColumn<?>> inspectedColumns,
            List<FillPatternGroup> fillPatterns) {
        _rowAnnotationFactory = rowAnnotationFactory;
        _inspectedColumns = inspectedColumns;
        _fillPatternGroups = fillPatterns;
    }

    public List<FillPatternGroup> getFillPatternGroups() {
        return Collections.unmodifiableList(_fillPatternGroups);
    }

    public boolean isGrouped() {
        if (_fillPatternGroups.size() == 1 && _fillPatternGroups.get(0).getGroupName() == null) {
            return false;
        }
        return true;
    }

    public RowAnnotationFactory getRowAnnotationFactory() {
        return _rowAnnotationFactory;
    }

    public List<InputColumn<?>> getInspectedColumns() {
        return Collections.unmodifiableList(_inspectedColumns);
    }
}
