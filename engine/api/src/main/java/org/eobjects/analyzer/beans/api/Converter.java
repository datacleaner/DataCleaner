/**
 * AnalyzerBeans
 * Copyright (C) 2014 Neopost - Customer Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.beans.api;

/**
 * Interface for converting objects from and to strings. Used to serialize
 * configured properties to external representations.
 * 
 * Converters should supply a no-args constructor.
 * 
 * Usually Converters are registered on a class or a configured property using
 * the {@link Convertable} annotation.
 * 
 * @param <E>
 */
public interface Converter<E> {

	/**
	 * Converts a string back to a Java object.
	 * 
	 * @param type
	 *            the specific type of object required. This will typically be
	 *            the "E" type, but since E can be a supertype, you can use this
	 *            type parameter to inspect subtypes.
	 * @param serializedForm
	 * @return
	 */
	public E fromString(Class<?> type, String serializedForm);

	/**
	 * Converts a Java object into a string.
	 * 
	 * @param instance
	 * @return
	 */
	public String toString(E instance);

	/**
	 * Determines if this converter is able to convert the particular type.
	 * 
	 * @param type
	 * @return
	 */
	public boolean isConvertable(Class<?> type);
}
