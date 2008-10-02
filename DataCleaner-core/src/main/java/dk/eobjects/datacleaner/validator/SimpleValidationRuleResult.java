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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;

/**
 * Default implementation of the <code>IValidationRuleResult</code> interface.
 */
public class SimpleValidationRuleResult implements IValidationRuleResult {

	private List<Row> _rows = new ArrayList<Row>();
	private Column[] _evaluatedColumns;
	private IValidationRuleDescriptor _descriptor;
	private Map<String, String> _properties;
	private Exception _error;

	public SimpleValidationRuleResult(Column[] evaluatedColumns,
			IValidationRuleDescriptor descriptor, Map<String, String> properties) {
		setEvaluatedColumns(evaluatedColumns);
		_descriptor = descriptor;
		_properties = properties;
	}

	public List<Row> getUnvalidatedRows() {
		return _rows;
	}

	public boolean isValidated() {
		return (_rows.size() == 0 && _error == null);
	}

	public void addErrorRow(Row row) {
		_rows.add(row);
	}

	public int getUnvalidatedRowCount() {
		return _rows.size();
	}

	@Override
	public String toString() {
		return "SimpleValidationRuleResult[error=" + _error + ",errorRows="
				+ _rows + "]";
	}

	public Column[] getEvaluatedColumns() {
		return _evaluatedColumns;
	}

	public void setEvaluatedColumns(Column[] evaluatedColumns) {
		_evaluatedColumns = evaluatedColumns;
	}

	public IValidationRuleDescriptor getDescriptor() {
		return _descriptor;
	}

	public Map<String, String> getProperties() {
		return _properties;
	}

	public void setError(Exception error) {
		_error = error;
	}

	public Exception getError() {
		return _error;
	}
}