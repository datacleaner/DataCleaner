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
package org.datacleaner.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.Predicate;
import org.apache.metamodel.util.Ref;
import org.apache.metamodel.util.SerializableRef;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;
import org.datacleaner.storage.RowAnnotations;

/**
 * Represents a typical "drill to detail" result consisting of a set of
 * annotated rows.
 * 
 * Furthermore, if classes inherit from {@link AnnotatedRowsResult}, they can be
 * annotated with the {@link Description} annotation to provide a labeling
 * description, used often in rendering.
 * 
 * 
 */
public class AnnotatedRowsResult implements AnalyzerResult, TableModelResult {

    private static final long serialVersionUID = 1L;

    private final Ref<RowAnnotationFactory> _annotationFactoryRef;
    private final InputColumn<?>[] _highlightedColumns;
    private final RowAnnotation _annotation;
    private transient List<InputRow> _rows;
    private transient TableModel _tableModel;
    private transient List<InputColumn<?>> _inputColumns;

    /**
     * Factory method for {@link AnnotatedRowsResult} that will return non-null
     * ONLY if the {@link RowAnnotation} passed in has any sample rows according
     * to the {@link RowAnnotationFactory}.
     * 
     * Otherwise returning null has the benefit that usually it makes it easy to
     * filter out unnecesary drill-to-detail result objects.
     * 
     * @param annotation
     * @param annotationFactory
     * @param column
     * @return
     */
    public static AnnotatedRowsResult createIfSampleRowsAvailable(RowAnnotation annotation,
            RowAnnotationFactory annotationFactory, InputColumn<?>... columns) {
        if (annotationFactory.hasSampleRows(annotation)) {
            return new AnnotatedRowsResult(annotation, annotationFactory, columns);
        }
        return null;
    }

    public AnnotatedRowsResult(RowAnnotation annotation, RowAnnotationFactory annotationFactory,
            InputColumn<?>... highlightedColumns) {
        _annotationFactoryRef = new SerializableRef<RowAnnotationFactory>(annotationFactory);
        _annotation = annotation;
        _highlightedColumns = highlightedColumns;
    }

    public List<InputColumn<?>> getInputColumns() {
        if (_inputColumns == null) {
            final List<InputRow> rows = getSampleRows();
            if (!rows.isEmpty()) {
                final InputRow firstRow = rows.iterator().next();
                final List<InputColumn<?>> inputColumns = firstRow.getInputColumns();
                _inputColumns = CollectionUtils.filter(inputColumns, new Predicate<InputColumn<?>>() {
                    @Override
                    public Boolean eval(InputColumn<?> col) {
                        if (col instanceof MutableInputColumn) {
                            if (((MutableInputColumn<?>) col).isHidden()) {
                                // avoid hidden columns in the
                                return false;
                            }
                        }
                        return true;
                    }
                });
            } else {
                _inputColumns = new ArrayList<InputColumn<?>>(0);
            }
        }
        return _inputColumns;
    }

    /**
     * 
     * @return
     * @deprecated use {@link #getSampleRows()} instead
     **/
    @Deprecated
    public InputRow[] getRows() {
        return getSampleRows().toArray(new InputRow[0]);
    }

    public List<InputRow> getSampleRows() {
        if (_rows == null) {
            final RowAnnotationFactory annotationFactory = _annotationFactoryRef.get();
            if (annotationFactory != null) {
                _rows = annotationFactory.getSampleRows(getAnnotation());
            }
            if (_rows == null) {
                _rows = Collections.emptyList();
            }
        }
        return _rows;
    }

    /**
     * Creates a table model containing only distinct values from a particular
     * input column, and the counts of those distinct values. Note that the
     * counts may only be the count from the data that is available in the
     * annotation row storage, which may just be a preview/subset of the actual
     * data.
     * 
     * @param inputColumnOfInterest
     * @return
     */
    public TableModel toDistinctValuesTableModel(InputColumn<?> inputColumnOfInterest) {
        final Map<Object, Integer> valueCounts;
        final RowAnnotationFactory annotationFactory = _annotationFactoryRef.get();
        if (annotationFactory == null) {
            valueCounts = Collections.emptyMap();
        } else {
            valueCounts = getValueCounts(annotationFactory, getAnnotation(), inputColumnOfInterest);
        }
        DefaultTableModel tableModel = new DefaultTableModel(new String[] { inputColumnOfInterest.getName(),
                "Count in dataset" }, valueCounts.size());

        // sort the set
        TreeSet<Entry<Object, Integer>> set = new TreeSet<Entry<Object, Integer>>(
                new Comparator<Entry<Object, Integer>>() {
                    @Override
                    public int compare(Entry<Object, Integer> o1, Entry<Object, Integer> o2) {
                        int countDiff = o2.getValue().intValue() - o1.getValue().intValue();
                        if (countDiff == 0) {
                            return -1;
                        }
                        return countDiff;
                    }
                });
        set.addAll(valueCounts.entrySet());

        int i = 0;
        for (Entry<Object, Integer> entry : set) {
            tableModel.setValueAt(entry.getKey(), i, 0);
            tableModel.setValueAt(entry.getValue(), i, 1);
            i++;
        }

        return tableModel;
    }

    private Map<Object, Integer> getValueCounts(RowAnnotationFactory annotationFactory, RowAnnotation annotation,
            InputColumn<?> inputColumn) {
        final List<InputRow> rows = annotationFactory.getSampleRows(annotation);

        if (rows == null || rows.isEmpty()) {
            return Collections.emptyMap();
        }

        HashMap<Object, Integer> map = new HashMap<Object, Integer>();
        for (InputRow row : rows) {
            Object value = row.getValue(inputColumn);
            Integer count = map.get(value);
            if (count == null) {
                count = 0;
            }
            count = count.intValue() + 1;
            map.put(value, count);
        }
        return map;
    }

    /**
     * 
     * @param maxRows
     * @return
     */
    public TableModel toTableModel(int maxRows) {
        if (maxRows < 0) {
            maxRows = Integer.MAX_VALUE;
        }

        final List<InputRow> rows = getSampleRows();
        final List<InputColumn<?>> inputColumns = getInputColumns();
        final String[] headers = new String[inputColumns.size()];
        for (int i = 0; i < headers.length; i++) {
            headers[i] = inputColumns.get(i).getName();
        }

        final int actualRows = Math.min(maxRows, rows.size());
        final TableModel tableModel = new DefaultTableModel(headers, actualRows);
        int row = 0;
        for (InputRow inputRow : rows) {
            if (actualRows == row) {
                break;
            }
            for (int i = 0; i < inputColumns.size(); i++) {
                InputColumn<?> inputColumn = inputColumns.get(i);
                Object value = inputRow.getValue(inputColumn);
                tableModel.setValueAt(value, row, i);
            }
            row++;
        }
        return tableModel;
    }

    @Override
    public TableModel toTableModel() {
        if (_tableModel == null) {
            _tableModel = toTableModel(-1);
        }
        return _tableModel;
    }

    public InputColumn<?>[] getHighlightedColumns() {
        return _highlightedColumns;
    }

    public int getColumnIndex(InputColumn<?> col) {
        List<InputColumn<?>> inputColumns = getInputColumns();
        int i = 0;
        for (InputColumn<?> inputColumn : inputColumns) {
            if (col.equals(inputColumn)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public RowAnnotation getAnnotation() {
        if (_annotation == null) {
            // only occurs for deserialized instances
            return RowAnnotations.getDefaultFactory().createAnnotation();
        }
        return _annotation;
    }

    public int getAnnotatedRowCount() {
        return getAnnotation().getRowCount();
    }
}
