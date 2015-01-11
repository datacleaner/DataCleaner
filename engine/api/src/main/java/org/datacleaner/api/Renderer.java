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
package org.datacleaner.api;


/**
 * Interface for renderers of analyzer results. Renderers are used to transform
 * the logical results into readable results such as HTML pages, Swing
 * components, Text or XML files.
 * 
 * @param <I>
 *            the input of the renderer, ie. the result type (or
 *            {@link Renderable} in general) to render
 * @param <O>
 *            the output type of the renderer. This should be the same as or a
 *            subclass of the output class of the matching RenderingFormat.
 * 
 * @see RendererBean
 * @see RenderingFormat
 */
public interface Renderer<I extends Renderable, O> {

	/**
	 * Checks whether this renderer is capable of rendering a particular
	 * renderable. This method allows renderers to respond to specific state
	 * available in the renderables.
	 * 
	 * @param renderable
	 * @return
	 */
	public RendererPrecedence getPrecedence(I renderable);

	/**
	 * Renders an analyzer result.
	 * 
	 * @param renderable
	 *            the {@link Renderable} to render.
	 * @return the outcoming, rendered result.
	 */
	public O render(I renderable);
}
