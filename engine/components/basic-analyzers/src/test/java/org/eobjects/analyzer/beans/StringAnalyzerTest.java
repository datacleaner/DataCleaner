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
package org.eobjects.analyzer.beans;

import java.util.Set;

import javax.swing.table.TableModel;

import junit.framework.TestCase;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.descriptors.MetricDescriptor;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.renderer.CrosstabTextRenderer;

@SuppressWarnings("deprecation")
public class StringAnalyzerTest extends TestCase {

    private InputColumn<String> c1;
    private InputColumn<String> c2;
    private StringAnalyzer stringAnalyzer;

    @SuppressWarnings("unchecked")
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        c1 = new MockInputColumn<String>("greetings", String.class);
        c2 = new MockInputColumn<String>("greeters", String.class);
        stringAnalyzer = new StringAnalyzer(c1, c2);
    }

    public void testTypicalExample() throws Exception {
        stringAnalyzer.run(new MockInputRow().put(c1, "Hello").put(c2, "world"), 1);
        stringAnalyzer.run(new MockInputRow().put(c1, "howdy").put(c2, "the universe"), 1);
        stringAnalyzer.run(new MockInputRow().put(c1, "Hey").put(c2, "country"), 1);
        stringAnalyzer.run(new MockInputRow().put(c1, "hi").put(c2, "stranger"), 1);

        CrosstabResult result = stringAnalyzer.getResult();

        assertEquals(Number.class, result.getCrosstab().getValueClass());

        String renderedResult = new CrosstabTextRenderer().render(result);
        String[] resultLines = renderedResult.split("\n");
        assertEquals(22, resultLines.length);

        int i=0;
        assertEquals("                                      greetings  greeters ", resultLines[i++]);
        assertEquals("Row count                                     4         4 ", resultLines[i++]);
        assertEquals("Null count                                    0         0 ", resultLines[i++]);
        assertEquals("Blank count                                   0         0 ", resultLines[i++]);
        assertEquals("Entirely uppercase count                      0         0 ", resultLines[i++]);
        assertEquals("Entirely lowercase count                      2         4 ", resultLines[i++]);
        assertEquals("Total char count                             15        32 ", resultLines[i++]);
        assertEquals("Max chars                                     5        12 ", resultLines[i++]);
        assertEquals("Min chars                                     2         5 ", resultLines[i++]);
        assertEquals("Avg chars                                  3.75         8 ", resultLines[i++]);
        assertEquals("Max white spaces                              0         1 ", resultLines[i++]);
        assertEquals("Min white spaces                              0         0 ", resultLines[i++]);
        assertEquals("Avg white spaces                              0      0.25 ", resultLines[i++]);
        assertEquals("Uppercase chars                               2         0 ", resultLines[i++]);
        assertEquals("Uppercase chars (excl. first letters)         0         0 ", resultLines[i++]);
        assertEquals("Lowercase chars                              13        31 ", resultLines[i++]);
        assertEquals("Digit chars                                   0         0 ", resultLines[i++]);
        assertEquals("Diacritic chars                               0         0 ", resultLines[i++]);
        assertEquals("Non-letter chars                              0         1 ", resultLines[i++]);
        assertEquals("Word count                                    4         5 ", resultLines[i++]);
        assertEquals("Max words                                     1         2 ", resultLines[i++]);
        assertEquals("Min words                                     1         1 ", resultLines[i++]);
    }

    public void testOddValuesWithMoreThanOneDistinctCount() throws Exception {
        // notice the non-1 "distinctCount"
        stringAnalyzer.run(new MockInputRow().put(c1, "HELLO").put(c2, "  "), 1);
        stringAnalyzer.run(new MockInputRow().put(c1, "HÖWDY").put(c2, null), 3);
        stringAnalyzer.run(new MockInputRow().put(c1, " HËJSÄN").put(c2, "eobjects.org"), 1);
        stringAnalyzer.run(new MockInputRow().put(c1, "SØREN SEN").put(c2, "- hi"), 4);

        StringAnalyzerResult result = stringAnalyzer.getResult();

        assertEquals(Number.class, result.getCrosstab().getValueClass());

        String renderedResult = new CrosstabTextRenderer().render(result);
        String[] resultLines = renderedResult.split("\n");
        assertEquals(22, resultLines.length);

        int i=0;
        assertEquals("                                      greetings  greeters ", resultLines[i++]);
        assertEquals("Row count                                     9         9 ", resultLines[i++]);
        assertEquals("Null count                                    0         3 ", resultLines[i++]);
        assertEquals("Blank count                                   0         0 ", resultLines[i++]);
        assertEquals("Entirely uppercase count                      9         0 ", resultLines[i++]);
        assertEquals("Entirely lowercase count                      0         5 ", resultLines[i++]);
        assertEquals("Total char count                             63        30 ", resultLines[i++]);
        assertEquals("Max chars                                     9        12 ", resultLines[i++]);
        assertEquals("Min chars                                     5         2 ", resultLines[i++]);
        assertEquals("Avg chars                                   6.5         6 ", resultLines[i++]);
        assertEquals("Max white spaces                              1         2 ", resultLines[i++]);
        assertEquals("Min white spaces                              0         0 ", resultLines[i++]);
        assertEquals("Avg white spaces                            0.5         1 ", resultLines[i++]);
        assertEquals("Uppercase chars                              58         0 ", resultLines[i++]);
        assertEquals("Uppercase chars (excl. first letters)        49         0 ", resultLines[i++]);
        assertEquals("Lowercase chars                               0        19 ", resultLines[i++]);
        assertEquals("Digit chars                                   0         0 ", resultLines[i++]);
        assertEquals("Diacritic chars                               9         0 ", resultLines[i++]);
        assertEquals("Non-letter chars                              5        11 ", resultLines[i++]);
        assertEquals("Word count                                   13         9 ", resultLines[i++]);
        assertEquals("Max words                                     2         2 ", resultLines[i++]);
        assertEquals("Min words                                     1         0 ", resultLines[i++]);

        AnnotatedRowsResult drillResult = (AnnotatedRowsResult) result.getCrosstab()
                .where("Measures", "Max white spaces").where("Column", "greetings").explore().getResult();
        assertEquals(5, drillResult.getAnnotation().getRowCount());

        TableModel tableModel = drillResult.toTableModel();

        // assert the default table model consists of the detailed rows
        assertEquals(2, tableModel.getColumnCount());
        assertEquals(2, tableModel.getRowCount());
        assertEquals("greetings", tableModel.getColumnName(0));
        assertEquals("greeters", tableModel.getColumnName(1));
        assertEquals(" HËJSÄN", tableModel.getValueAt(0, 0).toString());
        assertEquals("eobjects.org", tableModel.getValueAt(0, 1).toString());
        assertEquals("SØREN SEN", tableModel.getValueAt(1, 0).toString());
        assertEquals("- hi", tableModel.getValueAt(1, 1).toString());

        tableModel = drillResult.toDistinctValuesTableModel(c1);
        // assert the distinct values table model contains the greeings with
        // whitespaces and their counts
        assertEquals(2, tableModel.getColumnCount());
        assertEquals(2, tableModel.getColumnCount());
        assertEquals("greetings", tableModel.getColumnName(0));
        assertEquals("Count in dataset", tableModel.getColumnName(1));
        assertEquals("SØREN SEN", tableModel.getValueAt(0, 0));
        assertEquals(4, tableModel.getValueAt(0, 1));
        assertEquals(" HËJSÄN", tableModel.getValueAt(1, 0));
        assertEquals(1, tableModel.getValueAt(1, 1));
    }

    public void testNoRows() throws Exception {
        @SuppressWarnings("unchecked")
        StringAnalyzer stringAnalyzer = new StringAnalyzer(c1, c2);

        CrosstabResult result = stringAnalyzer.getResult();

        assertEquals(Number.class, result.getCrosstab().getValueClass());

        String renderedResult = new CrosstabTextRenderer().render(result);
        String[] resultLines = renderedResult.split("\n");
        assertEquals(22, resultLines.length);

        int i=0;
        assertEquals("                                      greetings  greeters ", resultLines[i++]);
        assertEquals("Row count                                     0         0 ", resultLines[i++]);
        assertEquals("Null count                                    0         0 ", resultLines[i++]);
        assertEquals("Blank count                                   0         0 ", resultLines[i++]);
        assertEquals("Entirely uppercase count                      0         0 ", resultLines[i++]);
        assertEquals("Entirely lowercase count                      0         0 ", resultLines[i++]);
        assertEquals("Total char count                              0         0 ", resultLines[i++]);
        assertEquals("Max chars                                <null>    <null> ", resultLines[i++]);
        assertEquals("Min chars                                <null>    <null> ", resultLines[i++]);
        assertEquals("Avg chars                                <null>    <null> ", resultLines[i++]);
        assertEquals("Max white spaces                         <null>    <null> ", resultLines[i++]);
        assertEquals("Min white spaces                         <null>    <null> ", resultLines[i++]);
        assertEquals("Avg white spaces                         <null>    <null> ", resultLines[i++]);
        assertEquals("Uppercase chars                               0         0 ", resultLines[i++]);
        assertEquals("Uppercase chars (excl. first letters)         0         0 ", resultLines[i++]);
        assertEquals("Lowercase chars                               0         0 ", resultLines[i++]);
        assertEquals("Digit chars                                   0         0 ", resultLines[i++]);
        assertEquals("Diacritic chars                               0         0 ", resultLines[i++]);
        assertEquals("Non-letter chars                              0         0 ", resultLines[i++]);
        assertEquals("Word count                                    0         0 ", resultLines[i++]);
        assertEquals("Max words                                <null>    <null> ", resultLines[i++]);
        assertEquals("Min words                                <null>    <null> ", resultLines[i++]);
    }

    public void testMetricDescriptor() throws Exception {
        AnalyzerBeanDescriptor<org.eobjects.analyzer.beans.StringAnalyzer> descriptor = Descriptors
                .ofAnalyzer(StringAnalyzer.class);
        MetricDescriptor metric = descriptor.getResultMetric(StringAnalyzer.MEASURE_ENTIRELY_LOWERCASE_COUNT);
        assertEquals("MetricDescriptorImpl[name=Entirely lowercase count]", metric.toString());
        assertTrue(metric.isParameterizedByInputColumn());
        assertFalse(metric.isParameterizedByString());
    }
}
