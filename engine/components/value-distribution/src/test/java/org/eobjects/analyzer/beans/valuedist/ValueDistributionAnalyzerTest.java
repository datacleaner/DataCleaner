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
package org.eobjects.analyzer.beans.valuedist;

import java.util.Collection;
import java.util.Set;

import junit.framework.TestCase;

import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.descriptors.MetricDescriptor;
import org.eobjects.analyzer.descriptors.MetricParameters;
import org.eobjects.analyzer.result.GroupedValueCountingAnalyzerResult;
import org.eobjects.analyzer.result.ValueCountList;
import org.eobjects.analyzer.result.ValueCountingAnalyzerResult;
import org.apache.metamodel.schema.MutableColumn;

public class ValueDistributionAnalyzerTest extends TestCase {

    public void testDescriptor() throws Exception {
        AnalyzerBeanDescriptor<?> desc = Descriptors.ofAnalyzer(ValueDistributionAnalyzer.class);
        assertEquals(0, desc.getInitializeMethods().size());
        assertEquals(6, desc.getConfiguredProperties().size());
        assertEquals(2, desc.getProvidedProperties().size());
        assertEquals("Value distribution", desc.getDisplayName());
    }

    public void testGetCounts() throws Exception {
        ValueDistributionAnalyzer vd = new ValueDistributionAnalyzer(
                new MetaModelInputColumn(new MutableColumn("col")), true, null, null);

        assertEquals(0, vd.getResult().getUniqueCount().intValue());
        assertEquals(0, vd.getResult().getNullCount());
        assertEquals(0, vd.getResult().getDistinctCount().intValue());
        assertEquals(0, vd.getResult().getTotalCount());

        vd.runInternal(new MockInputRow(), "hello", 1);
        assertEquals(1, vd.getResult().getUniqueCount().intValue());
        assertEquals(1, vd.getResult().getDistinctCount().intValue());
        assertEquals(1, vd.getResult().getTotalCount());

        vd.runInternal(new MockInputRow(), "world", 1);
        assertEquals(2, vd.getResult().getUniqueCount().intValue());
        assertEquals(2, vd.getResult().getDistinctCount().intValue());
        assertEquals(2, vd.getResult().getTotalCount());

        vd.runInternal(new MockInputRow(), "foobar", 2);
        assertEquals(2, vd.getResult().getUniqueCount().intValue());
        assertEquals(3, vd.getResult().getDistinctCount().intValue());
        assertEquals(4, vd.getResult().getTotalCount());

        vd.runInternal(new MockInputRow(), "world", 1);
        assertEquals(1, vd.getResult().getUniqueCount().intValue());
        assertEquals(3, vd.getResult().getDistinctCount().intValue());
        assertEquals(5, vd.getResult().getTotalCount());

        vd.runInternal(new MockInputRow(), "hello", 3);
        assertEquals(0, vd.getResult().getUniqueCount().intValue());
        assertEquals(3, vd.getResult().getDistinctCount().intValue());
        assertEquals(8, vd.getResult().getTotalCount());

        vd.runInternal(new MockInputRow(), null, 1);
        assertEquals(0, vd.getResult().getUniqueCount().intValue());
        assertEquals(1, vd.getResult().getNullCount());
        assertEquals(4, vd.getResult().getDistinctCount().intValue());
        assertEquals(9, vd.getResult().getTotalCount());

        vd.runInternal(new MockInputRow(), null, 3);
        assertEquals(0, vd.getResult().getUniqueCount().intValue());
        assertEquals(4, vd.getResult().getNullCount());
        assertEquals(4, vd.getResult().getDistinctCount().intValue());
        assertEquals(12, vd.getResult().getTotalCount());

    }

