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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.apache.metamodel.util.CollectionUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Additional (to {@link CollectionUtils} utility methods for common collection
 * or array operations.
 *
 *
 */
public final class CollectionUtils2 {

    private CollectionUtils2() {
        // prevent instantiation
    }

    /**
     * Refines a list of candidate objects based on a inclusion predicate. If no
     * candidates are found, the original list will be retained in the result.
     * Therefore the result will always have 1 or more elements in it.
     *
     * @param candidates
     * @param predicate
     * @return
     */
    public static <E> List<E> refineCandidates(final List<E> candidates, final Predicate<? super E> predicate) {
        if (candidates.size() == 1) {
            return candidates;
        }
        final List<E> newCandidates = CollectionUtils.filter(candidates, predicate);
        if (newCandidates.isEmpty()) {
            return candidates;
        }
        return newCandidates;
    }

    @SuppressWarnings("unchecked")
    public static <E> List<E> filterOnClass(final Collection<?> superTypeList, final Class<E> subType) {
        final List<E> result = new ArrayList<>();
        for (final Object object : superTypeList) {
            if (object != null) {
                if (ReflectionUtils.is(object.getClass(), subType)) {
                    result.add((E) object);
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <E> E[] array(final Class<E> elementClass, final Object existingArray, final E... elements) {
        if (existingArray == null) {
            return elements;
        }
        final E[] result;
        if (existingArray.getClass().isArray()) {
            final int length = Array.getLength(existingArray);
            result = (E[]) Array.newInstance(elementClass, length + elements.length);
            System.arraycopy(existingArray, 0, result, 0, length);
            System.arraycopy(elements, 0, result, length, elements.length);
        } else {
            result = (E[]) Array.newInstance(elementClass, 1 + elements.length);
            result[0] = (E) existingArray;
            System.arraycopy(elements, 0, result, 1, elements.length);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static <E> E[] arrayOf(final Class<E> elementClass, final Object arrayOrElement) {
        if (arrayOrElement == null) {
            return null;
        }
        if (arrayOrElement.getClass().isArray()) {
            return (E[]) arrayOrElement;
        }
        final Object result = Array.newInstance(elementClass, 1);
        Array.set(result, 0, arrayOrElement);
        return (E[]) result;
    }

    public static <E> List<E> sorted(final Collection<E> col, final Comparator<? super E> comparator) {
        final List<E> list = new ArrayList<>(col);
        Collections.sort(list, comparator);
        return list;
    }

    public static <E extends Comparable<? super E>> List<E> sorted(final Collection<E> col) {
        final List<E> list = new ArrayList<>(col);
        Collections.sort(list);
        return list;
    }

    /**
     * Creates a typical Google Guava cache
     *
     * @param maximumSize
     * @param expiryDurationSeconds
     * @return
     */
    public static <K, V> Cache<K, V> createCache(final int maximumSize, final long expiryDurationSeconds) {
        return CacheBuilder.newBuilder().maximumSize(maximumSize)
                .expireAfterAccess(expiryDurationSeconds, TimeUnit.SECONDS).build();
    }

    public static Object toArray(final List<?> list, final Class<?> componentType) {
        final int size = list.size();
        final Object result = Array.newInstance(componentType, size);
        if (!componentType.isPrimitive()) {
            for (int i = 0; i < size; i++) {
                Array.set(result, i, list.get(i));
            }
        } else if (componentType == boolean.class) {
            for (int i = 0; i < size; i++) {
                Array.setBoolean(result, i, (Boolean) list.get(i));
            }
        } else if (componentType == byte.class) {
            for (int i = 0; i < size; i++) {
                Array.setByte(result, i, (Byte) list.get(i));
            }
        } else if (componentType == short.class) {
            for (int i = 0; i < size; i++) {
                Array.setShort(result, i, (Short) list.get(i));
            }
        } else if (componentType == int.class) {
            for (int i = 0; i < size; i++) {
                Array.setInt(result, i, (Integer) list.get(i));
            }
        } else if (componentType == long.class) {
            for (int i = 0; i < size; i++) {
                Array.setLong(result, i, (Long) list.get(i));
            }
        } else if (componentType == float.class) {
            for (int i = 0; i < size; i++) {
                Array.setFloat(result, i, (Float) list.get(i));
            }
        } else if (componentType == double.class) {
            for (int i = 0; i < size; i++) {
                Array.setDouble(result, i, (Double) list.get(i));
            }
        } else if (componentType == char.class) {
            for (int i = 0; i < size; i++) {
                Array.setChar(result, i, (Character) list.get(i));
            }
        }
        return result;
    }
}
