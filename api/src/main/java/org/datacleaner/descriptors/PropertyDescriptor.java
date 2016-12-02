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
package org.datacleaner.descriptors;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Set;

import org.apache.metamodel.util.HasName;

/**
 * Super-interface for descriptor objects that describe metadata about
 * properties, typically configurable by the user or the framework.
 *
 * A property exist within a component, which are described by the
 * {@link ComponentDescriptor} interface.
 *
 *
 */
public interface PropertyDescriptor extends Comparable<PropertyDescriptor>, Serializable, HasName {

    /**
     * Gets the name of the property
     *
     * @return a string representation, the name, of the property
     */
    @Override
    String getName();

    /**
     * Sets the value of the property on a component instance.
     *
     * @param component
     *            the component instance
     * @param value
     *            the new value of the property
     * @throws IllegalArgumentException
     *             if the component is invalid or the value has a wrong type
     */
    void setValue(Object component, Object value) throws IllegalArgumentException;

    /**
     * Gets the current value of this property on a component.
     *
     * @param component
     *            the component to get the value from
     * @return the current value of this property
     * @throws IllegalArgumentException
     *             if the component is invalid.
     */
    Object getValue(Object component) throws IllegalArgumentException;

    Set<Annotation> getAnnotations();

    <A extends Annotation> A getAnnotation(Class<A> annotationClass);

    /**
     * Gets the property type, as specified by the field representing the
     * property
     *
     * @return
     */
    Class<?> getType();

    /**
     * @return whether or not the type of the property type is an array
     */
    boolean isArray();

    /**
     * @return the type of the property or the component type of the array, if
     *         the property type is an array
     */
    Class<?> getBaseType();

    /**
     * Gets the descriptor of the component that has this property.
     *
     * @return a descriptor object of the owning component.
     */
    ComponentDescriptor<?> getComponentDescriptor();

    int getTypeArgumentCount();

    Class<?> getTypeArgument(int index) throws IndexOutOfBoundsException;

}
