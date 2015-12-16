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

import java.util.Arrays;

import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.DateAndTimeCategory;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@Named("Date mask matcher")
@Description("Matches String values against a set of date masks, producing a corresponding set of output columns, specifying whether or not the strings could be interpreted as dates given those date masks")
@Categorized({ DateAndTimeCategory.class })
public class DateMaskMatcherTransformer implements Transformer {

	public static final String[] DEFAULT_DATE_MASKS = new String[] { "yyyy-MM-dd", "yyyy/MM/dd", "dd.MM.yyyy", "dd/MM/yyyy",
			"MM/dd/yy", "d MMM yyyy HH:mm:ss", "yyyy-MM-dd HH:mm:ss.S" };

	@Configured
	InputColumn<String> _column;

	@Configured
	String[] _dateMasks = DEFAULT_DATE_MASKS;

	@Configured
	MatchOutputType _outputType = MatchOutputType.TRUE_FALSE;

	private DateTimeFormatter[] _dateTimeFormatters;

	public DateMaskMatcherTransformer(InputColumn<String> column) {
		_column = column;
	}

	public DateMaskMatcherTransformer() {
	}

	@Initialize
	public void init() {
		_dateTimeFormatters = new DateTimeFormatter[_dateMasks.length];
		for (int i = 0; i < _dateTimeFormatters.length; i++) {
			try {
				_dateTimeFormatters[i] = DateTimeFormat.forPattern(_dateMasks[i]);
			} catch (Exception e) {
				// not a valid pattern!
				_dateTimeFormatters[i] = null;
			}
		}
	}

	@Override
	public OutputColumns getOutputColumns() {
		String columnName = _column.getName();
		String[] names = new String[_dateMasks.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = columnName + " '" + _dateMasks[i] + "'";
		}
		Class<?>[] types = new Class[_dateMasks.length];
		for (int i = 0; i < types.length; i++) {
			types[i] = _outputType.getOutputClass();
		}
		return new OutputColumns(names, types);
	}

	@Override
	public Object[] transform(InputRow inputRow) {
		Object[] result = new Object[_dateMasks.length];

		if (_outputType == MatchOutputType.TRUE_FALSE) {
			Arrays.fill(result, false);
		}

		String value = inputRow.getValue(_column);
		if (value != null) {
			for (int i = 0; i < _dateTimeFormatters.length; i++) {
				DateTimeFormatter dateTimeFormatter = _dateTimeFormatters[i];
				if (dateTimeFormatter != null) {
					boolean match = false;
					try {
						// this will throw an exception if the value is not
						// complying to the pattern
						dateTimeFormatter.parseDateTime(value);
						match = true;
					} catch (Exception e) {
						// ignore, it doesn't match
					}

					if (_outputType == MatchOutputType.TRUE_FALSE) {
						result[i] = match;
					} else if (_outputType == MatchOutputType.INPUT_OR_NULL) {
						if (match) {
							result[i] = value;
						} else {
							result[i] = null;
						}
					}
				}
			}
		}
		return result;
	}

	public void setDateMasks(String[] dateMasks) {
		_dateMasks = dateMasks;
	}

	public String[] getDateMasks() {
		return _dateMasks;
	}
}
