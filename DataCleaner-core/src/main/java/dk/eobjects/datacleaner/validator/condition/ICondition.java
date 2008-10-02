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

import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;

/**
 * Interface which represents a condition. Conditions can either be used as part
 * of a ConditionalValidationRule to decide whether or not to dispatch the
 * validation rule request or as the "content" of a SimpleValidationRule.
 */
public interface ICondition {

	public boolean evaluate(Row row, Column... columns);
}