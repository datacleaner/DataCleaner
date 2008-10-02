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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.profiler.BasicProfileDescriptor;
import dk.eobjects.datacleaner.util.ReflectionHelper;
import dk.eobjects.metamodel.schema.ColumnType;

public class BasicValidationRuleDescriptor implements IValidationRuleDescriptor {

	private static final long serialVersionUID = 599488159496699718L;
	private static final String PROPERTY_CONSTANT_PREFIX = "PROPERTY_";
	private static final Log _log = LogFactory.getLog(BasicProfileDescriptor.class);
	private String _displayName;
	private Class<? extends IValidationRule> _validationRuleClass;
	private boolean _datesRequired = false;
	private boolean _literalsRequired = false;
	private boolean _numbersRequired = false;
	private String[] _propertyNames;
	private String _iconPath;

	public void setDatesRequired(boolean datesRequired) {
		_datesRequired = datesRequired;
	}

	public void setLiteralsRequired(boolean literalsRequired) {
		_literalsRequired = literalsRequired;
	}

	public void setNumbersRequired(boolean numbersRequired) {
		_numbersRequired = numbersRequired;
	}

	public BasicValidationRuleDescriptor(String displayName,
			Class<? extends IValidationRule> validationRuleClass) {
		_displayName = displayName;
		_validationRuleClass = validationRuleClass;
	}

	public BasicValidationRuleDescriptor() {
	}

	public boolean isSupported(ColumnType type) {
		if (_datesRequired && !type.isTimeBased()) {
			return false;
		}
		if (_literalsRequired && !type.isLiteral()) {
			return false;
		}
		if (_numbersRequired && !type.isNumber()) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "BasicProfileDescriptor[displayName=" + _displayName
				+ ",validationRuleClass=" + _validationRuleClass + "]";
	}

	public String getDisplayName() {
		return _displayName;
	}

	public void setDisplayName(String displayName) {
		_displayName = displayName;
	}

	public Class<? extends IValidationRule> getValidationRuleClass() {
		return _validationRuleClass;
	}

	public void setValidationRuleClass(
			Class<? extends IValidationRule> validationRuleClass) {
		_validationRuleClass = validationRuleClass;
	}

	public String[] getPropertyNames() {
		ArrayList<String> result = new ArrayList<String>();
		if (_propertyNames != null) {
			for (int i = 0; i < _propertyNames.length; i++) {
				result.add(_propertyNames[i]);
			}
		}

		if (_validationRuleClass != null) {
			addConstantValues(result, _validationRuleClass);
		}

		return result.toArray(new String[result.size()]);
	}

	private void addConstantValues(List<String> result,
			Class<? extends Object> clazz) {
		Field[] constants = ReflectionHelper.getConstants(clazz);
		for (int i = 0; i < constants.length; i++) {
			Field constant = constants[i];
			if (constant.getName().startsWith(PROPERTY_CONSTANT_PREFIX)) {
				try {
					result.add(constant.get(null).toString());
				} catch (IllegalArgumentException e) {
					_log.error(e);
				} catch (IllegalAccessException e) {
					_log.error(e);
				}
			}
		}
	}

	public void setPropertyNames(String[] propertyNames) {
		_propertyNames = propertyNames;
	}

	public String getIconPath() {
		return _iconPath;
	}

	public void setIconPath(String iconPath) {
		_iconPath = iconPath;
	}
}