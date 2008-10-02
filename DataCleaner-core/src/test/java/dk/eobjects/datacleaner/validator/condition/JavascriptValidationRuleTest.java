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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ScriptableObject;

import dk.eobjects.datacleaner.testware.DataCleanerTestCase;
import dk.eobjects.datacleaner.validator.IValidationRuleResult;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;

public class JavascriptValidationRuleTest extends DataCleanerTestCase {

	public void testEvaluate() throws Exception {
		JavascriptValidationRule vr = new JavascriptValidationRule();
		Column col1 = new Column("name", ColumnType.VARCHAR);
		Column col3 = new Column("gender", ColumnType.VARCHAR);
		Column col2 = new Column("nevermind-me", ColumnType.INTEGER);
		Column[] columns = new Column[] { col1, col2, col3 };

		Map<String, String> properties = new HashMap<String, String>();
		String expression = "if (values.get('gender').toLowerCase() == 'm') {"
				+ "  values.get('name').toLowerCase().substring(0,3) == 'mr.';"
				+ "} else {"
				+ "  values.get('name').toLowerCase().substring(0,4) == 'mrs.';"
				+ "}";
		properties.put(JavascriptValidationRule.PROPERTY_JAVASCRIPT_EXPRESSION,
				expression);
		vr.setProperties(properties);

		vr.initialize(columns);

		SelectItem[] selectItems = new SelectItem[] { new SelectItem(col1),
				new SelectItem(col2), new SelectItem(col3) };
		vr.process(new Row(selectItems, new Object[] { "Mr. Jones", 1, "M" }),
				1);
		vr.process(new Row(selectItems, new Object[] { "Mrs. Jones", 1, "F" }),
				1);
		vr.process(
				new Row(selectItems, new Object[] { "Frank Jones", 1, "M" }),
				10);
		vr
				.process(new Row(selectItems, new Object[] { "Mrs. Foobar", 1,
						"F" }), 1);
		vr
				.process(new Row(selectItems,
						new Object[] { "Foo bar", 1, "F" }), 10);

		IValidationRuleResult result = vr.getResult();
		assertFalse(result.isValidated());

		Column[] evaluatedColumns = result.getEvaluatedColumns();
		assertEqualsArray(new Column[] { col1, col3 }, evaluatedColumns, false);

		String[] expectations = new String[] { "Row[values={Foo bar,1,F}]",
				"Row[values={Frank Jones,1,M}]" };
		List<Row> errorRows = result.getUnvalidatedRows();
		for (Row row : errorRows) {
			boolean testResult = ArrayUtils.indexOf(expectations, row
					.toString()) != -1;
			if (!testResult) {
				System.err.println(row.toString());
			}
			assertTrue(testResult);
		}
	}

	public void testRhinoJavascript() throws Exception {
		Context context = ContextFactory.getGlobal().enterContext();
		ScriptableObject scope = context.initStandardObjects();

		Map<String, Object> values = new HashMap<String, Object>();
		values.put("col1", "foo");
		values.put("col2", "bar");
		values.put("col3", "a value");
		values.put("col4", "w00p");

		Object wrappedValues = Context.javaToJS(values, scope);
		ScriptableObject.putProperty(scope, "values", wrappedValues);

		Object result = context.evaluateString(scope,
				"values.get('col2') == 'bar'", "unittest", 1, null);
		assertEquals(true, result);

		result = context.evaluateString(scope,
				"values.get('col1') == 'foobar'", "unittest", 1, null);
		assertEquals(false, result);

		result = context
				.evaluateString(
						scope,
						"values.get('col1') == 'foobar' || values.get('col2') == 'bar'",
						"unittest", 1, null);
		assertEquals(true, result);
		Context.exit();
	}

	public void testInvalidExpression() throws Exception {
		JavascriptValidationRule vr = new JavascriptValidationRule();
		Column col1 = new Column("name", ColumnType.VARCHAR);
		Column col3 = new Column("gender", ColumnType.VARCHAR);
		Column col2 = new Column("nevermind-me", ColumnType.INTEGER);
		Column[] columns = new Column[] { col1, col2, col3 };

		Map<String, String> properties = new HashMap<String, String>();
		properties.put(JavascriptValidationRule.PROPERTY_JAVASCRIPT_EXPRESSION,
				"foobar");
		vr.setProperties(properties);

		vr.initialize(columns);

		SelectItem[] selectItems = new SelectItem[] { new SelectItem(col1),
				new SelectItem(col2), new SelectItem(col3) };

		vr.process(new Row(selectItems, new Object[] { "Mr. Jones", 1, "M" }),
				1);
		IValidationRuleResult result = vr.getResult();
		assertFalse(result.isValidated());
		Exception error = result.getError();
		assertNotNull(error);
		assertEquals(
				"ReferenceError: \"foobar\" is not defined. (JavascriptValidationRule#1)",
				error.getMessage());
	}

	public void testMissingColumn() throws Exception {
		JavascriptValidationRule vr = new JavascriptValidationRule();
		Column col1 = new Column("name", ColumnType.VARCHAR);
		Column col2 = new Column("nevermind-me", ColumnType.INTEGER);
		Column[] columns = new Column[] { col1, col2 };

		Map<String, String> properties = new HashMap<String, String>();
		properties.put(JavascriptValidationRule.PROPERTY_JAVASCRIPT_EXPRESSION,
				"values.get('foobar') != null");
		vr.setProperties(properties);

		vr.initialize(columns);

		SelectItem[] selectItems = new SelectItem[] { new SelectItem(col1),
				new SelectItem(col2) };
		vr.process(new Row(selectItems, new Object[] { "Mr. Jones", 1 }), 1);
		IValidationRuleResult result = vr.getResult();
		assertFalse(result.isValidated());
		Exception error = result.getError();
		assertNotNull(error);
		assertEquals(
				"Wrapped java.lang.IllegalArgumentException: No such column 'foobar' (JavascriptValidationRule#1)",
				error.getMessage());
	}
}