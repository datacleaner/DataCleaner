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
package org.datacleaner.beans.filter;

import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Filter;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.Validate;
import org.datacleaner.beans.categories.FilterCategory;

@Named("String value range")
@Description("A filter that filters out values outside a specified value range")
@Categorized(FilterCategory.class)
public class StringValueRangeFilter implements Filter<RangeFilterCategory> {

	@Configured(order = 1)
	InputColumn<String> column;

	@Configured(order = 2)
	@Description("The lowest valid string value, eg. AAA")
	String lowestValue;

	@Configured(order = 3)
	@Description("The highest valid string value, eg. xxx")
	String highestValue;

	public StringValueRangeFilter() {
	}

	public StringValueRangeFilter(String lowestValue, String highestValue) {
		this.lowestValue = lowestValue;
		this.highestValue = highestValue;
	}

	@Validate
	public void validate() {
		if (lowestValue.compareTo(highestValue) > 0) {
			throw new IllegalStateException("Lowest value is greater than the highest value");
		}
	}

	@Override
	public RangeFilterCategory categorize(InputRow inputRow) {
		String value = inputRow.getValue(column);
		return categorize(value);
	}

	protected RangeFilterCategory categorize(String value) {
		if (value == null) {
			return RangeFilterCategory.LOWER;
		}
		if (value.compareTo(lowestValue) < 0) {
			return RangeFilterCategory.LOWER;
		}
		if (value.compareTo(highestValue) > 0) {
			return RangeFilterCategory.HIGHER;
		}
		return RangeFilterCategory.VALID;
	}
}
