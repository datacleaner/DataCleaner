/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.monitor.server;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.eobjects.metamodel.util.DateUtils;
import org.eobjects.metamodel.util.Month;
import org.quartz.CronExpression;

import com.ibm.icu.text.SimpleDateFormat;

public class SchedulingServiceImplTest extends TestCase {
    
    public void testToCronExpressionYearly() throws Exception {
        CronExpression dailyExpr = SchedulingServiceImpl.toCronExpression("@yearly");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.add(Calendar.YEAR, 1);

        assertEquals(cal.getTime(), dailyExpr.getNextValidTimeAfter(new Date()));
    }

    public void testToCronExpressionMonthly() throws Exception {
        CronExpression dailyExpr = SchedulingServiceImpl.toCronExpression("@monthly");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.DATE, 1);
        cal.add(Calendar.MONTH, 1);

        assertEquals(cal.getTime(), dailyExpr.getNextValidTimeAfter(new Date()));
    }

    public void testToCronExpressionWeekly() throws Exception {
        CronExpression dailyExpr = SchedulingServiceImpl.toCronExpression("@weekly");
        Date callTime = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(callTime);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        while (cal.get(Calendar.DAY_OF_WEEK) != cal.getFirstDayOfWeek()) {
            cal.add(Calendar.DATE, 1);
        }

        assertEquals(cal.getTime(), dailyExpr.getNextValidTimeAfter(callTime));

        callTime = DateUtils.get(2012, Month.MARCH, 21);
        assertEquals("2012-03-25", new SimpleDateFormat("yyyy-MM-dd").format(dailyExpr.getNextValidTimeAfter(callTime)));
        
        callTime = DateUtils.get(2012, Month.MARCH, 24);
        assertEquals("2012-03-25", new SimpleDateFormat("yyyy-MM-dd").format(dailyExpr.getNextValidTimeAfter(callTime)));
        
        callTime = DateUtils.get(2012, Month.MARCH, 25);
        assertEquals("2012-03-25", new SimpleDateFormat("yyyy-MM-dd").format(dailyExpr.getNextValidTimeAfter(callTime)));
        
        callTime = DateUtils.get(2012, Month.MARCH, 26);
        assertEquals("2012-04-01", new SimpleDateFormat("yyyy-MM-dd").format(dailyExpr.getNextValidTimeAfter(callTime)));
    }

    public void testToCronExpressionDaily() throws Exception {
        CronExpression dailyExpr = SchedulingServiceImpl.toCronExpression("@daily");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.add(Calendar.DATE, 1);

        assertEquals(cal.getTime(), dailyExpr.getNextValidTimeAfter(new Date()));
    }

    public void testToCronExpressionHourly() throws Exception {
        CronExpression dailyExpr = SchedulingServiceImpl.toCronExpression("@hourly");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.add(Calendar.HOUR_OF_DAY, 1);

        assertEquals(cal.getTime(), dailyExpr.getNextValidTimeAfter(new Date()));
    }
    
    public void testToCronExpressionMinutely() throws Exception {
        CronExpression dailyExpr = SchedulingServiceImpl.toCronExpression("@minutely");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.add(Calendar.MINUTE, 1);

        assertEquals(cal.getTime(), dailyExpr.getNextValidTimeAfter(new Date()));
    }
}
