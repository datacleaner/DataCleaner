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
package dk.eobjects.datacleaner.profiler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.util.ReflectionHelper;
import dk.eobjects.metamodel.schema.ColumnType;

/**
 * A basic (default) profile descriptor class. This class is designed to support
 * easy dependency injection, for example as spring beans. The setter methods
 * provide easy configuration of the profile descriptor.
 * 
 * Any constant in the profiles specified in the profile beginning with the name
 * "PROPERTY_" is automatically added to the descriptors list of property names
 */
public class BasicProfileDescriptor implements IProfileDescriptor {

	private static final long serialVersionUID = 7463620056978325219L;
	private static final String PROPERTY_CONSTANT_PREFIX = "PROPERTY_";
	private static final Log _log = LogFactory.getLog(BasicProfileDescriptor.class);
	private String _displayName;
	private Class<? extends IProfile> _profileClass;
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

	public BasicProfileDescriptor(String displayName,
			Class<? extends IProfile> profileClass) {
		_displayName = displayName;
		_profileClass = profileClass;
	}

	public BasicProfileDescriptor() {
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
				+ ",profileClass=" + _profileClass + "]";
	}

	public String getDisplayName() {
		return _displayName;
	}

	public void setDisplayName(String displayName) {
		_displayName = displayName;
	}

	public Class<? extends IProfile> getProfileClass() {
		return _profileClass;
	}

	public void setProfileClass(Class<? extends IProfile> profileClass) {
		_profileClass = profileClass;
	}

	public String[] getPropertyNames() {
		ArrayList<String> result = new ArrayList<String>();
		if (_propertyNames != null) {
			for (int i = 0; i < _propertyNames.length; i++) {
				result.add(_propertyNames[i]);
			}
		}

		if (_profileClass != null) {
			addConstantValues(result, _profileClass);
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