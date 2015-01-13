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

import org.datacleaner.api.Initialize;

/**
 * Descriptor for an initialize method. The most common way of registering an
 * initialize method is using the @Initialize annotation.
 * 
 * @see Initialize
 * 
 * 
 */
public interface InitializeMethodDescriptor extends Serializable {

    /**
     * Determines if this initialize method is distributed or not. 
     * 
     * @return
     * 
     * @see Initialize#distributed()
     */
    public boolean isDistributed();

    /**
     * Invokes the initialize method
     * 
     * @param component
     *            the component to initialize
     */
    public void initialize(Object component);

    /**
     * Gets the annotations of the method
     * 
     * @return the annotations of the method
     */
    public Set<Annotation> getAnnotations();

    /**
     * Gets a particular annotation of the method
     * 
     * @param <A>
     *            the annotation type
     * @param annotationClass
     *            the annotation class to look for
     * @return a matching annotation or null, if none is present
     */
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass);
}
