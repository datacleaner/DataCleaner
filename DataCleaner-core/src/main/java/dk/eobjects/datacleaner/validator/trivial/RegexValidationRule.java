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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import dk.eobjects.datacleaner.validator.AbstractValidationRule;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;

/**
 * A validation rule that validates values according to a regular expression
 */
public class RegexValidationRule extends AbstractValidationRule {

	public static final String PROPERTY_REGEX = "Regular expression";
	private Pattern _pattern;
	private String _expression;

	@Override
	public void setProperties(Map<String, String> properties) {
		super.setProperties(properties);
		_expression = properties.get(PROPERTY_REGEX);
		if (_expression == null) {
			throw new IllegalStateException("No regular expression provided");
		}
	}

	@Override
	public void initialize(Column... columns) {
		super.initialize(columns);
		try {
			_pattern = Pattern.compile(_expression);
		} catch (PatternSyntaxException e) {
			_result.setError(e);
		}
	}

	@Override
	protected boolean isValid(Row row) throws Exception {
		for (int i = 0; i < _columns.length; i++) {
			Object value = row.getValue(_columns[i]);
			if (value != null) {
				Matcher matcher = _pattern.matcher(value.toString());
				return matcher.matches();
			}
			return false;
		}
		return false;
	}

}