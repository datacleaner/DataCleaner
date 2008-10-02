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

import java.util.List;
import java.util.Map;

import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;

/**
 * Represents the result of the execution of a validation rule
 */
public interface IValidationRuleResult {

	/**
	 * @return the descriptor of the validation rule that has been executed
	 */
	public IValidationRuleDescriptor getDescriptor();

	/**
	 * @return the properties of the validation rule
	 */
	public Map<String, String> getProperties();

	/**
	 * @return an overall evaluation of whether the validation rule was
	 *         successfull. A validation rule can be invalid if either there are
	 *         unvalidated rows or if a general errors have occurred.
	 */
	public boolean isValidated();

	/**
	 * If the validation rule was unsuccessfull, this method returns the rows
	 * that could not be validated and their distinct row counts.
	 */
	public List<Row> getUnvalidatedRows();

	/**
	 * @return the columns that was used for validation
	 */
	public Column[] getEvaluatedColumns();

	/**
	 * @return the error (if any) that occurred during execution
	 */
	public Exception getError();
}