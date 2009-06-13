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
package dk.eobjects.datacleaner.gui.setup;

import java.util.Collection;

import org.apache.commons.lang.ArrayUtils;

import dk.eobjects.datacleaner.gui.model.NamedConnection;
import dk.eobjects.datacleaner.profiler.IProfileDescriptor;
import dk.eobjects.datacleaner.testware.DataCleanerTestCase;
import dk.eobjects.datacleaner.util.ReflectionHelper;
import dk.eobjects.datacleaner.validator.IValidationRuleDescriptor;

public class GuiConfigurationTest extends DataCleanerTestCase {

	public void testInitialize() throws Exception {
		GuiConfiguration.initialize(getTestResourceAsFile("datacleaner-config-test.xml"));
		Collection<IProfileDescriptor> profileDescriptors = GuiConfiguration.getBeansOfClass(IProfileDescriptor.class);
		String[] expectations = new String[] { "Standard measures", "String analysis", "Pattern finder",
				"Value distribution", "Number analysis", "Time analysis", "Dictionary matcher", "Date mask matcher",
				"Regex matcher" };
		assertEquals(expectations.length, profileDescriptors.size());
		Object[] displayNames = ReflectionHelper.getProperties(profileDescriptors, "displayName");
		for (Object displayName : displayNames) {
			assertTrue(ArrayUtils.indexOf(expectations, displayName) != -1);
		}

		Collection<IValidationRuleDescriptor> validationRuleDescriptors = GuiConfiguration
				.getBeansOfClass(IValidationRuleDescriptor.class);
		expectations = new String[] { "Javascript evaluation", "Dictionary lookup", "Value range evaluation",
				"Regex validation", "Not-null check" };
		assertEquals(expectations.length, validationRuleDescriptors.size());
		displayNames = ReflectionHelper.getProperties(validationRuleDescriptors, "displayName");
		for (Object displayName : displayNames) {
			assertTrue(ArrayUtils.indexOf(expectations, displayName) != -1);
		}

		Collection<NamedConnection> namedConnections = GuiConfiguration.getNamedConnections();
		expectations = new String[] { "- select -", "Some Derby database" };
		assertEquals(expectations.length, namedConnections.size());
		displayNames = ReflectionHelper.getProperties(namedConnections, "name");
		for (Object displayName : displayNames) {
			assertTrue(ArrayUtils.indexOf(expectations, displayName) != -1);
		}
	}
}