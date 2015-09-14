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
package org.datacleaner.beans.valuedist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.result.ValueCountList;
import org.datacleaner.result.ValueCountingAnalyzerResult;
import org.junit.Test;

public class ValueDistributionAnalyzerResultReducerTest {

    @Test
    public void testReduceSingleResults() throws Exception {
        ValueDistributionAnalyzer valueDist1 = new ValueDistributionAnalyzer(new MetaModelInputColumn(
                new MutableColumn("col")), true);

        valueDist1.runInternal(new MockInputRow(), "hello", 1);
        valueDist1.runInternal(new MockInputRow(), "hello", 1);
        valueDist1.runInternal(new MockInputRow(), "world", 3);
        valueDist1.runInternal(new MockInputRow(), "locallyUniqueWord", 1);
        ValueDistributionAnalyzerResult partialResult1 = valueDist1.getResult();

        ValueCountList partialTopValues1 = ((SingleValueDistributionResult) partialResult1).getTopValues();
        assertEquals(2, partialTopValues1.getActualSize());
        assertEquals("[world->3]", partialTopValues1.getValueCounts().get(0).toString());
        assertEquals("[hello->2]", partialTopValues1.getValueCounts().get(1).toString());

        assertEquals(0, partialResult1.getNullCount());
        assertEquals(1, partialResult1.getUniqueCount().intValue());

        ValueDistributionAnalyzer valueDist2 = new ValueDistributionAnalyzer(new MetaModelInputColumn(
                new MutableColumn("col")), true);

        valueDist2.runInternal(new MockInputRow(), "hello", 5);
        valueDist2.runInternal(new MockInputRow(), "hello", 1);
        valueDist2.runInternal(new MockInputRow(), "world", 7);
        valueDist2.runInternal(new MockInputRow(), "locallyUniqueWord", 1);
        valueDist2.runInternal(new MockInputRow(), "globallyUniqueWord", 1);
        ValueDistributionAnalyzerResult partialResult2 = valueDist2.getResult();

        ValueCountList partialTopValues2 = ((SingleValueDistributionResult) partialResult2).getTopValues();
        assertEquals(2, partialTopValues2.getActualSize());
        assertEquals("[world->7]", partialTopValues2.getValueCounts().get(0).toString());
        assertEquals("[hello->6]", partialTopValues2.getValueCounts().get(1).toString());

        assertEquals(0, partialResult2.getNullCount());
        assertEquals(2, partialResult2.getUniqueCount().intValue());

        List<ValueDistributionAnalyzerResult> partialResults = new ArrayList<>();
        partialResults.add(partialResult1);
        partialResults.add(partialResult2);

        ValueDistributionAnalyzerResultReducer reducer = new ValueDistributionAnalyzerResultReducer();
        ValueDistributionAnalyzerResult reducedResult = reducer.reduce(partialResults);

        SingleValueDistributionResult singleReducedResult = (SingleValueDistributionResult) reducedResult;
        assertEquals(Integer.valueOf(4), singleReducedResult.getDistinctCount());
        assertEquals(21, singleReducedResult.getTotalCount());
        assertEquals(Integer.valueOf(1), singleReducedResult.getUniqueCount());
        assertEquals("[globallyUniqueWord]", singleReducedResult.getUniqueValues().toString());
        ValueCountList reducedTopValues = singleReducedResult.getTopValues();
        assertEquals(2, reducedTopValues.getActualSize());
        assertEquals("[world->10]", reducedTopValues.getValueCounts().get(0).toString());
        assertEquals("[hello->8]", reducedTopValues.getValueCounts().get(1).toString());
    }

