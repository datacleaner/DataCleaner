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
 * Classes that are annotated with the @TransformerBean annotation are
 * components for data transformation.
 * 
 * A @TransformerBean annotated class should implement the Transformer
 * interface.
 * 
 * TransformerBeans are by default assumed to be concurrent and thread-safe.
 * This behaviour can be overridden by using the @Concurrent annotation.
 * 
 * @see Transformer
 * @see Concurrent
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface TransformerBean {

	/**
	 * The display name of the Transformer. The display name should be humanly
	 * readable and is presented to the user in User Interfaces.
	 * 
	 * @return the name of the TransformerBean
	 */
	String value();
}