    public void testGetValueCountMetric() throws Exception {
        ValueDistributionAnalyzer vd = new ValueDistributionAnalyzer(
                new MetaModelInputColumn(new MutableColumn("col")), true, null, null);
        vd.runInternal(new MockInputRow(), "hello", 1);
        vd.runInternal(new MockInputRow(), "world", 1);
        vd.runInternal(new MockInputRow(), "foobar", 2);
        vd.runInternal(new MockInputRow(), "world", 1);
        vd.runInternal(new MockInputRow(), "hello", 3);
        vd.runInternal(new MockInputRow(), null, 1);
        vd.runInternal(new MockInputRow(), null, 3);

        final ValueCountingAnalyzerResult result = vd.getResult();

        final AnalyzerBeanDescriptor<?> desc = Descriptors.ofAnalyzer(ValueDistributionAnalyzer.class);
        
        final MetricDescriptor metric = desc.getResultMetric("Value count");
        Collection<String> suggestions = metric.getMetricParameterSuggestions(result);
        assertEquals("[hello, foobar, world]", suggestions.toString());

        assertEquals(4, metric.getValue(result, new MetricParameters("hello")));
        assertEquals(2, metric.getValue(result, new MetricParameters("world")));
        assertEquals(6, metric.getValue(result, new MetricParameters("IN [hello,world]")));
        assertEquals(8, metric.getValue(result, new MetricParameters("NOT IN [foobar,world]")));
    }

    public void testGetValueDistribution() throws Exception {
        ValueDistributionAnalyzer vd = new ValueDistributionAnalyzer(
                new MetaModelInputColumn(new MutableColumn("col")), true, null, null);

        vd.runInternal(new MockInputRow(), "hello", 1);
        vd.runInternal(new MockInputRow(), "hello", 1);
        vd.runInternal(new MockInputRow(), "world", 3);

        ValueCountingAnalyzerResult result = vd.getResult();
        
        ValueCountList topValues = ((SingleValueDistributionResult)result).getTopValues();
        assertEquals(2, topValues.getActualSize());
        assertEquals("[world->3]", topValues.getValueCounts().get(0).toString());
        assertEquals("[hello->2]", topValues.getValueCounts().get(1).toString());

        assertEquals(0, result.getNullCount());
        assertEquals(0, result.getUniqueCount().intValue());

        String[] resultLines = result.toString().split("\n");
        assertEquals(3, resultLines.length);
        assertEquals("Value distribution for: col", resultLines[0]);
        assertEquals(" - world: 3", resultLines[1]);
        assertEquals(" - hello: 2", resultLines[2]);
    }

    public void testGroupedRun() throws Exception {
        ValueDistributionAnalyzer vd = new ValueDistributionAnalyzer(new MockInputColumn<String>("foo", String.class),
                new MockInputColumn<String>("bar", String.class), true, null, null);

        vd.runInternal(new MockInputRow(), "Copenhagen N", "2200", 3);
        vd.runInternal(new MockInputRow(), "Copenhagen E", "2100", 2);
        vd.runInternal(new MockInputRow(), "Copenhagen", "1732", 4);
        vd.runInternal(new MockInputRow(), "Coppenhagen", "1732", 3);

        ValueCountingAnalyzerResult result = vd.getResult();
        assertTrue(result instanceof GroupedValueCountingAnalyzerResult);

        String resultString = result.toString();
        System.out.println(resultString);
        String[] resultLines = resultString.split("\n");
        assertEquals(11, resultLines.length);

        assertEquals("Value distribution for column: foo", resultLines[0]);
        
        int i=0;
        assertEquals("Value distribution for column: foo", resultLines[i++]);
        assertEquals("", resultLines[i++]);
        assertEquals("Group: 1732", resultLines[i++]);
        assertEquals(" - Copenhagen: 4", resultLines[i++]);
        assertEquals(" - Coppenhagen: 3", resultLines[i++]);
        assertEquals("", resultLines[i++]);
        assertEquals("Group: 2100", resultLines[i++]);
        assertEquals(" - Copenhagen E: 2", resultLines[i++]);
        assertEquals("", resultLines[i++]);
        assertEquals("Group: 2200", resultLines[i++]);
        assertEquals(" - Copenhagen N: 3", resultLines[i++]);
    }
}
