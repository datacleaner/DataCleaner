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

import java.util.ArrayList;
import java.util.List;

import org.apache.metamodel.schema.MutableColumn;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.result.ValueCountList;
import org.junit.Test;

public class ValueDistributionAnalyzerResultReducerTest {

    @Test
    public void testReduceSingleResults() throws Exception {
        ValueDistributionAnalyzer valueDist1 = new ValueDistributionAnalyzer(
                new MetaModelInputColumn(new MutableColumn("col")), true);

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
        
        ValueDistributionAnalyzer valueDist2 = new ValueDistributionAnalyzer(
                new MetaModelInputColumn(new MutableColumn("col")), true);
        
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

}
