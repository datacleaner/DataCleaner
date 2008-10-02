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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class keeps track of all validation rules and their descriptors. As such
 * the ValidatorManager serves a central entry point to the validator
 * framework.
 */
public final class ValidatorManager {

	private static List<IValidationRuleDescriptor> _validationRuleDescriptors = new ArrayList<IValidationRuleDescriptor>();
	private static Log _log = LogFactory.getLog(ValidatorManager.class);

	/**
	 * Prevent instantiation
	 */
	private ValidatorManager() {
	}

	public static IValidationRuleDescriptor[] getValidationRuleDescriptors() {
		return _validationRuleDescriptors
				.toArray(new IValidationRuleDescriptor[_validationRuleDescriptors
						.size()]);
	}

	public static IValidationRuleDescriptor getValidationRuleDescriptorByValidationRuleClass(
			Class<? extends IValidationRule> validationRuleClass) {
		if (validationRuleClass != null) {
			for (IValidationRuleDescriptor descriptor : _validationRuleDescriptors) {
				if (validationRuleClass == descriptor.getValidationRuleClass()) {
					return descriptor;
				}
			}
		}
		return null;
	}

	public static IValidationRuleDescriptor getValidationRuleDescriptorByValidationRuleClassName(
			String validationRuleClassName) {
		if (validationRuleClassName != null) {
			for (IValidationRuleDescriptor descriptor : _validationRuleDescriptors) {
				if (validationRuleClassName.equals(descriptor
						.getValidationRuleClass().getName())) {
					return descriptor;
				}
			}
		}
		return null;
	}

	public static void setValidationRuleDescriptors(
			List<IValidationRuleDescriptor> validationRuleDescriptors) {
		if (_log.isInfoEnabled()) {
			_log.info("Setting profile descriptors: "
					+ ArrayUtils.toString(validationRuleDescriptors));
		}
		_validationRuleDescriptors = validationRuleDescriptors;
	}

	public static void addValidationRuleDescriptor(
			IValidationRuleDescriptor descriptor) {
		if (_log.isInfoEnabled()) {
			_log.info("Adding profile descriptor: " + descriptor);
		}
		_validationRuleDescriptors.add(descriptor);
	}
}