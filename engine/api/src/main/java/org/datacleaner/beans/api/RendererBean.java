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
 * Annotation used to mark a class as a renderer. Mostly renderers are used for
 * AnalyzerResults, but can also be used as a UI representation of eg.
 * properties, components and more.
 * 
 * Renderers are grouped together by rendering formats, which are defined by the
 * parameter to this annotation. This makes it possible to combine renderers for
 * particular result types and for particular output formats such as HTML,
 * Swing, clear text, XML etc.
 * 
 * Renderers are resolved by combining the rendering format with the best
 * fitting output type defined by the Renderer interface.
 * 
 * The configuration of renderer beans are not standardized since they target
 * very different situations. A RendererFactory is used to retrieve
 * perform instantiation and initialization of a renderer, so any life cycle
 * steps pertaining to renderer initialization and more is dependent on the
 * usage of the RendererFactory.
 * 
 * @see RenderingFormat
 * @see Renderer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface RendererBean {

	/**
	 * Defines the rendering format of this renderer bean.
	 * 
	 * @return the class constant which represents the rendering format.
	 */
	public Class<? extends RenderingFormat<?>> value();
}
