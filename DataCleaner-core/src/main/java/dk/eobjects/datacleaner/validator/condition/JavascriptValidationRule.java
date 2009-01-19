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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import dk.eobjects.datacleaner.validator.AbstractValidationRule;
import dk.eobjects.datacleaner.validator.IValidationRuleResult;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;

public class JavascriptValidationRule extends AbstractValidationRule {

	public static final String PROPERTY_JAVASCRIPT_EXPRESSION = "Javascript expression";
	private Set<Column> _evaluatedColumns = new HashSet<Column>();
	private Script _script;
	private ScriptableObject _standardObjectsScope;
	private Context _context;

	@Override
	public void initialize(Column... columns) {
		super.initialize(columns);
		String expression = _properties.get(PROPERTY_JAVASCRIPT_EXPRESSION);
		if (expression == null) {
			throw new IllegalStateException(
					"No javascript expression provided!");
		}
		_context = ContextFactory.getGlobal().enterContext();
		_script = _context.compileString(expression,
				"JavascriptValidationRule", 1, null);
		_standardObjectsScope = _context.initStandardObjects();
		Context.exit();
	}

	@Override
	protected boolean isValid(Row row) throws Exception {
		ContextFactory.getGlobal().enterContext(_context);
		try {

			Map<String, Object> values = new ScriptAccessColumnMap(_columns,
					row, _evaluatedColumns);

			Scriptable scope = _context.newObject(_standardObjectsScope);
			scope.setPrototype(_standardObjectsScope);
			scope.setParentScope(null);

			Object wrappedValues = Context.javaToJS(values, scope);
			ScriptableObject.putProperty(scope, "values", wrappedValues);
			Object result = _script.exec(_context, scope);

			if (result instanceof Boolean) {
				return (Boolean) result;
			}
			throw new IllegalStateException(
					"Javascript expression did not return a boolean");
		} finally {
			Context.exit();
		}
	}

	@Override
	public IValidationRuleResult getResult() {
		setEvaluatedColumns(_evaluatedColumns
				.toArray(new Column[_evaluatedColumns.size()]));
		return super.getResult();
	}
}