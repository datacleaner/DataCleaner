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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.datacleaner.api.InputRow;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;

/**
 * Represents a group of scattered points
 */
@SuppressWarnings("SuspiciousNameCombination")
public class JavaScatterGroup implements IScatterGroup {
    private static final long serialVersionUID = 1L;

    private final String _name;

    private final RowAnnotationFactory _rowAnnotationFactory;
    private final Map<Pair<Number, Number>, RowAnnotation> _annotations = new LinkedHashMap<>();

    public JavaScatterGroup(String name, RowAnnotationFactory rowAnnotationFactory) {
        _name = name;
        _rowAnnotationFactory = rowAnnotationFactory;
    }

    @Override
    public String getName() {
        return _name;
    }

    public RowAnnotationFactory getRowAnnotationFactory() {
        return _rowAnnotationFactory;
    }

    private RowAnnotation getAnnotation(Number x, Number y) {
        if (_annotations.containsKey(new ImmutablePair<>(x, y))) {
            return _annotations.get(new ImmutablePair<>(x, y));
        } else {
            final RowAnnotation annotation = _rowAnnotationFactory.createAnnotation();
            _annotations.put(new ImmutablePair<>(x, y), annotation);
            return annotation;
        }
    }

    public void register(Number x, Number y, InputRow row, int distinctCount) {
        final RowAnnotation annotation = getAnnotation(x, y);
        _rowAnnotationFactory.annotate(row, distinctCount, annotation);
    }

    public RowAnnotation getRowAnnotation(Number x, Number y) {
        return _annotations.get(new ImmutablePair<>(x, y));
    }

    public Set<Pair<Number, Number>> getCoordinates() {
        return _annotations.keySet();
    }

    @Override
    public Map<Pair<Number, Number>, RowAnnotation> getRowAnnotations() {
        return _annotations;
    }

    @Override
    public String toString() {
        return "Name=" + _name + "annotations" + _annotations.keySet();
    }
}