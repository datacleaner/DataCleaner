/**
 * DataCleaner (community edition)
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
package org.eobjects.analyzer.util;


public final class CompareUtils {

	private CompareUtils() {
		// prevent instantiation
	}

	/**
	 * 
	 * @param <E>
	 * @param obj1
	 * @param obj2
	 * @return a negative integer, zero, or a positive integer as the first
	 *         argument is less than, equal to, or greater than the second.
	 */
	public static final <E> int compare(Comparable<E> obj1, E obj2) {
		if (obj1 == obj2) {
			return 0;
		}
		if (obj1 == null) {
			return -1;
		}
		if (obj2 == null) {
			return 1;
		}

		return obj1.compareTo(obj2);
	}
}