    @Test
    public void testReduceGroupedResults() throws Exception {
        final ValueDistributionAnalyzer valueDist1 = new ValueDistributionAnalyzer(new MetaModelInputColumn(
                new MutableColumn("col")),
                new MetaModelInputColumn(new MutableColumn("groupCol", ColumnType.STRING)).narrow(String.class), true);

        valueDist1.runInternal(new MockInputRow(), "hello", "group1", 1);
        valueDist1.runInternal(new MockInputRow(), "hello", "group2", 1);
        valueDist1.runInternal(new MockInputRow(), "world", "group1", 3);
        valueDist1.runInternal(new MockInputRow(), "locallyUniqueWord", "group1", 1);
        final ValueDistributionAnalyzerResult partialResult1 = valueDist1.getResult();

        final Collection<? extends ValueCountingAnalyzerResult> partialSingleResultList1 = ((GroupedValueDistributionResult) partialResult1)
                .getGroupResults();
        assertEquals(2, partialSingleResultList1.size());
        final Iterator<? extends ValueCountingAnalyzerResult> iterator = partialSingleResultList1.iterator();
        final SingleValueDistributionResult firstGroup = (SingleValueDistributionResult) iterator.next();
        assertEquals("group1", firstGroup.getName());
        final SingleValueDistributionResult secondGroup = (SingleValueDistributionResult) iterator.next();
        assertEquals("group2", secondGroup.getName());

        assertEquals(0, partialResult1.getNullCount());
        assertEquals(2, partialResult1.getUniqueCount().intValue());
        assertTrue(partialResult1.getUniqueValues().contains("hello"));
        assertTrue(partialResult1.getUniqueValues().contains("locallyUniqueWord"));
        assertEquals(6, partialResult1.getTotalCount());

        ValueDistributionAnalyzer valueDist2 = new ValueDistributionAnalyzer(new MetaModelInputColumn(
                new MutableColumn("col")),
                new MetaModelInputColumn(new MutableColumn("groupCol", ColumnType.STRING)).narrow(String.class), true);

        valueDist2.runInternal(new MockInputRow(), "hello", "group1", 5);
        valueDist2.runInternal(new MockInputRow(), "hello", "group1", 1);
        valueDist2.runInternal(new MockInputRow(), "world", "group2", 7);
        valueDist2.runInternal(new MockInputRow(), "locallyUniqueWord", "group1", 1);
        valueDist2.runInternal(new MockInputRow(), "globallyUniqueWord", "group1", 1);
        ValueDistributionAnalyzerResult partialResult2 = valueDist2.getResult();

        final Collection<? extends ValueCountingAnalyzerResult> partialSingleResultList2 = ((GroupedValueDistributionResult) partialResult1)
                .getGroupResults();
        assertEquals(2, partialSingleResultList2.size());
        final Iterator<? extends ValueCountingAnalyzerResult> iterator2 = partialSingleResultList2.iterator();
        final SingleValueDistributionResult firstGroup2 = (SingleValueDistributionResult) iterator2.next();
        assertEquals("group1", firstGroup2.getName());
        final SingleValueDistributionResult secondGroup2 = (SingleValueDistributionResult) iterator2.next();
        assertEquals("group2", secondGroup2.getName());

        assertEquals(0, partialResult2.getNullCount());
        assertEquals(2, partialResult2.getUniqueCount().intValue());
        assertTrue(partialResult2.getUniqueValues().contains("globallyUniqueWord"));
        assertTrue(partialResult2.getUniqueValues().contains("locallyUniqueWord"));
        assertEquals(15, partialResult2.getTotalCount());

        List<ValueDistributionAnalyzerResult> partialResults = new ArrayList<>();
        partialResults.add(partialResult1);
        partialResults.add(partialResult2);

        ValueDistributionAnalyzerResultReducer reducer = new ValueDistributionAnalyzerResultReducer();
        ValueDistributionAnalyzerResult reducedResult = reducer.reduce(partialResults);

        GroupedValueDistributionResult groupedReducedResult = (GroupedValueDistributionResult) reducedResult;
        assertEquals(Integer.valueOf(4), groupedReducedResult.getDistinctCount());
        assertEquals(21, groupedReducedResult.getTotalCount());
        assertEquals(Integer.valueOf(1), groupedReducedResult.getUniqueCount());
        assertEquals("[globallyUniqueWord]", groupedReducedResult.getUniqueValues().toString());
    }

}
