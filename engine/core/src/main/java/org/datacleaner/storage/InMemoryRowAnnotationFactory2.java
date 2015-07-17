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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;

/**
 * Successor of {@link InMemoryRowAnnotationFactory}, our implementation of
 * {@link RowAnnotationFactory} that is based on in-memory storage of sample
 * records. A new class was added to allow deserialization of old DataCleaner
 * results, yet this class fully replaces the old one functionally.
 */
final class InMemoryRowAnnotationFactory2 implements RowAnnotationFactory, Serializable {

    private static final long serialVersionUID = 1L;

    private final ConcurrentHashMap<RowAnnotation, Collection<InputRow>> _storage;
    private final int _maxSampleRecords;

    public InMemoryRowAnnotationFactory2() {
        this(500);
    }

    public InMemoryRowAnnotationFactory2(int maxSampleRecords) {
        _storage = new ConcurrentHashMap<>();
        _maxSampleRecords = maxSampleRecords;
    }

    @Override
    public RowAnnotation createAnnotation() {
        return new RowAnnotationImpl();
    }

    @Override
    public void annotate(InputRow[] rows, RowAnnotation annotation) {
        if (rows == null || annotation == null || rows.length == 0) {
            return;
        }

        final Collection<InputRow> rowCollection = getInputRowCollection(rows.length, annotation);

        incrementAnnotationCount(annotation, rows.length);
        addInputRowsToCollection(rowCollection, rows);
    }

    private void incrementAnnotationCount(RowAnnotation annotation, int count) {
        final RowAnnotationImpl rowAnnotationImpl = (RowAnnotationImpl) annotation;
        rowAnnotationImpl.incrementRowCount(count);
    }

    private void addInputRowsToCollection(Collection<InputRow> rowCollection, InputRow[] rows) {
        int size = rowCollection.size();
        if (size >= _maxSampleRecords) {
            return;
        }

        for (InputRow inputRow : rows) {
            rowCollection.add(inputRow);
            size++;
            if (size >= _maxSampleRecords) {
                return;
            }
        }
    }

    private void addInputRowsToCollection(Collection<InputRow> rowCollection, Collection<InputRow> rows) {
        int size = rowCollection.size();
        if (size >= _maxSampleRecords) {
            return;
        }

        for (InputRow inputRow : rows) {
            rowCollection.add(inputRow);
            size++;
            if (size >= _maxSampleRecords) {
                return;
            }
        }
    }

    private Collection<InputRow> getInputRowCollection(int defaultSize, RowAnnotation annotation) {
        Collection<InputRow> rowCollection = _storage.get(annotation);
        if (rowCollection == null) {
            rowCollection = new ArrayList<InputRow>(defaultSize);
            final Collection<InputRow> existingCollection = _storage.putIfAbsent(annotation, rowCollection);
            if (existingCollection != null) {
                rowCollection = existingCollection;
            }
        }
        return rowCollection;
    }

    @Override
    public void annotate(InputRow row, int distinctCount, RowAnnotation annotation) {
        incrementAnnotationCount(annotation, distinctCount);

        final Collection<InputRow> rowCollection = getInputRowCollection(10, annotation);
        for (int i = 0; i < distinctCount; i++) {
            if (rowCollection.size() >= _maxSampleRecords) {
                return;
            }
            rowCollection.add(row);
        }
    }

    @Override
    public void reset(RowAnnotation annotation) {
        final RowAnnotationImpl rowAnnotationImpl = (RowAnnotationImpl) annotation;
        rowAnnotationImpl.resetRowCount();
        
        _storage.remove(annotation);
    }

    @Override
    public InputRow[] getRows(RowAnnotation annotation) {
        final Collection<InputRow> collection = _storage.get(annotation);
        if (collection == null) {
            return new InputRow[0];
        }
        return collection.toArray(new InputRow[collection.size()]);
    }

    @Override
    public Map<Object, Integer> getValueCounts(RowAnnotation annotation, InputColumn<?> inputColumn) {
        final Collection<InputRow> collection = _storage.get(annotation);
        if (collection == null || collection.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<Object, Integer> map = new HashMap<Object, Integer>();

        for (InputRow row : collection) {
            final Object value = row.getValue(inputColumn);
            Integer count = map.get(value);
            if (count == null) {
                count = 0;
            }
            count = count.intValue() + 1;
            map.put(value, count);
        }
        return map;
    }

    @Override
    public void transferAnnotations(RowAnnotation from, RowAnnotation to) {
        final Collection<InputRow> fromCollection = _storage.get(from);
        if (fromCollection == null || fromCollection.isEmpty()) {
            return;
        }

        Collection<InputRow> toCollection = getInputRowCollection(fromCollection.size(), to);

        incrementAnnotationCount(to, from.getRowCount());
        addInputRowsToCollection(toCollection, fromCollection);

        reset(from);
    }
}
