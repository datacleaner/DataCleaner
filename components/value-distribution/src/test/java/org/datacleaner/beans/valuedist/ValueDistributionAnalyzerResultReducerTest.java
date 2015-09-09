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
        ValueDistributionAnalyzerResult partialResult1 = valueDist1.getResult();

        ValueCountList partialTopValues1 = ((SingleValueDistributionResult) partialResult1).getTopValues();
        assertEquals(2, partialTopValues1.getActualSize());
        assertEquals("[world->3]", partialTopValues1.getValueCounts().get(0).toString());
        assertEquals("[hello->2]", partialTopValues1.getValueCounts().get(1).toString());

        assertEquals(0, partialResult1.getNullCount());
        assertEquals(0, partialResult1.getUniqueCount().intValue());
        
        ValueDistributionAnalyzer valueDist2 = new ValueDistributionAnalyzer(
                new MetaModelInputColumn(new MutableColumn("col")), true);
        
        valueDist2.runInternal(new MockInputRow(), "hello", 5);
        valueDist2.runInternal(new MockInputRow(), "hello", 1);
        valueDist2.runInternal(new MockInputRow(), "world", 7);
        ValueDistributionAnalyzerResult partialResult2 = valueDist2.getResult();

        ValueCountList partialTopValues2 = ((SingleValueDistributionResult) partialResult2).getTopValues();
        assertEquals(2, partialTopValues2.getActualSize());
        assertEquals("[world->7]", partialTopValues2.getValueCounts().get(0).toString());
        assertEquals("[hello->6]", partialTopValues2.getValueCounts().get(1).toString());

        assertEquals(0, partialResult2.getNullCount());
        assertEquals(0, partialResult2.getUniqueCount().intValue());
        
        List<ValueDistributionAnalyzerResult> partialResults = new ArrayList<>();
        partialResults.add(partialResult1);
        partialResults.add(partialResult2);
        
        ValueDistributionAnalyzerResultReducer reducer = new ValueDistributionAnalyzerResultReducer();
        ValueDistributionAnalyzerResult reducedResult = reducer.reduce(partialResults);
        
        ValueCountList reducedTopValues = ((SingleValueDistributionResult) reducedResult).getTopValues();
        assertEquals(2, reducedTopValues.getActualSize());
        assertEquals("[world->10]", reducedTopValues.getValueCounts().get(0).toString());
        assertEquals("[hello->8]", reducedTopValues.getValueCounts().get(1).toString());
    }

}
