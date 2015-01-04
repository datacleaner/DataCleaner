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

import java.util.Arrays;
import java.util.Date;

import org.datacleaner.data.InputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.result.CrosstabResult;
import org.datacleaner.result.renderer.CrosstabTextRenderer;
import org.apache.metamodel.util.DateUtils;
import org.apache.metamodel.util.Month;

import junit.framework.TestCase;

public class WeekdayDistributionResultReducerTest extends TestCase {

    private final WeekdayDistributionResultReducer reducer = new WeekdayDistributionResultReducer();

    @SuppressWarnings("unchecked")
    public void testReduce() throws Exception {
        final InputColumn<Date> col1 = new MockInputColumn<Date>("from");
        final InputColumn<Date> col2 = new MockInputColumn<Date>("to");

        WeekdayDistributionAnalyzer analyzer;

        analyzer = new WeekdayDistributionAnalyzer();
        analyzer.dateColumns = new InputColumn[] { col1, col2 };
        analyzer.init();
        analyzer.run(new MockInputRow().put(col1, getADate(Month.SEPTEMBER)).put(col2, getADate(Month.NOVEMBER)), 1);
        analyzer.run(new MockInputRow().put(col1, getADate(Month.JUNE)).put(col2, getADate(Month.JULY)), 1);
        final CrosstabResult result1 = analyzer.getResult();

        analyzer = new WeekdayDistributionAnalyzer();
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
        assertEquals("Sunday         2      2 ", lines[1]);
        assertEquals("Monday         1      0 ", lines[2]);
        assertEquals("Tuesday        2      2 ", lines[3]);
        assertEquals("Wednesday      0      1 ", lines[4]);
        assertEquals("Thursday       0      0 ", lines[5]);
        assertEquals("Friday         0      0 ", lines[6]);
        assertEquals("Saturday       0      0 ", lines[7]);

        assertEquals(8, lines.length);
    }

    private Date getADate(Month month) {
        return DateUtils.get(2013, month, 31);
    }
}
