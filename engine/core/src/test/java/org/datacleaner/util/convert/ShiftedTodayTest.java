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
package org.datacleaner.util.convert;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

public class ShiftedTodayTest {
    @Test
    public void testOutput() throws Exception {
        for (int i = 0; i < 10; i++) {
            final Date x = getTodayPlus(i, 0, 0);
            x.setTime(x.getTime() + i * 10000);
            System.out.println(x.toString());
        }
    }

    @Test
    public void testMonthChangeByDays() throws Exception {
        final ShiftedToday shiftedToday = new ShiftedToday("42d0m0y");
        assertEquals(getTodayPlus(42, 0, 0), shiftedToday);
    }

    @Test
    public void testNegativeShifts() throws Exception {
        final ShiftedToday shiftedToday = new ShiftedToday("-3d-2m-1y");
        assertEquals(getTodayPlus(-3, -2, -1), shiftedToday);
    }

    @Test(expected = RuntimeException.class)
    public void testEmpty() throws Exception {
        final String input = "";
        new ShiftedToday(input);
    }

    @Test
    public void testExpression() throws Exception {
        final String shift = "-0d+0m+0y";
        final String input = "shifted_today(" + shift + ")";
        final ShiftedToday shiftedToday = new ShiftedToday(input);
        assertEquals(shift, shiftedToday.getInput());
    }

    @Test
    public void testZeroShift() throws Exception {
        final ShiftedToday shiftedToday = new ShiftedToday("0d0m0y");
        assertEquals(getTodayPlus(0, 0, 0), shiftedToday);
    }

    @Test
    public void testPlus() throws Exception {
        final ShiftedToday shiftedToday = new ShiftedToday("+1d0m0y");
        assertEquals(getTodayPlus(1, 0, 0), shiftedToday);
    }

    @Test
    public void testMinus() throws Exception {
        final ShiftedToday shiftedToday = new ShiftedToday("0d0m-1y");
        assertEquals(getTodayPlus(0, 0, -1), shiftedToday);
    }

    @Test
    public void testSpacesInInput() throws Exception {
        final ShiftedToday shiftedToday = new ShiftedToday("   0d +0m   -0y  ");
        assertEquals(getTodayPlus(0, 0, 0), shiftedToday);
    }

    private Date getTodayPlus(final int days, final int months, final int years) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        calendar.add(Calendar.MONTH, months);
        calendar.add(Calendar.YEAR, years);

        return calendar.getTime();
    }
}
