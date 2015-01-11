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
package org.datacleaner.beans;

import org.datacleaner.api.InputColumn;
import org.datacleaner.api.ParameterizableMetric;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.result.renderer.CrosstabTextRenderer;

import junit.framework.TestCase;

public class BooleanAnalyzerTest extends TestCase {

    public void testSimpleScenario() throws Exception {
        @SuppressWarnings("unchecked")
        final InputColumn<Boolean>[] c = new InputColumn[2];
        c[0] = new MockInputColumn<Boolean>("b1", Boolean.class);
        c[1] = new MockInputColumn<Boolean>("b2", Boolean.class);

        final BooleanAnalyzer ba = new BooleanAnalyzer(c);
        ba.init();

        ba.run(new MockInputRow().put(c[0], true).put(c[1], true), 3);
        ba.run(new MockInputRow().put(c[0], true).put(c[1], true), 1);
        ba.run(new MockInputRow().put(c[0], true).put(c[1], false), 1);
        ba.run(new MockInputRow().put(c[0], false).put(c[1], true), 1);
        ba.run(new MockInputRow().put(c[0], false).put(c[1], true), 1);

        final BooleanAnalyzerResult result = ba.getResult();

        String[] resultLines = new CrosstabTextRenderer().render(result.getColumnStatisticsCrosstab()).split("\n");
        assertEquals(5, resultLines.length);
        assertEquals("                b1     b2 ", resultLines[0]);
        assertEquals("Row count        7      7 ", resultLines[1]);
        assertEquals("Null count       0      0 ", resultLines[2]);
        assertEquals("True count       5      6 ", resultLines[3]);
        assertEquals("False count      2      1 ", resultLines[4]);

        resultLines = new CrosstabTextRenderer().render(result.getValueCombinationCrosstab()).split("\n");
        assertEquals(4, resultLines.length);
        assertEquals("                      b1        b2 Frequency ", resultLines[0]);
        assertEquals("Most frequent          1         1         4 ", resultLines[1]);
        assertEquals("Combination 1          0         1         2 ", resultLines[2]);
        assertEquals("Least frequent         1         0         1 ", resultLines[3]);
    }

    public void testGetMetrics() throws Exception {
        @SuppressWarnings("unchecked")
        final InputColumn<Boolean>[] c = new InputColumn[2];
        c[0] = new MockInputColumn<Boolean>("b1", Boolean.class);
        c[1] = new MockInputColumn<Boolean>("b2", Boolean.class);

        final BooleanAnalyzer ba = new BooleanAnalyzer(c);
        ba.init();

        ba.run(new MockInputRow().put(c[0], true).put(c[1], true), 3);
        ba.run(new MockInputRow().put(c[0], true).put(c[1], true), 1);
        ba.run(new MockInputRow().put(c[0], true).put(c[1], false), 1);
        ba.run(new MockInputRow().put(c[0], false).put(c[1], true), 1);
        ba.run(new MockInputRow().put(c[0], false).put(c[1], true), 1);

        final BooleanAnalyzerResult result = ba.getResult();
        
        final ParameterizableMetric trueCountMetric = result.getTrueCount();
        assertEquals("[b1, b2]", trueCountMetric.getParameterSuggestions().toString());
        assertEquals(5, trueCountMetric.getValue("b1"));
        assertEquals(0, trueCountMetric.getValue("foobar"));

        final ParameterizableMetric combinationCountMetric = result.getCombinationCount();
        assertEquals("[Most frequent, Least frequent, true,true, false,true, true,false]", combinationCountMetric.getParameterSuggestions().toString());

        assertEquals(4, combinationCountMetric.getValue("Most frequent").intValue());
        assertEquals(2, combinationCountMetric.getValue("Combination 1").intValue());
        assertEquals(1, combinationCountMetric.getValue("Least frequent").intValue());
        assertEquals(0, combinationCountMetric.getValue("foobar").intValue());

        assertEquals(4, combinationCountMetric.getValue("true,true").intValue());
        assertEquals(2, combinationCountMetric.getValue("false,true").intValue());
        assertEquals(1, combinationCountMetric.getValue(" true , false ").intValue());
        assertEquals(0, combinationCountMetric.getValue("false,false").intValue());
        
        assertEquals(0, combinationCountMetric.getValue("false,foobar").intValue());
    }
}
