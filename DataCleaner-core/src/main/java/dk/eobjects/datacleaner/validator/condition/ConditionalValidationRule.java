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
package dk.eobjects.datacleaner.validator.condition;

import java.util.Map;

import dk.eobjects.datacleaner.validator.IValidationRule;
import dk.eobjects.datacleaner.validator.IValidationRuleResult;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;

/**
 * Uses a condition to verify whether or not the validation should be dispatched
 * to another validation rule
 */
public class ConditionalValidationRule implements IValidationRule {

	private IValidationRule _innerValidationRule;
	private ICondition _condition;
	private Column[] _columns;

	public ConditionalValidationRule() {
	}

	public ConditionalValidationRule(ICondition condition,
			IValidationRule validationRule) {
		setCondition(condition);
		setInnerValidationRule(validationRule);
	}

	public IValidationRuleResult getResult() {
		return _innerValidationRule.getResult();
	}

	public void initialize(Column... columns) {
		_columns = columns;
		_innerValidationRule.initialize(columns);
	}

	public void process(Row row, long distinctRowCount) {
		if (_condition.evaluate(row, _columns)) {
			_innerValidationRule.process(row, distinctRowCount);
		}
	}

	public void setProperties(Map<String, String> properties) {
		_innerValidationRule.setProperties(properties);
	}

	public ICondition getCondition() {
		return _condition;
	}

	public void setCondition(ICondition condition) {
		_condition = condition;
	}

	public IValidationRule getInnerValidationRule() {
		return _innerValidationRule;
	}

	public void setInnerValidationRule(IValidationRule innerValidationRule) {
		_innerValidationRule = innerValidationRule;
	}
}