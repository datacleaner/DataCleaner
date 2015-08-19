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
package org.datacleaner.metamodel.datahub;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.metamodel.data.AbstractDataSet;
import org.apache.metamodel.data.DataSetHeader;
import org.apache.metamodel.data.DefaultRow;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.schema.Column;

/**
 * dataset with fixed query result of 3 rows
 * 
 * @author hetty
 *
 */
public class DatahubDataSet extends AbstractDataSet {

    List<Object[]> _queryResult;
    AtomicInteger _index;
    private Object[] record;

/*    public DatahubDataSet(Column[] columns) {
        
        // TODO dummy implementation
        super(columns);
        _queryResult = new ArrayList<Object[]>();
        for (int y = 0; y < 3; ++y) {
            Object[] row = new Object[columns.length];
            for (int i = 0; i < columns.length; ++i) {
                row[i] = "row" + y + ":value" + i;
            }
            _queryResult.add(row);
        }
        _index = new AtomicInteger();
    }
*/
    public DatahubDataSet(List<Object[]> queryResult, Column[] columns) {
        super(columns);
        _queryResult = queryResult;
        _index = new AtomicInteger();
    }

    @Override
    public boolean next() {
        int index = _index.getAndIncrement();
        if (index < _queryResult.size()) {
            record = _queryResult.get(index);
            return true;
        }
        record = null;
        _index.set(0);
        return false;
    }

    @Override
    public Row getRow() {
        if (record != null) {
            final DataSetHeader header = super.getHeader();
            return new DefaultRow(header, record);
        }
        return null;

    }

}
