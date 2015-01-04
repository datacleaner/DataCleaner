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
package org.eobjects.analyzer.descriptors;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Set;

import org.eobjects.analyzer.beans.api.Close;

/**
 * Descriptor for a close method. The most common way of registering a close
 * method is by using the @Close annotation or by implementing the Closeable
 * interface.
 * 
 * @see Close
 * 
 * 
 * 
 */
public interface CloseMethodDescriptor extends Serializable {

    /**
     * Determines if this close method is distributed or not.
     * 
     * @return
     * 
     * @see Close#distributed()
     */
    public boolean isDistributed();

    /**
     * Determines if this close method should be run when the context of the
     * component (typically a job execution) is successful.
     * 
     * @return
     * 
     * @see Close#onSuccess()
     */
    public boolean isEnabledOnSuccess();

    /**
     * Determines if this close method should be run when the context of the
     * component (typically a job execution) is in a failure state.
     * 
     * @return
     * 
     * @see Close#onFailure()
     */
    public boolean isEnabledOnFailure();

    /**
     * Invokes the close method
     * 
     * @param bean
     */
    public void close(Object bean);

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
