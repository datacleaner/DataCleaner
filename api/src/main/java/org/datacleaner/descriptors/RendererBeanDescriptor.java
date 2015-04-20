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

import org.datacleaner.api.Renderable;
import org.datacleaner.api.Renderer;
import org.datacleaner.api.RendererBean;
import org.datacleaner.api.RenderingFormat;

/**
 * Defines an abstract {@link ComponentDescriptor} for {@link Renderer}s.
 * 
 * @see RendererBean
 *
 * @param <R>
 *            the type of {@link Renderer}
 */
public interface RendererBeanDescriptor<R extends Renderer<?, ?>> extends ComponentDescriptor<R> {

    /**
     * Gets the {@link RenderingFormat} that this renderer pertains to. The type
     * returned by {@link RenderingFormat#getOutputClass()} will typically
     * correspond to accepted types of {@link #isOutputApplicableFor(Class)}.
     * 
     * @return
     */
    public Class<? extends RenderingFormat<?>> getRenderingFormat();

    /**
     * Gets the type of {@link Renderable} that this renderer can render.
     * 
     * @return
     */
    public Class<? extends Renderable> getRenderableType();

    /**
     * Determines if an object of the provided class argument can be provided
     * using this renderer.
     * 
     * @see #getRenderingFormat()
     * 
     * @param requiredClass
     * @return
     */
    boolean isOutputApplicableFor(Class<?> requiredClass);
}
