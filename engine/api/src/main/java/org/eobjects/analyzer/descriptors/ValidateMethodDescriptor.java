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
package org.eobjects.analyzer.descriptors;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Set;

import org.eobjects.analyzer.beans.api.Validate;

/**
 * Descriptor for a validation method. The most common way of registering a
 * validateion method is using the @Validate annotation.
 * 
 * @see Validate
 * 
 * 
 */
public interface ValidateMethodDescriptor extends Serializable {

	/**
	 * Invokes the validate method
	 * 
	 * @param component
	 *            the component to validate
	 */
	public void validate(Object component);

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
