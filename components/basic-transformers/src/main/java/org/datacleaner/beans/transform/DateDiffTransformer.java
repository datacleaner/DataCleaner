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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.DateAndTimeCategory;
import org.apache.metamodel.util.DateUtils;

@Named("Date difference / period length")
@Description("Calculates the length of a period made of two dates")
@Categorized(DateAndTimeCategory.class)
public class DateDiffTransformer implements Transformer {

	@Configured(order = 1)
	InputColumn<Date> fromColumn;

	@Configured(order = 2)
	InputColumn<Date> toColumn;

	@Configured(order = 3)
	boolean days = true;

	@Configured(order = 4)
	boolean hours = false;

	@Configured(order = 5)
	boolean minutes = false;

	@Configured(order = 6)
	boolean seconds = false;

	@Configured(order = 7)
	boolean milliseconds = false;

	@Override
	public OutputColumns getOutputColumns() {
		List<String> columnNames = new ArrayList<String>();
		String from = fromColumn.getName();
		String to = toColumn.getName();
		if (days) {
			columnNames.add("Days from " + from + " to " + to);
		}
		if (hours) {
			columnNames.add("Hours from " + from + " to " + to);
		}
		if (minutes) {
			columnNames.add("Minutes from " + from + " to " + to);
		}
		if (seconds) {
			columnNames.add("Seconds from " + from + " to " + to);
		}
		if (milliseconds || columnNames.isEmpty()) {
			columnNames.add("Milliseconds from " + from + " to " + to);
		}

		return new OutputColumns(Number.class, columnNames.toArray(new String[columnNames.size()]));
	}

	@Override
	public Number[] transform(InputRow inputRow) {
		Date from = inputRow.getValue(fromColumn);
		Date to = inputRow.getValue(toColumn);
		return transform(from, to);
	}

	public Number[] transform(final Date from, final Date to) {
		final long fromTime = from == null ? 0 : from.getTime();
		final long toTime = to == null ? 0 : to.getTime();
		final long diff = toTime - fromTime;

		final List<Number> numbers = new ArrayList<Number>();
		if (days) {
			numbers.add(diff / DateUtils.MILLISECONDS_PER_DAY);
		}
		if (hours) {
			numbers.add(diff / DateUtils.MILLISECONDS_PER_HOUR);
		}
		if (minutes) {
			numbers.add(diff / DateUtils.MILLISECONDS_PER_MINUTE);
		}
		if (seconds) {
			numbers.add(diff / DateUtils.MILLISECONDS_PER_SECOND);
		}
		if (milliseconds || numbers.isEmpty()) {
			numbers.add(diff);
		}

		Number[] result = new Number[numbers.size()];

		if (from == null || to == null) {
			// return an array with null's if either of the inputs are null
			return result;
		}
		return numbers.toArray(result);
	}

	public boolean isHours() {
		return hours;
	}

	public void setHours(boolean hours) {
		this.hours = hours;
	}

	public void setDays(boolean days) {
		this.days = days;
	}

	public void setMinutes(boolean minutes) {
		this.minutes = minutes;
	}

	public void setSeconds(boolean seconds) {
		this.seconds = seconds;
	}

	public void setMilliseconds(boolean milliseconds) {
		this.milliseconds = milliseconds;
	}
}
