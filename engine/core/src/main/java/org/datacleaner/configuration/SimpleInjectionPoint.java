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
package org.datacleaner.configuration;

import java.lang.annotation.Annotation;

/**
 * Represents an {@link InjectionPoint} that is not tied to a field, parameter
 * or other member or property.
 * 
 * @param <E>
 */
public class SimpleInjectionPoint<E> implements InjectionPoint<E> {

    private final Class<E> _class;

    /**
     * Factory method to produce an {@link InjectionPoint} that describes the
     * specified class.
     * 
     * @param cls
     * @return
     */
    public static final <E> InjectionPoint<E> of(Class<E> cls) {
        return new SimpleInjectionPoint<E>(cls);
    }

    /**
     * Constructs a {@link SimpleInjectionPoint} for requesting a specific class
     * 
     * @param cls
     */
    public SimpleInjectionPoint(Class<E> cls) {
        if (cls == null) {
            throw new IllegalArgumentException("Injection class cannot be null");
        }
        _class = cls;
    }

    @Override
    public String toString() {
        return "SimpleInjectionPoint[" + _class.getName() + "]";
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return null;
    }

    @Override
    public Object getInstance() {
        return this;
    }

    @Override
    public Class<E> getBaseType() {
        return _class;
    }

    @Override
    public boolean isGenericType() {
        return false;
    }

    @Override
    public int getGenericTypeArgumentCount() {
        return 0;
    }

    @Override
    public Class<?> getGenericTypeArgument(int i) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException("This injection point has no generic type arguments, requested index no. "
                + i);
    }

}
