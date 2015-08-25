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
package org.datacleaner.beans.transform;

import org.datacleaner.api.*;
import org.datacleaner.components.categories.DateAndTimeCategory;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Named("Extract date part")
@Description("Extract the parts of a date (year, month, day etc.)")
@Categorized(DateAndTimeCategory.class)
@WSStatelessComponent
public class DatePartTransformer implements Transformer {

	public static enum WeekDay {
		MONDAY(Calendar.MONDAY), TUESDAY(Calendar.TUESDAY), WEDNESDAY(Calendar.WEDNESDAY), THURSDAY(Calendar.THURSDAY), FRIDAY(
				Calendar.FRIDAY), SATURDAY(Calendar.SATURDAY), SUNDAY(Calendar.SUNDAY);

		private final int _calendarInt;

		private WeekDay(int calendarInt) {
			_calendarInt = calendarInt;
		}

		public int getCalendarInt() {
			return _calendarInt;
		}

		public static WeekDay get(int firstDayOfWeek) {
			for (WeekDay weekDay : values()) {
				if (firstDayOfWeek == weekDay.getCalendarInt()) {
					return weekDay;
				}
			}
			return null;
		}

		public WeekDay next() {
			if (this == SUNDAY) {
				return MONDAY;
			}
			return values()[ordinal() + 1];
		}
	}

	@Configured(order = 1)
	InputColumn<Date> column;

	@Configured(order = 2)
	boolean year = true;

	@Configured(order = 3)
	boolean month = true;

	@Configured(order = 4)
	boolean dayOfMonth = true;

	@Configured(order = 5)
	boolean hour = false;

	@Configured(order = 6)
	boolean minute = false;

	@Configured(order = 7)
	boolean second = false;

	@Configured(order = 8)
	boolean dayOfWeek = false;

	@Configured(order = 9)
	boolean weekNumber = false;

	@Configured(order = 10)
	int minimalDaysInFirstWeek = Calendar.getInstance().getMinimalDaysInFirstWeek();

	@Configured(order = 11)
	WeekDay firstDayOfWeek = WeekDay.get(Calendar.getInstance().getFirstDayOfWeek());

	private ArrayList<WeekDay> _indexedWeekDays;

	@Initialize
	public void init() {
		// build indexed week days for reuse in transformation
		_indexedWeekDays = new ArrayList<WeekDay>(7);
		WeekDay nextWeekDay = firstDayOfWeek;
		for (int i = 0; i < 7; i++) {
			_indexedWeekDays.add(nextWeekDay);
			nextWeekDay = nextWeekDay.next();
		}
	}

	@Override
	public OutputColumns getOutputColumns() {
		final List<String> columnNames = new ArrayList<String>();
		final String columnName = column.getName();

		if (year) {
			columnNames.add(columnName + " (year)");
		}
		if (month) {
			columnNames.add(columnName + " (month)");
		}
		if (dayOfMonth) {
			columnNames.add(columnName + " (day of month)");
		}
		if (hour) {
			columnNames.add(columnName + " (hour)");
		}
		if (minute) {
			columnNames.add(columnName + " (minute)");
		}
		if (second) {
			columnNames.add(columnName + " (second)");
		}
		if (dayOfWeek) {
			columnNames.add(columnName + " (day of week)");
		}
		if (weekNumber) {
			columnNames.add(columnName + " (week number)");
		}

		if (columnNames.isEmpty()) {
			columnNames.add(columnName + " (year)");
		}

		return new OutputColumns(Number.class, columnNames.toArray(new String[columnNames.size()]));
	}

	@Override
	public Number[] transform(InputRow inputRow) {
		final Date value = inputRow.getValue(column);
		return transform(value);
	}

	public Number[] transform(Date date) {
		final Calendar cal;
		if (date == null) {
			cal = null;
		} else {
			cal = Calendar.getInstance();
			cal.setFirstDayOfWeek(firstDayOfWeek.getCalendarInt());
			cal.setMinimalDaysInFirstWeek(minimalDaysInFirstWeek);
			cal.setTime(date);
		}

		final List<Number> result = new ArrayList<Number>();

		if (year) {
			result.add(getYear(cal));
		}
		if (month) {
			result.add(getMonth(cal));
		}
		if (dayOfMonth) {
			result.add(getDayOfMonth(cal));
		}
		if (hour) {
			result.add(getHour(cal));
		}
		if (minute) {
			result.add(getMinute(cal));
		}
		if (second) {
			result.add(getSecond(cal));
		}
		if (dayOfWeek) {
			result.add(getDayOfWeek(cal));
		}
		if (weekNumber) {
			result.add(getWeekNumber(cal));
		}

		if (result.isEmpty()) {
			result.add(getYear(cal));
		}
		return result.toArray(new Number[result.size()]);
	}

	private Number getWeekNumber(Calendar cal) {
		if (cal == null) {
			return null;
		}
		return cal.get(Calendar.WEEK_OF_YEAR);
	}

	private Number getDayOfWeek(Calendar cal) {
		if (cal == null) {
			return null;
		}

		WeekDay weekDay = WeekDay.get(cal.get(Calendar.DAY_OF_WEEK));

		return _indexedWeekDays.indexOf(weekDay) + 1;
	}

	private Number getSecond(Calendar cal) {
		if (cal == null) {
			return null;
		}
		return cal.get(Calendar.SECOND);
	}

	private Number getMinute(Calendar cal) {
		if (cal == null) {
			return null;
		}
		return cal.get(Calendar.MINUTE);
	}

	private Number getHour(Calendar cal) {
		if (cal == null) {
			return null;
		}
		return cal.get(Calendar.HOUR_OF_DAY);
	}

	private Number getDayOfMonth(Calendar cal) {
		if (cal == null) {
			return null;
		}
		return cal.get(Calendar.DAY_OF_MONTH);
	}

	private Number getMonth(Calendar cal) {
		if (cal == null) {
			return null;
		}
		// add 1 to the month, to make it 1-based (January = 1)
		return cal.get(Calendar.MONTH) + 1;
	}

	private Number getYear(Calendar cal) {
		if (cal == null) {
			return null;
		}
		return cal.get(Calendar.YEAR);
	}
}
