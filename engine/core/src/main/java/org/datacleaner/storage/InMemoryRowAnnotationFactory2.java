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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.datacleaner.api.InputRow;

/**
 * Successor of {@link InMemoryRowAnnotationFactory}, our implementation of
 * {@link RowAnnotationFactory} that is based on in-memory storage of sample
 * records. A new class was added to allow deserialization of old DataCleaner
 * results, yet this class fully replaces the old one functionally.
 */
public final class InMemoryRowAnnotationFactory2 extends AbstractRowAnnotationFactory2 implements RowAnnotationFactory,
        Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Kind of a magic number, but a way to ensure that the ratio between sample
     * sets and records is kept under control, at least in default scenarios.
     */
    private static final int DEFAULT_SAMPLE_LIMIT = 500 * 500;

    private final ConcurrentHashMap<RowAnnotation, List<InputRow>> _storage;
    private final int _maxSampleRecords;
    private final int _maxSampleSets;

    public InMemoryRowAnnotationFactory2() {
        this(500);
    }

    public InMemoryRowAnnotationFactory2(int maxSampleRecords) {
        this(Math.min(10, DEFAULT_SAMPLE_LIMIT / maxSampleRecords), 500);
    }

    /**
     * 
     * @param maxSampleSets
     *            the maximum number of sample record collections to keep
     * @param maxSampleRecords
     *            the maximum number of records to keep in each collection
     */
    public InMemoryRowAnnotationFactory2(int maxSampleSets, int maxSampleRecords) {
        _storage = new ConcurrentHashMap<>();
        _maxSampleSets = Math.max(0, maxSampleSets);
        _maxSampleRecords = Math.max(0, maxSampleRecords);
    }

    private void addInputRowsToCollection(Collection<InputRow> rowCollection, Collection<InputRow> rows) {
        if (rowCollection == null) {
            return;
        }
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
        List<InputRow> rowCollection = _storage.get(annotation);
        if (rowCollection == null) {
            if (_storage.size() > _maxSampleSets) {
                return null;
            }
            rowCollection = new ArrayList<InputRow>(defaultSize);
            final List<InputRow> existingCollection = _storage.putIfAbsent(annotation, rowCollection);
            if (existingCollection != null) {
                rowCollection = existingCollection;
            }
        }
        return rowCollection;
    }

    @Override
    public void annotate(InputRow row, RowAnnotation annotation) {
        super.annotate(row, annotation);

        final Collection<InputRow> rowCollection = getInputRowCollection(10, annotation);
        if (rowCollection != null) {
            if (rowCollection.size() >= _maxSampleRecords) {
                return;
            }
            rowCollection.add(row);
        }
    }

    @Override
    public void resetAnnotation(RowAnnotation annotation) {
        super.resetAnnotation(annotation);

        _storage.remove(annotation);
    }

    @Override
    public List<InputRow> getSampleRows(RowAnnotation annotation) {
        final List<InputRow> collection = _storage.get(annotation);
        if (collection == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(collection);
    }

    @Override
    public void transferAnnotations(RowAnnotation from, RowAnnotation to) {
        super.transferAnnotations(from, to);

        final Collection<InputRow> fromCollection = _storage.get(from);
        if (fromCollection == null || fromCollection.isEmpty()) {
            return;
        }

        Collection<InputRow> toCollection = getInputRowCollection(fromCollection.size(), to);

        addInputRowsToCollection(toCollection, fromCollection);

        _storage.remove(from);
    }

    @Override
    public boolean hasSampleRows(RowAnnotation annotation) {
        return _storage.containsKey(annotation);
    }
}
