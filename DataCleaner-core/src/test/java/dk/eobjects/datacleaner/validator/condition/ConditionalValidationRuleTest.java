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

import java.util.HashMap;

import org.easymock.EasyMock;

import dk.eobjects.datacleaner.testware.DataCleanerTestCase;
import dk.eobjects.datacleaner.validator.IValidationRule;
import dk.eobjects.datacleaner.validator.IValidationRuleResult;
import dk.eobjects.datacleaner.validator.SimpleValidationRuleResult;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;

public class ConditionalValidationRuleTest extends DataCleanerTestCase {

	public void testSimple() throws Exception {
		ICondition conditionMock = createMock(ICondition.class);
		IValidationRule vrMock = createMock(IValidationRule.class);
		Column col = new Column("foobar", ColumnType.INTEGER);
		Column[] columns = new Column[] { col };
		SelectItem[] selectItems = new SelectItem[] { new SelectItem(col) };
		Row row1 = new Row(selectItems, new Object[] { 2 });
		Row row2 = new Row(selectItems, new Object[] { 3 });

		ConditionalValidationRule conditionalValidationRule = new ConditionalValidationRule(
				conditionMock, vrMock);

		HashMap<String, String> properties = new HashMap<String, String>();

		vrMock.setProperties(properties);
		vrMock.initialize(columns);

		EasyMock.expect(conditionMock.evaluate(row1, columns)).andReturn(false);
		EasyMock.expect(conditionMock.evaluate(row2, columns)).andReturn(true);

		vrMock.process(row2, 1);

		IValidationRuleResult mockResult = new SimpleValidationRuleResult(
				columns, null, properties);
		EasyMock.expect(vrMock.getResult()).andReturn(mockResult);

		replayMocks();
		conditionalValidationRule.setProperties(properties);
		conditionalValidationRule.initialize(columns);

		conditionalValidationRule.process(row1, 1);
		conditionalValidationRule.process(row2, 1);
		IValidationRuleResult result = conditionalValidationRule.getResult();

		verifyMocks();

		assertSame(mockResult, result);
	}
}