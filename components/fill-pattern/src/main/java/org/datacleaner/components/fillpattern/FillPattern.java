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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.datacleaner.storage.RowAnnotation;

public class FillPattern implements Comparable<FillPattern>, Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Object> _fillOutcomes;
    private final RowAnnotation _rowAnnotation;

    public FillPattern(List<Object> fillOutcomes, RowAnnotation rowAnnotation) {
        _fillOutcomes = fillOutcomes;
        _rowAnnotation = rowAnnotation;
    }

    public int getObservationCount() {
        return _rowAnnotation.getRowCount();
    }

    public RowAnnotation getRowAnnotation() {
        return _rowAnnotation;
    }

    public List<Object> getFillOutcomes() {
        return Collections.unmodifiableList(_fillOutcomes);
    }

    @Override
    public int compareTo(FillPattern other) {
        int diff = other.getObservationCount() - getObservationCount();
        if (diff == 0) {
            diff = other._fillOutcomes.hashCode() - _fillOutcomes.hashCode();
            if (diff == 0) {
                // at this point it does not matter, we just don't want to
                // return them as equal
                diff = other.hashCode() - hashCode();
            }
        }
        return diff;
    }

}
