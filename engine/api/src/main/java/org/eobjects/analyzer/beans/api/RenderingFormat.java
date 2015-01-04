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
package org.eobjects.analyzer.beans.api;


/**
 * Represents a rendering format to be used for rendering. AnalyzerBeans ships
 * with a couple of built-in rendering formats (eg. HTML and Text), but it is
 * also possible to roll your own. Simply create a class that implements this
 * interface and reference the class in the @RendererBean annotation when
 * implementing renderers.
 * 
 * @param <T>
 * 
 * @see RendererBean
 * @see Renderer
 */
public interface RenderingFormat<T> {

	public Class<T> getOutputClass();
}
