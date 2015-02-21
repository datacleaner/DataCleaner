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

import java.util.Date;

import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.DateAndTimeCategory;
import org.joda.time.DateTime;
import org.joda.time.Years;

/**
 * A transformer for turning a date into age (both in years and in days).
 * 
 * The transformer and it's intended usage is depicted in this graph:
 * 
 * <img src="doc-files/DateToAgeTransformer.jpg" alt=""/>
 * 
 * <p>
 * The transformer has been more thouroughly explained at kasper's source, see:
 * <a href=
 * "http://kasper.eobjects.org/2010/09/developing-value-transformer-using.html"
 * >Developing a value transformer</a>.
 * </p>
 * 
 * 
 */
@Named("Date to age")
@Description("Turn a Date-column into columns of age (both in years and in days).")
@Categorized(DateAndTimeCategory.class)
public class DateToAgeTransformer implements Transformer {

	@Configured("Date column")
	InputColumn<Date> dateColumn;

	private Date today = new Date();

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns(Integer.class, "Age in days", "Age in years");
	}

	@Override
	public Integer[] transform(InputRow inputRow) {
		Integer[] result = new Integer[2];
		Date date = inputRow.getValue(dateColumn);

		if (date != null) {
			long diffMillis = today.getTime() - date.getTime();
			int diffDays = (int) (diffMillis / (1000 * 60 * 60 * 24));

			result[0] = diffDays;

			// use Joda time to easily calculate the diff in years
			int diffYears = Years.yearsBetween(new DateTime(date), new DateTime(today)).getYears();
			result[1] = diffYears;
		}

		return result;
	}

	// injection for testing purposes only
	public void setToday(Date today) {
		this.today = today;
	}

	// injection for testing purposes only
	public void setDateColumn(InputColumn<Date> dateColumn) {
		this.dateColumn = dateColumn;
	}
}
