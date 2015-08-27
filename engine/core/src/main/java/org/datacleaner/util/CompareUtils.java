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
package org.datacleaner.util;

public final class CompareUtils {

    private CompareUtils() {
        // prevent instantiation
    }

    /**
     * Compares two objects with an unbound (and thus unsafe) {@link Comparable}
     * . Use {@link #compare(Comparable, Object)} if possible.
     * 
     * @param obj1
     * @param obj2
     * @return a negative integer, zero, or a positive integer as the first
     *         argument is less than, equal to, or greater than the second.
     * 
     * @throws ClassCastException
     *             if obj1's Comparable type is not compatible with the argument
     *             obj2.
     */
    public static final int compareUnbound(Comparable<?> obj1, Object obj2) throws ClassCastException {
        if (obj1 == obj2) {
            return 0;
        }
        if (obj1 == null) {
            return -1;
        }
        if (obj2 == null) {
            return 1;
        }

        @SuppressWarnings("unchecked")
        final Comparable<Object> castedObj1 = (Comparable<Object>) obj1;
        return castedObj1.compareTo(obj2);
    }

    /**
     * Compares two objects of which one of them is a comparable of the other.
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
