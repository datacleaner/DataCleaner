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
package org.datacleaner.components.group;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.datacleaner.api.InputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.test.MockOutputRowCollector;
import org.junit.Test;

public class GrouperTransformerTest {

    private final MockInputColumn<String> groupKey = new MockInputColumn<>("key");
    private final MockInputColumn<String> value1 = new MockInputColumn<>("value1");
    private final MockInputColumn<String> value2 = new MockInputColumn<>("value2");

    @Test
    public void testRecordOrderSorting() throws Exception {
        final GrouperTransformer grouper = new GrouperTransformer();
        grouper.groupKey = groupKey;
        grouper.aggregatedValues = new InputColumn[] { value1, value2 };
        grouper.aggregationTypes = new GrouperTransformer.AggregationType[] {
                GrouperTransformer.AggregationType.CONCAT_VALUES, GrouperTransformer.AggregationType.CONCAT_VALUES };
        grouper.valueSortation = SortationType.RECORD_ORDER;
        grouper.concatenationSeparator = ";";

        grouper.init();

        MockOutputRowCollector collector = new MockOutputRowCollector();
        grouper.initializeOutputDataStream(null, null, collector);

        grouper.transform(new MockInputRow(3).put(groupKey, "A").put(value1, "hi").put(value2, "C"));
        grouper.transform(new MockInputRow(2).put(groupKey, "A").put(value1, "world").put(value2, "B"));
        grouper.transform(new MockInputRow(1).put(groupKey, "A").put(value1, "hello").put(value2, "A"));
        grouper.transform(new MockInputRow(5).put(groupKey, "B").put(value1, "hola").put(value2, "E"));
        grouper.transform(new MockInputRow(4).put(groupKey, "A").put(value1, "there").put(value2, "D"));

        grouper.close();

        List<Object[]> output = collector.getOutput();
        assertEquals(2, output.size());
        assertEquals("[A, 4, hello;world;hi;there, A;B;C;D]", Arrays.toString(output.get(0)));
        assertEquals("[B, 1, hola, E]", Arrays.toString(output.get(1)));
    }

    @Test
    public void testNaturalSorting() throws Exception {
        final GrouperTransformer grouper = new GrouperTransformer();
        grouper.groupKey = groupKey;
        grouper.aggregatedValues = new InputColumn[] { value1, value2 };
        grouper.aggregationTypes = new GrouperTransformer.AggregationType[] {
                GrouperTransformer.AggregationType.CONCAT_VALUES, GrouperTransformer.AggregationType.CONCAT_VALUES };
        grouper.valueSortation = SortationType.NATURAL_SORT_ASC;
        grouper.concatenationSeparator = ";";

        grouper.init();

        MockOutputRowCollector collector = new MockOutputRowCollector();
        grouper.initializeOutputDataStream(null, null, collector);

        grouper.transform(new MockInputRow(3).put(groupKey, "A").put(value1, "hi").put(value2, "C"));
        grouper.transform(new MockInputRow(2).put(groupKey, "A").put(value1, "world").put(value2, "B"));
        grouper.transform(new MockInputRow(1).put(groupKey, "A").put(value1, "hello").put(value2, "A"));
        grouper.transform(new MockInputRow(5).put(groupKey, "B").put(value1, "hola").put(value2, "E"));
        grouper.transform(new MockInputRow(4).put(groupKey, "A").put(value1, "there").put(value2, "D"));

        grouper.close();

        List<Object[]> output = collector.getOutput();
        assertEquals(2, output.size());
        assertEquals("[A, 4, hello;hi;there;world, A;B;C;D]", Arrays.toString(output.get(0)));
        assertEquals("[B, 1, hola, E]", Arrays.toString(output.get(1)));
    }

    @Test
    public void testNaturalSortingDesc() throws Exception {
        final GrouperTransformer grouper = new GrouperTransformer();
        grouper.groupKey = groupKey;
        grouper.aggregatedValues = new InputColumn[] { value1, value2 };
        grouper.aggregationTypes = new GrouperTransformer.AggregationType[] {
                GrouperTransformer.AggregationType.CONCAT_VALUES, GrouperTransformer.AggregationType.CONCAT_VALUES };
        grouper.valueSortation = SortationType.NATURAL_SORT_DESC;
        grouper.concatenationSeparator = ";";

        grouper.init();

        MockOutputRowCollector collector = new MockOutputRowCollector();
        grouper.initializeOutputDataStream(null, null, collector);

        grouper.transform(new MockInputRow(3).put(groupKey, "A").put(value1, "hi").put(value2, "C"));
        grouper.transform(new MockInputRow(2).put(groupKey, "A").put(value1, "world").put(value2, "B"));
        grouper.transform(new MockInputRow(1).put(groupKey, "A").put(value1, "hello").put(value2, "A"));
        grouper.transform(new MockInputRow(5).put(groupKey, "B").put(value1, "hola").put(value2, "E"));
        grouper.transform(new MockInputRow(4).put(groupKey, "A").put(value1, "there").put(value2, "D"));

        grouper.close();

        List<Object[]> output = collector.getOutput();
        assertEquals(2, output.size());
        assertEquals("[A, 4, world;there;hi;hello, D;C;B;A]", Arrays.toString(output.get(0)));
        assertEquals("[B, 1, hola, E]", Arrays.toString(output.get(1)));
    }
}
