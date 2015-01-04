/**
 * AnalyzerBeans
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

import java.util.Arrays;
import java.util.Date;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.renderer.CrosstabTextRenderer;
import org.apache.metamodel.util.DateUtils;
import org.apache.metamodel.util.Month;

import junit.framework.TestCase;

public class MonthDistributionResultReducerTest extends TestCase {

    private final MonthDistributionResultReducer reducer = new MonthDistributionResultReducer();

    @SuppressWarnings("unchecked")
    public void testReduce() throws Exception {
        final InputColumn<Date> col1 = new MockInputColumn<Date>("from");
        final InputColumn<Date> col2 = new MockInputColumn<Date>("to");

        MonthDistributionAnalyzer analyzer;

        analyzer = new MonthDistributionAnalyzer();
        analyzer.dateColumns = new InputColumn[] { col1, col2 };
        analyzer.init();
        analyzer.run(new MockInputRow().put(col1, getADate(Month.SEPTEMBER)).put(col2, getADate(Month.NOVEMBER)), 1);
        analyzer.run(new MockInputRow().put(col1, getADate(Month.JUNE)).put(col2, getADate(Month.JULY)), 1);
        final CrosstabResult result1 = analyzer.getResult();

        analyzer = new MonthDistributionAnalyzer();
        analyzer.dateColumns = new InputColumn[] { col1, col2 };
        analyzer.init();
        analyzer.run(new MockInputRow().put(col1, getADate(Month.NOVEMBER)).put(col2, getADate(Month.DECEMBER)), 1);
        analyzer.run(new MockInputRow().put(col1, getADate(Month.SEPTEMBER)).put(col2, getADate(Month.SEPTEMBER)), 1);
        analyzer.run(new MockInputRow().put(col1, getADate(Month.FEBRUARY)).put(col2, getADate(Month.MARCH)), 1);
        final CrosstabResult result2 = analyzer.getResult();

        final CrosstabResult finalResult = reducer.reduce(Arrays.asList(result1, result2));

        final String text = new CrosstabTextRenderer().render(finalResult);
        
        final String[] lines = text.split("\n");
        assertEquals("            from     to ", lines[0]);
        assertEquals("January        0      0 ", lines[1]);
        assertEquals("February       0      0 ", lines[2]);
        assertEquals("March          1      1 ", lines[3]);
        assertEquals("April          0      0 ", lines[4]);
        assertEquals("May            0      0 ", lines[5]);
        assertEquals("June           0      0 ", lines[6]);
        assertEquals("July           1      1 ", lines[7]);
        assertEquals("August         0      0 ", lines[8]);
        assertEquals("September      0      0 ", lines[9]);
        assertEquals("October        2      1 ", lines[10]);
        assertEquals("November       0      0 ", lines[11]);
        assertEquals("December       1      2 ", lines[12]);

        assertEquals(13, lines.length);
    }

    private Date getADate(Month month) {
        return DateUtils.get(2013, month, 31);
    }
}
