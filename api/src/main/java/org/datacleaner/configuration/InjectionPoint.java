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

import javax.inject.Inject;

/**
 * Defines an injection point in a component. An injection point is typically
 * derived from the {@link Inject} annotation
 * 
 * @param <E>
 */
public interface InjectionPoint<E> {

    public <A extends Annotation> A getAnnotation(Class<A> annotationClass);

    /**
     * Gets the instance that requests the injection
     * 
     * @return
     */
    public Object getInstance();

    /**
     * Gets the base type to inject. This will be the class of the injected
     * variable, eg. String, InputColumn, Number etc.
     * 
     * @return
     */
    public Class<E> getBaseType();

    /**
     * Gets whether the injected type has generic arguments in addition to the
     * base type
     * 
     * @return
     */
    public boolean isGenericType();

    /**
     * Gets the amount of generic arguments in the injected type
     * 
     * @return
     */
    public int getGenericTypeArgumentCount();

    /**
     * Gets a generic argument by index
     * 
     * @param i
     * @return
     * @throws IndexOutOfBoundsException
     */
    public Class<?> getGenericTypeArgument(int i) throws IndexOutOfBoundsException;
}
