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
package org.datacleaner.components.fuse;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.MutableTable;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.test.MockOutputRowCollector;

public class FuseStreamsComponentUnitTest extends TestCase {

    public void testCombineValuesFromMultipleTables() throws Exception {
        final MutableTable table1 = new MutableTable("table1");
        table1.addColumn(new MutableColumn("id").setTable(table1));
        table1.addColumn(new MutableColumn("name1").setTable(table1));
        table1.addColumn(new MutableColumn("city1").setTable(table1));

        final MutableTable table2 = new MutableTable("table2");
        table2.addColumn(new MutableColumn("uuid").setTable(table2));
        table2.addColumn(new MutableColumn("name2").setTable(table2));
        table2.addColumn(new MutableColumn("town2").setTable(table2));

        final InputColumn<?> colName1 = new MetaModelInputColumn(table1.getColumnByName("name1"));
        final InputColumn<?> colCity1 = new MetaModelInputColumn(table1.getColumnByName("city1"));
        final InputColumn<?> colName2 = new MetaModelInputColumn(table2.getColumnByName("name2"));
        final InputColumn<?> colTown2 = new MetaModelInputColumn(table2.getColumnByName("town2"));

        final CoalesceUnit unit1 = new CoalesceUnit("name1", "name2");
        final CoalesceUnit unit2 = new CoalesceUnit("city1", "town2");
        final FuseStreamsComponent component = new FuseStreamsComponent(unit1, unit2);

        component._inputs = new InputColumn[] { colName1, colCity1, colName2, colTown2 };

        final OutputDataStream[] streams = component.getOutputDataStreams();
        assertEquals(1, streams.length);

        final OutputDataStream stream = streams[0];
        assertEquals("[name1, city1]", Arrays.toString(stream.getTable().getColumnNames()));
        
        final MockOutputRowCollector outputRowCollector = new MockOutputRowCollector();
        component.initializeOutputDataStream(stream, null, outputRowCollector);

        component.init();
        component.run(new MockInputRow().put(colName1, "A").put(colCity1, "B"));
        component.run(new MockInputRow().put(colName2, "C").put(colTown2, "D"));

        final List<Object[]> output = outputRowCollector.getOutput();
        assertEquals(2, output.size());
        assertEquals("[A, B]", Arrays.toString(output.get(0)));
        assertEquals("[C, D]", Arrays.toString(output.get(1)));
    }
}
