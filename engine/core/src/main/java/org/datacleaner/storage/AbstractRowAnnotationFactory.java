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
package org.datacleaner.storage;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.datacleaner.api.InputRow;
import org.datacleaner.util.CollectionUtils2;

import com.google.common.cache.Cache;

/**
 * An abstract RowAnnotationFactory that supports a (optional) threshold
 * 
 * @deprecated this abstract implementation was found to be way too greedy and
 *             dirty, see issue #506
 */
@Deprecated
public abstract class AbstractRowAnnotationFactory implements RowAnnotationFactory, Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<RowAnnotationImpl, AtomicInteger> _rowCounts = new ConcurrentHashMap<RowAnnotationImpl, AtomicInteger>();
    private final Integer _storedRowsThreshold;

    private final transient Cache<Integer, Boolean> _cachedRows = CollectionUtils2.createCache(10000, 10 * 60);

    public AbstractRowAnnotationFactory(Integer storedRowsThreshold) {
        if (storedRowsThreshold == null) {
            _storedRowsThreshold = Integer.MAX_VALUE;
        } else {
            _storedRowsThreshold = storedRowsThreshold;
        }
    }

    @Override
    public final void annotate(InputRow row, RowAnnotation annotation) {
        final RowAnnotationImpl ann = (RowAnnotationImpl) annotation;

        final AtomicInteger count = getCounter(ann);

        boolean storeRow = true;
        if (_storedRowsThreshold != null) {
            if (count.getAndIncrement() >= _storedRowsThreshold.intValue()) {
                storeRow = false;
            }
        }

        if (storeRow) {
            // TODO: In clustered scenarios, there's a chance of row ID
            // collision
            final int rowId = row.getId();
            if (_cachedRows != null) {
                Boolean previously = _cachedRows.asMap().putIfAbsent(rowId, true);
                if (previously == null) {
                    // only store row values when they where not present
                    // previously
                    storeRowValues(rowId, row);
                }
            }
            storeRowAnnotation(rowId, annotation);
        }

        ann.incrementRowCount(1);
    }

    private AtomicInteger getCounter(RowAnnotationImpl ann) {
        AtomicInteger count = _rowCounts.get(ann);
        if (count == null) {
            if (_rowCounts instanceof ConcurrentMap) {
                AtomicInteger newCounter = new AtomicInteger();
                ConcurrentMap<RowAnnotationImpl, AtomicInteger> concurrentMap = (ConcurrentMap<RowAnnotationImpl, AtomicInteger>) _rowCounts;
                count = concurrentMap.putIfAbsent(ann, newCounter);
                if (count == null) {
                    count = newCounter;
                }
            } else {
                // for backwards compatibility we also need to support
                // (deserialized) hash maps
                synchronized (_rowCounts) {
                    count = _rowCounts.get(ann);
                    if (count == null) {
                        count = new AtomicInteger();
                        _rowCounts.put(ann, count);
                    }
                }
            }
        }
        return count;
    }

    @Override
    public final void resetAnnotation(RowAnnotation annotation) {
        RowAnnotationImpl ann = (RowAnnotationImpl) annotation;
        ann.resetRowCount();
        _rowCounts.remove(annotation);
        resetRows(annotation);
    }

    @Override
    public final RowAnnotation createAnnotation() {
        RowAnnotationImpl ann = new RowAnnotationImpl();
        return ann;
    }

    /**
     * Removes the annotation from any rows that has been annotated with it.
     * 
     * @param annotation
     */
    protected abstract void resetRows(RowAnnotation annotation);

    /**
     * Gets the distinct count from a row that has been stored and retried using
     * the getRows(...) method.
     * 
     * @param row
     * @return
     */
    protected abstract int getDistinctCount(InputRow row);

    protected abstract void storeRowAnnotation(int rowId, RowAnnotation annotation);

    protected abstract void storeRowValues(int rowId, InputRow row);

    public final Integer getStoredRowsThreshold() {
        return _storedRowsThreshold;
    }
}
