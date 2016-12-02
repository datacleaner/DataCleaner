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

import org.apache.metamodel.util.DateUtils;
import org.apache.metamodel.util.Month;
import org.datacleaner.api.InputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.result.CrosstabResult;
import org.datacleaner.result.renderer.CrosstabTextRenderer;

import junit.framework.TestCase;

public class DatePartDistributionResultReducerTest extends TestCase {

    private final DatePartDistributionResultReducer reducer = new DatePartDistributionResultReducer();

    @SuppressWarnings("unchecked")
    public void testReduce() throws Exception {
        final InputColumn<Date> col1 = new MockInputColumn<>("from");
        final InputColumn<Date> col2 = new MockInputColumn<>("to");

        YearDistributionAnalyzer analyzer;

        analyzer = new YearDistributionAnalyzer();
        analyzer.dateColumns = new InputColumn[] { col1, col2 };
        analyzer.init();
        analyzer.run(new MockInputRow().put(col1, getADate(2012)).put(col2, getADate(2013)), 1);
        analyzer.run(new MockInputRow().put(col1, getADate(2010)).put(col2, getADate(2010)), 1);
        final CrosstabResult result1 = analyzer.getResult();

        analyzer = new YearDistributionAnalyzer();
        analyzer.dateColumns = new InputColumn[] { col1, col2 };
        analyzer.init();
        analyzer.run(new MockInputRow().put(col1, getADate(2012)).put(col2, getADate(2012)), 1);
        analyzer.run(new MockInputRow().put(col1, getADate(2010)).put(col2, getADate(2010)), 1);
        analyzer.run(new MockInputRow().put(col1, getADate(2010)).put(col2, getADate(2011)), 1);
        final CrosstabResult result2 = analyzer.getResult();

        final CrosstabResult finalResult = reducer.reduce(Arrays.asList(result1, result2));

        final String text = new CrosstabTextRenderer().render(finalResult);
        final String[] lines = text.split("\n");
        assertEquals("       from     to ", lines[0]);
        assertEquals("2010      3      2 ", lines[1]);
        assertEquals("2011      0      1 ", lines[2]);
        assertEquals("2012      2      1 ", lines[3]);
        assertEquals("2013      0      1 ", lines[4]);

        assertEquals(5, lines.length);
    }

    private Date getADate(final int year) {
        return DateUtils.get(year, Month.JANUARY, 31);
    }
}
