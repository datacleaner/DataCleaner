/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.validator.trivial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.validator.AbstractValidationRule;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;
import dk.eobjects.metamodel.util.BooleanComparator;
import dk.eobjects.metamodel.util.NumberComparator;
import dk.eobjects.metamodel.util.ObjectComparator;
import dk.eobjects.metamodel.util.TimeComparator;
import dk.eobjects.metamodel.util.ToStringComparator;

/**
 * Validation rule for checking that values are within a certain range. This
 * rule can also be used as a simple "greater than" or "lower than" rule, if
 * either highest value or lowest value isn't provided.
 */
@SuppressWarnings("unchecked")
public class ValueRangeValidationRule extends AbstractValidationRule {

	public static final String PROPERTY_HIGHEST_VALUE = "Highest value";
	public static final String PROPERTY_LOWEST_VALUE = "Lowest value";

	private static final Log _log = LogFactory
			.getLog(ValueRangeValidationRule.class);
	private Map<Column, Comparable> _highestValues = new HashMap<Column, Comparable>();
	private Map<Column, Comparable> _lowestValues = new HashMap<Column, Comparable>();

	@Override
	public void initialize(Column... columns) {
		super.initialize(columns);
		ArrayList<Column> result = new ArrayList<Column>();
		String highestValue = _properties.get(PROPERTY_HIGHEST_VALUE);
		String lowestValue = _properties.get(PROPERTY_LOWEST_VALUE);

		if (_log.isDebugEnabled()) {
			_log.debug("Highest value: " + highestValue);
			_log.debug("Lowest value: " + lowestValue);
		}

		for (int i = 0; i < columns.length; i++) {
			Column column = columns[i];

			ColumnType type = column.getType();
			try {
				if (highestValue != null) {
					_highestValues.put(column,
							getComparable(type, highestValue));
				}

				if (lowestValue != null) {
					_lowestValues.put(column, getComparable(type, lowestValue));
				}
				result.add(column);
			} catch (IllegalArgumentException e) {
				_log
						.error("Disregarding column because type was incompatible with comparable: "
								+ column);
				_log.info(e);
			}
		}
		_columns = result.toArray(new Column[result.size()]);
	}

	private Comparable getComparable(ColumnType type, String value)
			throws IllegalArgumentException {
		try {
			if (type != null) {
				if (type.isLiteral()) {
					return ToStringComparator.getComparable(value);
				}
				if (type.isNumber()) {
					return NumberComparator.getComparable(value);
				}
				if (type.isTimeBased()) {
					return TimeComparator.getComparable(value);
				}
				if (type.isBoolean()) {
					return BooleanComparator.getComparable(value);
				}
			}
			return ObjectComparator.getComparable(value);
		} catch (Exception e) {
			if (e instanceof IllegalArgumentException) {
				throw (IllegalArgumentException) e;
			}
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	protected boolean isValid(Row row) {
		if (_properties.get(PROPERTY_HIGHEST_VALUE) == null
				&& _properties.get(PROPERTY_LOWEST_VALUE) == null) {
			throw new IllegalStateException(
					"Neither lowest value or highest value has been set!");
		}
		boolean result = true;
		for (int i = 0; i < _columns.length && result; i++) {
			Column column = _columns[i];
			Object value = row.getValue(column);
			result = checkValue(column, value);
		}
		return result;
	}

	private boolean checkValue(Column column, Object value) {
		boolean result = true;
		if (value == null) {
			return false;
		}

		Comparable lowestValue = _lowestValues.get(column);
		Comparable highestValue = _highestValues.get(column);

		if (lowestValue != null) {
			if (lowestValue.compareTo(value) > 0) {
				result = false;
			}
		}
		if (highestValue != null) {
			if (highestValue.compareTo(value) < 0) {
				result = false;
			}
		}
		return result;
	}

	public Comparable getHighestValue(Column column) {
		return _highestValues.get(column);
	}

	public Comparable getLowestValue(Column column) {
		return _lowestValues.get(column);
	}

	public Column[] getColumns() {
		return _columns;
	}
}