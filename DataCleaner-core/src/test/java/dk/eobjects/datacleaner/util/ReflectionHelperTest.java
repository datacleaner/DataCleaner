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
package dk.eobjects.datacleaner.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import junit.framework.TestCase;
import dk.eobjects.metamodel.schema.Table;

public class ReflectionHelperTest extends TestCase {

	public static final String SAMPLE_CONSTANT = "foo";
	public static final String ANOTHER_CONSTANT = "bar";
	public static String NOT_QUITE_A_CONSTANT = "I'm not declared as final";
	private static final String NON_PUBLIC_CONSTANT = "I'm private, so I shouldn't be accessible";

	public void testGetProperty() {
		Table table = new Table("foobar");

		Object property = ReflectionHelper.getProperty(table, "name");
		assertEquals("foobar", property);
	}

	public void testGetConstants() throws Exception {
		System.out.println(NON_PUBLIC_CONSTANT);
		Field[] constants = ReflectionHelper.getConstants(this.getClass());
		assertEquals(2, constants.length);
		assertEquals("SAMPLE_CONSTANT", constants[0].getName());
		assertEquals("ANOTHER_CONSTANT", constants[1].getName());
	}

	public void testGetIteratedProperties() throws Exception {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("foo", "bar");
		properties.put("bar", "foo");
		properties.put("foobar_0", "f");
		properties.put("foobar_1", "o");
		properties.put("foobar_2", "o");
		properties.put("foobar_3", "b");
		properties.put("foobar_4", "a");
		properties.put("foobar_5", "r");

		List<String> result = ReflectionHelper.getIteratedProperties("foobar_",
				properties);
		assertEquals("{f,o,o,b,a,r}", ArrayUtils.toString(result.toArray()));
	}

	public void testAddIteratedProperties() throws Exception {
		Map<String, String> properties = new HashMap<String, String>();
		ReflectionHelper.addIteratedProperties(properties, "foobar_",
				new String[] { "f", "o", "o", "b", "a", "r" });

		assertEquals("f", properties.get("foobar_0"));
		assertEquals("o", properties.get("foobar_1"));
		assertEquals("o", properties.get("foobar_2"));
		assertEquals("b", properties.get("foobar_3"));
		assertEquals("a", properties.get("foobar_4"));
		assertEquals("r", properties.get("foobar_5"));
		assertEquals(6, properties.size());
	}
}