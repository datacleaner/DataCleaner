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
package org.datacleaner.connection.datahub.dummy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.metamodel.data.AbstractDataSet;
import org.apache.metamodel.data.DataSetHeader;
import org.apache.metamodel.data.DefaultRow;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.schema.Column;

/**
 * Dummy dataset with fixed query result of 3 rows
 * @author hetty
 *
 */
public class DatahubDummyDataSet extends AbstractDataSet {

    List<Object[]> queryResult;
    AtomicInteger _index;
    private Object[] record;

    DatahubDummyDataSet(Column[] columns) {
        super(columns);
        queryResult = new ArrayList<Object[]>();
        queryResult
                .add(new Object[] { new Integer(1), "John", new Integer(18) });
        queryResult
                .add(new Object[] { new Integer(2), "Mary", new Integer(20) });
        queryResult.add(new Object[] { new Integer(3), "Gwendolyn",
                new Integer(4) });
        _index = new AtomicInteger();
    }

    @Override
    public boolean next() {
        int index = _index.getAndIncrement();
        if (index < queryResult.size()) {
            record = queryResult.get(index);
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
