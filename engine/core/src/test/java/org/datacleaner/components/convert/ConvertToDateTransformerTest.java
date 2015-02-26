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
package org.datacleaner.components.convert;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.datacleaner.api.InputColumn;
import org.datacleaner.components.convert.ConvertToDateTransformer;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;

public class ConvertToDateTransformerTest extends TestCase {

    private static final String TEST_TIMEZONE = "CET";

    private SimpleDateFormat dateFormat;

    protected void setUp() throws Exception {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TEST_TIMEZONE));
    };

    public void testConvertWithNumberOnlyDateMask() throws Exception {
        ConvertToDateTransformer transformer = new ConvertToDateTransformer();
        transformer.timeZone = TEST_TIMEZONE;
        transformer.dateMasks = new String[] { "ddMMyyyy" };
        InputColumn<?> col = new MockInputColumn<Object>("datestr");
        transformer.input = new InputColumn[] { col };
        transformer.init();

        Date[] result;

        result = transformer.transform(new MockInputRow().put(col, "31012014"));
        assertEquals("2014-01-31", dateFormat.format(result[0]));

        result = transformer.transform(new MockInputRow().put(col, 31012014));
        assertEquals("2014-01-31", dateFormat.format(result[0]));
    }

    public void testConvertFromNumber() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 1971);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);
        cal.setTimeZone(TimeZone.getTimeZone(TEST_TIMEZONE));

        assertTrue(cal.getTime().getTime() > 5000000);

        ConvertToDateTransformer transformer = new ConvertToDateTransformer();
        transformer.timeZone = TEST_TIMEZONE;
        transformer.init();

        assertEquals("1971-01-01", format(transformer.transformValue(cal)));
        assertEquals("1971-01-01", format(transformer.transformValue(cal.getTime())));

        assertEquals("1970-04-03", format(transformer.convertFromNumber(8000000000l)));
        assertEquals("1997-05-19", format(transformer.convertFromNumber(10000)));
        assertEquals("1997-05-19", format(transformer.convertFromNumber(19970519)));
        assertEquals("1997-05-19", format(transformer.convertFromNumber(970519)));
    }

    public void testConvertFromString() throws Exception {
        ConvertToDateTransformer transformer = new ConvertToDateTransformer();
        transformer.timeZone = TEST_TIMEZONE;
        transformer.init();

        assertEquals("1999-04-20", format(transformer.convertFromString("1999-04-20")));
        assertEquals("1999-04-20", format(transformer.convertFromString("04/20/1999")));
        assertEquals("1999-04-20", format(transformer.convertFromString("1999/04/20")));
        assertEquals("2008-07-11", format(transformer.convertFromString("2008-07-11 00:00:00")));
        assertEquals("2012-04-26", format(transformer.convertFromString("2012-04-26 19:12:19.792012")));

        Date result = transformer.convertFromString("2008-07-11 14:05:13");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone(TEST_TIMEZONE));
        assertEquals("2008-07-11 14:05:13", format.format(result));
    }

    public void testConvertFromExpression() throws Exception {
        ConvertToDateTransformer transformer = new ConvertToDateTransformer();
        transformer.timeZone = TEST_TIMEZONE;
        transformer.init();

        final Date now = new Date();

        assertEquals(format(now), format(transformer.convertFromString("TODAY()")));
        assertEquals(format(now), format(transformer.convertFromString("NOW()")));

        final Date yesterDay = new Date(now.getTime() - 24 * 60 * 60 * 1000);

        assertEquals(format(yesterDay), format(transformer.convertFromString("YESTERDAY()")));
    }

    public void testConvertSingleHourDigit() throws Exception {
        ConvertToDateTransformer transformer = new ConvertToDateTransformer();
        transformer.timeZone = TEST_TIMEZONE;
        transformer.dateMasks = new String[] { "dd/MM/yyyy HH:mm" };
        transformer.init();

        Date date = transformer.convertFromString("29/3/2009 12:55");
        assertNotNull(date);

        date = transformer.convertFromString("29/3/2009 3:15");
        assertNotNull(date);
    }

    private String format(Date date) {
        assertNotNull("date is null", date);
        return dateFormat.format(date);
    }
}
