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
package org.datacleaner.components.group;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.metamodel.util.AggregateBuilder;
import org.apache.metamodel.util.ObjectComparator;

abstract class AbstractRowNumberAwareAggregateBuilder<T> implements AggregateBuilder<T> {

    private final SortationType _sortationType;
    private final Object _values;
    private final boolean _skipNulls;

    public AbstractRowNumberAwareAggregateBuilder(SortationType sortationType, boolean skipNulls) {
        _sortationType = sortationType;
        _skipNulls = skipNulls;

        switch (sortationType) {
        case NONE:
            _values = null;
            break;
        case NATURAL_SORT_ASC:
            _values = new TreeSet<>(ObjectComparator.getComparator());
            break;
        case NATURAL_SORT_DESC:
            _values = new TreeSet<>(Collections.reverseOrder(ObjectComparator.getComparator()));
            break;
        case RECORD_ORDER:
            _values = new TreeMap<Long, Object>();
            break;
        default:
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public final void add(Object o) {
        throw new UnsupportedOperationException();
    }

    public final void add(Object o, long rowNumber) {
        if (_skipNulls && o == null) {
            return;
        }

        switch (_sortationType) {
        case NONE:
            addSorted(o);
            break;
        case NATURAL_SORT_ASC:
        case NATURAL_SORT_DESC:
            @SuppressWarnings("unchecked")
            final Collection<Object> collection = (Collection<Object>) _values;
            collection.add(o);
            break;
        case RECORD_ORDER:
            @SuppressWarnings("unchecked")
            final Map<Long, Object> map = (Map<Long, Object>) _values;
            map.put(rowNumber, o);
            break;
        }
    }

    @Override
    public final T getAggregate() {
        switch (_sortationType) {
        case NONE:
            break;
        case NATURAL_SORT_ASC:
        case NATURAL_SORT_DESC:
            @SuppressWarnings("unchecked")
            final Collection<Object> collection = (Collection<Object>) _values;
            for (Object o : collection) {
                addSorted(o);
            }
            break;
        case RECORD_ORDER:
            @SuppressWarnings("unchecked")
            final Map<Integer, Object> map = (Map<Integer, Object>) _values;
            final Collection<Object> objects = map.values();
            for (Object o : objects) {
                addSorted(o);
            }
            break;
        }

        return getAggregateSorted();
    }

    protected abstract T getAggregateSorted();

    protected abstract void addSorted(Object o);
}
