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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.datacleaner.api.InputRow;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.util.ImmutableEntry;

/**
 * Default {@link RowAnnotationFactory} instance. Stores up to 1000 rows in an
 * in memory annotation.
 *
 * @deprecated get your {@link RowAnnotationFactory} from the
 *             {@link DataCleanerEnvironment#getStorageProvider()} method or the
 *             {@link RowAnnotations} class instead.
 */
@Deprecated
public class InMemoryRowAnnotationFactory extends AbstractRowAnnotationFactory {

    private static final long serialVersionUID = 1L;

    // contains annotations, mapped to row-ids
    private final Map<RowAnnotation, Set<Integer>> _annotatedRows = new ConcurrentHashMap<>();

    // contains row id's mapped to rows mapped to distinct counts
    private final Map<Integer, Map.Entry<InputRow, Integer>> _distinctCounts = new ConcurrentHashMap<>();

    public InMemoryRowAnnotationFactory() {
        this(1000);
    }

    public InMemoryRowAnnotationFactory(final int storedRowsThreshold) {
        super(storedRowsThreshold);
    }

    protected int getInMemoryRowCount(final RowAnnotation annotation) {
        final Set<Integer> rows = _annotatedRows.get(annotation);
        if (rows == null) {
            return 0;
        }
        return rows.size();
    }

    @Override
    protected void resetRows(final RowAnnotation annotation) {
        _annotatedRows.remove(annotation);
    }

    @Override
    protected int getDistinctCount(final InputRow row) {
        return _distinctCounts.get(row.getId()).getValue();
    }

    @Override
    public void annotate(final InputRow row, final int distinctCount, final RowAnnotation annotation) {
        for (int i = 0; i < distinctCount; i++) {
            annotate(row, annotation);
        }
    }

    @Override
    protected void storeRowAnnotation(final int rowId, final RowAnnotation annotation) {
        final Set<Integer> rowIds = getRowIds(annotation);
        rowIds.add(rowId);
    }

    private Set<Integer> getRowIds(final RowAnnotation annotation) {
        Set<Integer> rowIds = _annotatedRows.get(annotation);
        if (rowIds == null) {
            rowIds = Collections.synchronizedSet(new LinkedHashSet<Integer>());
            _annotatedRows.put(annotation, rowIds);
        }
        return rowIds;
    }

    @Override
    protected void storeRowValues(final int rowId, final InputRow row) {
        _distinctCounts.put(rowId, new ImmutableEntry<>(row, 1));
    }

    @Override
    public boolean hasSampleRows(final RowAnnotation annotation) {
        if (_annotatedRows.containsKey(annotation)) {
            return true;
        }
        return false;
    }

    @Override
    public List<InputRow> getSampleRows(final RowAnnotation annotation) {
        final Set<Integer> rowIds = _annotatedRows.get(annotation);
        if (rowIds == null) {
            return Collections.emptyList();
        }
        final List<InputRow> rows = new ArrayList<>(rowIds.size());
        for (final Integer rowId : rowIds) {
            final InputRow row = _distinctCounts.get(rowId).getKey();
            rows.add(row);
        }
        return rows;
    }

    @Override
    public void transferAnnotations(final RowAnnotation from, final RowAnnotation to) {
        final int rowCountToAdd = from.getRowCount();
        ((RowAnnotationImpl) to).incrementRowCount(rowCountToAdd);
    }
}
