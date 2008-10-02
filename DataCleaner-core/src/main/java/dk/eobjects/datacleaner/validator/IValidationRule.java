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

import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;

/**
 * A validation rule is a rule that can applied to data to verify that an
 * assertion is true or false.
 */
public interface IValidationRule {

	/**
	 * General property for all validation rules: A user-written name of this
	 * rule.
	 */
	public static final String PROPERTY_NAME = "Validation rule name";

	/**
	 * Sets configuration properties for the validation rule
	 * 
	 * @param properties
	 */
	public void setProperties(Map<String, String> properties);

	/**
	 * Initializes the validation rule
	 * 
	 * @param columns
	 *            the columns to be validated
	 */
	public void initialize(Column... columns);

	/**
	 * Processes a row of data
	 * 
	 * @param row
	 *            the row to be processed
	 * @param distinctRowCount
	 *            the distinct count of the values in the row
	 */
	public void process(Row row, long distinctRowCount);

	/**
	 * @return the result of the validation rule, when there are no more rows to
	 *         process
	 */
	public IValidationRuleResult getResult();
}