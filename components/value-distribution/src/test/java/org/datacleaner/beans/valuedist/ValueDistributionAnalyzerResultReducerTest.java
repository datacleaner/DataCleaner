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

import static org.junit.Assert.*;

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
        assertTrue(partialResult1.getUniqueValues().contains("locallyUniqueWord"));

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
        assertTrue(partialResult2.getUniqueValues().contains("locallyUniqueWord"));
        assertTrue(partialResult2.getUniqueValues().contains("globallyUniqueWord"));

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
        valueDist1.runInternal(new MockInputRow(), "world", "group1", 3);
        valueDist1.runInternal(new MockInputRow(), "locallyUniqueWord", "group1", 1);
        valueDist1.runInternal(new MockInputRow(), "hello", "group2", 1);
        final ValueDistributionAnalyzerResult partialResult1 = valueDist1.getResult();

        final Collection<? extends ValueCountingAnalyzerResult> partialSingleResultList1 = ((GroupedValueDistributionResult) partialResult1)
                .getGroupResults();
        // Confirm what we got from the the first analyzer...
        {
            assertEquals(2, partialSingleResultList1.size());
            final Iterator<? extends ValueCountingAnalyzerResult> iterator = partialSingleResultList1.iterator();
            final SingleValueDistributionResult group1Analyzer1 = (SingleValueDistributionResult) iterator.next();
            assertEquals("group1", group1Analyzer1.getName());
            assertEquals(0, group1Analyzer1.getNullCount());
            assertEquals(2, group1Analyzer1.getUniqueCount().intValue());
            assertTrue(group1Analyzer1.getUniqueValues().contains("hello"));
            assertTrue(group1Analyzer1.getUniqueValues().contains("locallyUniqueWord"));
            assertEquals(5, group1Analyzer1.getTotalCount());
            final SingleValueDistributionResult group2Analyzer1 = (SingleValueDistributionResult) iterator.next();
            assertEquals("group2", group2Analyzer1.getName());
            assertEquals(0, group2Analyzer1.getNullCount());
            assertEquals(1, group2Analyzer1.getUniqueCount().intValue());
            assertTrue(group2Analyzer1.getUniqueValues().contains("hello"));
            assertEquals(1, group2Analyzer1.getTotalCount());
        }

        ValueDistributionAnalyzer valueDist2 = new ValueDistributionAnalyzer(new MetaModelInputColumn(
                new MutableColumn("col")),
                new MetaModelInputColumn(new MutableColumn("groupCol", ColumnType.STRING)).narrow(String.class), true);

        valueDist2.runInternal(new MockInputRow(), "hello", "group1", 6);
        valueDist2.runInternal(new MockInputRow(), "locallyUniqueWord", "group1", 1);
        valueDist2.runInternal(new MockInputRow(), "globallyUniqueWord", "group1", 1);
        valueDist2.runInternal(new MockInputRow(), "world", "group2", 7);
        ValueDistributionAnalyzerResult partialResult2 = valueDist2.getResult();

        // Confirm what we got from the the second analyzer...
        {
            final Collection<? extends ValueCountingAnalyzerResult> partialSingleResultList2 = ((GroupedValueDistributionResult) partialResult2)
                    .getGroupResults();
            assertEquals(2, partialSingleResultList2.size());
            final Iterator<? extends ValueCountingAnalyzerResult> iterator2 = partialSingleResultList2.iterator();
            final SingleValueDistributionResult group1Analyzer2 = (SingleValueDistributionResult) iterator2.next();
            assertEquals("group1", group1Analyzer2.getName());
            assertEquals(0, group1Analyzer2.getNullCount());
            assertEquals(2, group1Analyzer2.getUniqueCount().intValue());
            assertTrue(group1Analyzer2.getUniqueValues().contains("globallyUniqueWord"));
            assertTrue(group1Analyzer2.getUniqueValues().contains("locallyUniqueWord"));
            assertEquals(8, group1Analyzer2.getTotalCount());
            final SingleValueDistributionResult group2Analyzer2 = (SingleValueDistributionResult) iterator2.next();
            assertEquals("group2", group2Analyzer2.getName());
            assertEquals(0, group2Analyzer2.getNullCount());
            assertEquals(0, group2Analyzer2.getUniqueCount().intValue());
            assertEquals(7, group2Analyzer2.getTotalCount());
        }

        List<ValueDistributionAnalyzerResult> partialResults = new ArrayList<>();
        partialResults.add(partialResult1);
        partialResults.add(partialResult2);

        ValueDistributionAnalyzerResultReducer reducer = new ValueDistributionAnalyzerResultReducer();
        ValueDistributionAnalyzerResult reducedResult = reducer.reduce(partialResults);

        // Assert the aggregates from the reduced groups
        {
            final GroupedValueDistributionResult groupedReducedResult = (GroupedValueDistributionResult) reducedResult;
            final Iterator<? extends ValueCountingAnalyzerResult> reducedGroupsIterator = groupedReducedResult
                    .getGroupResults().iterator();
            SingleValueDistributionResult firstReducedGroup = (SingleValueDistributionResult) reducedGroupsIterator
                    .next();
            assertEquals("group1", firstReducedGroup.getName());
            assertEquals(0, firstReducedGroup.getNullCount());
            assertEquals(2, firstReducedGroup.getUniqueCount().intValue());
            assertTrue(firstReducedGroup.getUniqueValues().contains("globallyUniqueWord"));
            assertTrue(firstReducedGroup.getUniqueValues().contains("hello"));
            assertEquals(13, firstReducedGroup.getTotalCount());

            SingleValueDistributionResult secondReducedGroup = (SingleValueDistributionResult) reducedGroupsIterator
                    .next();
            assertEquals("group2", secondReducedGroup.getName());
            assertEquals(0, secondReducedGroup.getNullCount());
            assertEquals(1, secondReducedGroup.getUniqueCount().intValue());
            assertTrue(secondReducedGroup.getUniqueValues().contains("hello"));
            assertEquals(8, secondReducedGroup.getTotalCount());
        }
    }

}
