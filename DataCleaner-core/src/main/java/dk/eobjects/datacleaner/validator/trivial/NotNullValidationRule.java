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

import dk.eobjects.datacleaner.validator.AbstractValidationRule;
import dk.eobjects.metamodel.data.Row;

/**
 * A validation rule that asserts that content of selected columns may not be
 * null
 */
public class NotNullValidationRule extends AbstractValidationRule {

	@Override
	protected boolean isValid(Row row) {
		for (int i = 0; i < _columns.length; i++) {
			Object value = row.getValue(_columns[i]);
			if (value == null) {
				return false;
			}
		}
		return true;
	}

}