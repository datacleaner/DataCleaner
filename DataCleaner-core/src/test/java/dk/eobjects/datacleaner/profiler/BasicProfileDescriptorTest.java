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

import junit.framework.TestCase;
import dk.eobjects.datacleaner.profiler.trivial.RepeatedValuesProfile;
import dk.eobjects.metamodel.schema.ColumnType;

@SuppressWarnings("deprecation")
public class BasicProfileDescriptorTest extends TestCase {

	public void testIsSupported() throws Exception {
		BasicProfileDescriptor pd = new BasicProfileDescriptor();
		pd.setDatesRequired(false);

		assertTrue(pd.isSupported(ColumnType.VARCHAR));
		assertTrue(pd.isSupported(ColumnType.TIMESTAMP));

		pd.setLiteralsRequired(true);

		assertTrue(pd.isSupported(ColumnType.VARCHAR));
		assertFalse(pd.isSupported(ColumnType.BIGINT));
	}

	public void testGetPropertyNames() throws Exception {
		BasicProfileDescriptor pd = new BasicProfileDescriptor();
		pd.setPropertyNames(new String[] { "foobar" });

		String[] propertyNames = pd.getPropertyNames();
		assertEquals(1, propertyNames.length);
		assertEquals("foobar", propertyNames[0]);

		pd.setProfileClass(RepeatedValuesProfile.class);
		propertyNames = pd.getPropertyNames();
		assertEquals(2, propertyNames.length);
		assertEquals("foobar", propertyNames[0]);
		assertEquals("Significance rate (%)", propertyNames[1]);
	}
}