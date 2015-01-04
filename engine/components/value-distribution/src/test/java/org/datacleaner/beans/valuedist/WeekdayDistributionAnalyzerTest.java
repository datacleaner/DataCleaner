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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.datacleaner.data.InputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.descriptors.AnalyzerBeanDescriptor;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.result.CrosstabResult;
import org.datacleaner.result.renderer.CrosstabTextRenderer;

import junit.framework.TestCase;

public class WeekdayDistributionAnalyzerTest extends TestCase {
    
    public void testDescriptorIsDistributable() throws Exception {
        AnalyzerBeanDescriptor<WeekdayDistributionAnalyzer> desc = Descriptors.ofAnalyzer(WeekdayDistributionAnalyzer.class);
        
        assertTrue(desc.isDistributable());
    }
    
	public void testTypicalUsage() throws Exception {
		WeekdayDistributionAnalyzer analyzer = new WeekdayDistributionAnalyzer();

		@SuppressWarnings("unchecked")
		InputColumn<Date>[] dateColumns = new InputColumn[3];
		dateColumns[0] = new MockInputColumn<Date>("Order date", Date.class);
		dateColumns[1] = new MockInputColumn<Date>("Shipment date", Date.class);
		dateColumns[2] = new MockInputColumn<Date>("Delivery date", Date.class);

		analyzer.setDateColumns(dateColumns);
		analyzer.init();

		// 1x: friday, saturday, tuesday
		analyzer.run(new MockInputRow(dateColumns, new Object[] { d(2010, 1, 1), d(2010, 1, 2), d(2010, 1, 5) }), 1);
		// 2x: monday, tuesday, friday
		analyzer.run(new MockInputRow(dateColumns, new Object[] { d(2010, 2, 1), d(2010, 2, 2), d(2010, 2, 5) }), 2);
		// 1x: thursday, friday, monday
		analyzer.run(new MockInputRow(dateColumns, new Object[] { d(2010, 4, 1), d(2010, 4, 2), d(2010, 4, 5) }), 1);

		CrosstabResult result = analyzer.getResult();

		String[] resultLines = new CrosstabTextRenderer().render(result).split("\n");
		assertEquals(8, resultLines.length);
		assertEquals("             Order date Shipment date Delivery date ", resultLines[0]);
		assertEquals("Sunday                0             0             0 ", resultLines[1]);
		assertEquals("Monday                2             0             1 ", resultLines[2]);
		assertEquals("Tuesday               0             2             1 ", resultLines[3]);
		assertEquals("Wednesday             0             0             0 ", resultLines[4]);
		assertEquals("Thursday              1             0             0 ", resultLines[5]);
		assertEquals("Friday                1             1             2 ", resultLines[6]);
		assertEquals("Saturday              0             1             0 ", resultLines[7]);
	}

	public void testDateGen() throws Exception {
		Date d = d(2010, 1, 1);
		assertEquals("2010-01-01", new SimpleDateFormat("yyyy-MM-dd").format(d));
	}

	private Date d(int year, int month, int date) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(0l);
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month - 1);
		c.set(Calendar.DAY_OF_MONTH, date);
		return c.getTime();
	}

}
