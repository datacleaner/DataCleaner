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

import dk.eobjects.datacleaner.validator.condition.JavascriptValidationRule;
import dk.eobjects.datacleaner.validator.dictionary.DictionaryValidationRule;
import dk.eobjects.datacleaner.validator.trivial.NotNullValidationRule;
import dk.eobjects.datacleaner.validator.trivial.ValueRangeValidationRule;

import junit.framework.TestCase;

public class ValidationRuleManagerTest extends TestCase {

	public static final IValidationRuleDescriptor DESCRIPTOR_NOT_NULL = new BasicValidationRuleDescriptor(
			"Not-null checker", NotNullValidationRule.class);
	public static final IValidationRuleDescriptor DESCRIPTOR_VALUE_RANGE = new BasicValidationRuleDescriptor(
			"Value range validation", ValueRangeValidationRule.class);
	public static final IValidationRuleDescriptor DESCRIPTOR_JAVASCRIPT = new BasicValidationRuleDescriptor(
			"Javascript evaluator", JavascriptValidationRule.class);
	public static final IValidationRuleDescriptor DESCRIPTOR_DICTIONARY = new BasicValidationRuleDescriptor(
			"Dictionary lookup", DictionaryValidationRule.class);

	public void testGetDescriptorByValidationRuleClass() throws Exception {
		initValidationRuleManager();
		IValidationRuleDescriptor validationRuleDescriptor = ValidatorManager
				.getValidationRuleDescriptorByValidationRuleClass(JavascriptValidationRule.class);
		assertSame(DESCRIPTOR_JAVASCRIPT, validationRuleDescriptor);
	}

	public static void initValidationRuleManager() {
		List<IValidationRuleDescriptor> descriptors = new ArrayList<IValidationRuleDescriptor>();
		descriptors.add(DESCRIPTOR_NOT_NULL);
		descriptors.add(DESCRIPTOR_VALUE_RANGE);
		descriptors.add(DESCRIPTOR_JAVASCRIPT);
		descriptors.add(DESCRIPTOR_DICTIONARY);
		ValidatorManager.setValidationRuleDescriptors(descriptors);
	}
}