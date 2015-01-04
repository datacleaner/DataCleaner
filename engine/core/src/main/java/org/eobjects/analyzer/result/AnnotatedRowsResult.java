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
package org.eobjects.analyzer.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.storage.InMemoryRowAnnotationFactory;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;
import org.apache.metamodel.util.Ref;
import org.apache.metamodel.util.SerializableRef;

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
    private transient InputRow[] _rows;
    private transient TableModel _tableModel;
    private transient List<InputColumn<?>> _inputColumns;

    public AnnotatedRowsResult(RowAnnotation annotation, RowAnnotationFactory annotationFactory,
            InputColumn<?>... highlightedColumns) {
        _annotationFactoryRef = new SerializableRef<RowAnnotationFactory>(annotationFactory);
        _annotation = annotation;
        _highlightedColumns = highlightedColumns;
    }

    public List<InputColumn<?>> getInputColumns() {
        if (_inputColumns == null) {
            InputRow[] rows = getRows();
            if (rows.length > 0) {
                InputRow firstRow = rows[0];
                _inputColumns = firstRow.getInputColumns();
            } else {
                _inputColumns = new ArrayList<InputColumn<?>>(0);
            }
        }
        return _inputColumns;
    }

    public InputRow[] getRows() {
        if (_rows == null) {
            RowAnnotationFactory annotationFactory = _annotationFactoryRef.get();
            if (annotationFactory != null) {
                _rows = annotationFactory.getRows(getAnnotation());
            }
            if (_rows == null) {
                _rows = new InputRow[0];
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
            valueCounts = annotationFactory.getValueCounts(getAnnotation(), inputColumnOfInterest);
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

    /**
     * 
     * @param maxRows
     * @return
     */
    public TableModel toTableModel(int maxRows) {
        if (maxRows < 0) {
            maxRows = Integer.MAX_VALUE;
        }

        final InputRow[] rows = getRows();
        final List<InputColumn<?>> inputColumns = getInputColumns();
        final String[] headers = new String[inputColumns.size()];
        for (int i = 0; i < headers.length; i++) {
            headers[i] = inputColumns.get(i).getName();
        }

        final int actualRows = Math.min(maxRows, rows.length);
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
            return new InMemoryRowAnnotationFactory().createAnnotation();
        }
        return _annotation;
    }

    public int getAnnotatedRowCount() {
        return getAnnotation().getRowCount();
    }
}
