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

import junit.framework.TestCase;
import dk.eobjects.datacleaner.validator.trivial.ValueRangeValidationRule;
import dk.eobjects.metamodel.schema.ColumnType;

public class BasicValidationRuleDescriptorTest extends TestCase {
	public void testIsSupported() throws Exception {
		BasicValidationRuleDescriptor descriptor = new BasicValidationRuleDescriptor();
		descriptor.setDatesRequired(false);

		assertTrue(descriptor.isSupported(ColumnType.VARCHAR));
		assertTrue(descriptor.isSupported(ColumnType.TIMESTAMP));

		descriptor.setLiteralsRequired(true);

		assertTrue(descriptor.isSupported(ColumnType.VARCHAR));
		assertFalse(descriptor.isSupported(ColumnType.BIGINT));
	}

	public void testGetPropertyNames() throws Exception {
		BasicValidationRuleDescriptor descriptor = new BasicValidationRuleDescriptor();
		descriptor.setPropertyNames(new String[] { "foobar" });

		String[] propertyNames = descriptor.getPropertyNames();
		assertEquals(1, propertyNames.length);
		assertEquals("foobar", propertyNames[0]);

		descriptor.setValidationRuleClass(ValueRangeValidationRule.class);
		propertyNames = descriptor.getPropertyNames();
		assertEquals(4, propertyNames.length);
		assertEquals("foobar", propertyNames[0]);
		assertEquals("Highest value", propertyNames[1]);
		assertEquals("Lowest value", propertyNames[2]);
		assertEquals("Validation rule name", propertyNames[3]);
	}
}