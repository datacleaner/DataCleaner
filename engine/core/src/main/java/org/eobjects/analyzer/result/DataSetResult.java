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
import java.util.List;

import javax.swing.table.TableModel;

import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.DataSetTableModel;
import org.apache.metamodel.data.InMemoryDataSet;
import org.apache.metamodel.data.Row;

public class DataSetResult implements TableModelResult, AnalyzerResult {

    private static final long serialVersionUID = 1L;

    // this class uses a list of rows in order to make it serializable (a
    // DataSet is not serializable)
    private final List<Row> _rows;

    public DataSetResult(List<Row> rows) {
        _rows = rows;
    }

    public DataSetResult(DataSet ds) {
        _rows = new ArrayList<Row>();
        while (ds.next()) {
            _rows.add(ds.getRow());
        }
        ds.close();
    }

    public List<Row> getRows() {
        return _rows;
    }

    public DataSet getDataSet() {
        return new InMemoryDataSet(_rows);
    }

    @Override
    public TableModel toTableModel() {
        return new DataSetTableModel(getDataSet());
    }
}
