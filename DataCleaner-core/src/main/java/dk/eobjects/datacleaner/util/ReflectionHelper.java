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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Helper-class for doing common reflection tasks
 */
public class ReflectionHelper {

	/**
	 * Resolves a property via reflection from an array of similar objects
	 * 
	 * @param objects
	 *            the array to get the property from
	 * @param propertyName
	 *            the name of the property
	 * @return an array with the property values
	 */
	public static Object[] getProperties(Object[] objects, String propertyName) {
		Object[] result = new Object[objects.length];
		for (int i = 0; i < objects.length; i++) {
			result[i] = getProperty(objects[i], propertyName);
		}
		return result;
	}

	public static Object[] getProperties(Collection<? extends Object> objects,
			String string) {
		return getProperties(objects.toArray(new Object[objects.size()]),
				string);
	}

	/**
	 * Investigates an object and tries to resolve a property for the object.
	 * This is done by searching for fields or setter-methods that matches the
	 * propertyname
	 * 
	 * @param object
	 * @param propertyName
	 * @return the value of the property
	 * @throws IllegalArgumentException
	 *             if such a property does not exist
	 */
	public static Object getProperty(Object object, String propertyName) {
		Class<? extends Object> clazz = object.getClass();
		try {
			Field field = clazz.getField(propertyName);
			return field.get(object);
		} catch (Exception e) {
			try {
				StringBuilder sb = new StringBuilder(propertyName);
				sb.replace(0, 1, ""
						+ Character.toUpperCase(propertyName.charAt(0)));
				Method method = clazz.getMethod("get" + sb.toString(),
						new Class[0]);
				return method.invoke(object, new Object[0]);
			} catch (Exception ex) {
				throw new IllegalArgumentException(ex);
			}
		}
	}

	/**
	 * Investigates a class and get it's constants
	 * 
	 * @param clazz
	 *            the class to be investigated
	 * @return an array of fields, representing the constants
	 */
	public static Field[] getConstants(Class<? extends Object> clazz) {
		List<Field> result = new ArrayList<Field>();
		Field[] fields = clazz.getFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			int mod = field.getModifiers();
			if (Modifier.isFinal(mod) && Modifier.isPublic(mod)
					&& Modifier.isStatic(mod)) {
				result.add(field);
			}
		}
		return result.toArray(new Field[result.size()]);
	}

	public static List<String> getIteratedProperties(String prefix,
			Map<String, String> properties) {
		List<String> result = new ArrayList<String>();
		if (properties != null) {
			for (int i = 0; true; i++) {
				String value = properties.get(prefix + i);
				if (value == null) {
					break;
				}
				result.add(value);
			}
		}
		return result;
	}

	public static void addIteratedProperties(Map<String, String> properties,
			String prefix, String[] values) {
		for (int i = 0; i < values.length; i++) {
			properties.put(prefix + i, values[i]);
		}
	}
}