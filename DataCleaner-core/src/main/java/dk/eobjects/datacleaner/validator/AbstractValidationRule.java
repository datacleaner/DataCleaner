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
package dk.eobjects.datacleaner.validator;

import java.util.Map;

import dk.eobjects.datacleaner.validator.dictionary.DictionaryValidationRule;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;

/**
 * Abstract implementation of the IValidationRule interface. Using this
 * implementation substantially simplifies the creation of a validation rule
 * because you only have to fill in the isValid method, but also has some
 * limitations: You can't buffer validation requests, which is why
 * DictionaryValidationRule doesn't extend this class.
 * 
 * @see DictionaryValidationRule
 */
public abstract class AbstractValidationRule implements IValidationRule {

	protected SimpleValidationRuleResult _result;
	protected Column[] _columns;
	protected Map<String, String> _properties;

	public IValidationRuleResult getResult() {
		return _result;
	}

	public void initialize(Column... columns) {
		_columns = columns;
		_result = new SimpleValidationRuleResult(columns, ValidatorManager
				.getValidationRuleDescriptorByValidationRuleClass(this
						.getClass()), _properties);
	}

	public void process(Row row, long distinctRowCount) {
		try {
			if (_result.getError() == null) {
				if (!isValid(row)) {
					_result.addErrorRow(row);
				}
			}
		} catch (Exception e) {
			_result.setError(e);
		}
	}

	public void setProperties(Map<String, String> properties) {
		_properties = properties;
	}

	protected void setEvaluatedColumns(Column... columns) {
		_result.setEvaluatedColumns(columns);
	}

	protected abstract boolean isValid(Row row) throws Exception;
}
