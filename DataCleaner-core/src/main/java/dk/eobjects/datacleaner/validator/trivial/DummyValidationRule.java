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

import java.util.HashMap;
import java.util.Map;

import dk.eobjects.datacleaner.validator.BasicValidationRuleDescriptor;
import dk.eobjects.datacleaner.validator.IValidationRule;
import dk.eobjects.datacleaner.validator.IValidationRuleDescriptor;
import dk.eobjects.datacleaner.validator.IValidationRuleResult;
import dk.eobjects.datacleaner.validator.SimpleValidationRuleResult;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;

/**
 * This validation rule doesn't actually do anything except it can be used to
 * configure the <code>ValidationRuleRunner</code> to query for more columns
 * than are actually needed. This is usefull if you want to view the contents of
 * other columns in the error rows that a validation rule result produces.
 */
public final class DummyValidationRule implements IValidationRule {

	public static final IValidationRuleDescriptor DESCRIPTOR = new BasicValidationRuleDescriptor(
			"Dummy validation rule", DummyValidationRule.class);

	public IValidationRuleResult getResult() {
		return new SimpleValidationRuleResult(new Column[0], DESCRIPTOR,
				new HashMap<String, String>());
	}

	public void initialize(Column... columns) {
	}

	public void process(Row row, long distinctRowCount) {
	}

	public void setProperties(Map<String, String> properties) {
	}
}