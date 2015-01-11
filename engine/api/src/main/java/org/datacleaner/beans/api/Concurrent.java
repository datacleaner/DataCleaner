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
package org.datacleaner.beans.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to override the default component concurrency model. Any
 * component (transformer, filter or analyzer) with this annotation can define
 * whether or not the framework should be allowed to invoke the component
 * concurrently (ie. from several threads at the same time) or not.
 * 
 * The default behaviour of the components is:
 * 
 * <ul>
 * <li>Transformers and Filters are invoked concurrently. The rationale behind
 * this default value is that the invoked methods (transform(...) and
 * categorize(...)) both return their results immidiately and thus a stateless
 * implementation will be the normal scenario.</li>
 * <li>Analyzers are <i>not</i> invoked concurrently. The rationale behind this
 * default value is that analyzers are expected to build up it's result during
 * execution and thus will typically be stateful.</li>
 * </ul>
 * 
 * @see Component
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Concurrent {

	/**
	 * Determines whether or not the component with this annotation is
	 * thread-safe/concurrent.
	 * 
	 * @return a boolean indicating whether or not concurrent execution of the
	 *         component is allowed.
	 */
	public boolean value();
}
