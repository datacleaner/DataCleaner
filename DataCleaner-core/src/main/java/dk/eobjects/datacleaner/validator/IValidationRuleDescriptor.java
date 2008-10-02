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

import java.io.Serializable;

import dk.eobjects.metamodel.schema.ColumnType;

/**
 * An object which describes a validation rule. The validation rule descriptors
 * are used as a way to provide information about the types of validation rules.
 */
public interface IValidationRuleDescriptor extends Serializable {

	/**
	 * @return a name or label for the validation rule
	 */
	public String getDisplayName();

	/**
	 * @return a path to a image/icon to represent the validation rule
	 */
	public String getIconPath();

	/**
	 * @param columnType
	 *            a column type from the java.sql.Types constants
	 * @return true if the validation rule supports columns with this type
	 */
	public boolean isSupported(ColumnType columnType);

	/**
	 * @return the validation rule class for validation.
	 */
	public Class<? extends IValidationRule> getValidationRuleClass();

	/**
	 * @return the property-names for the validation rule configuration.
	 */
	public String[] getPropertyNames();
}