/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.widgets.result;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.api.RenderingFormat;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.descriptors.RendererBeanDescriptor;
import org.eobjects.analyzer.result.ListResult;
import org.eobjects.analyzer.result.renderer.Renderable;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;

public class ListResultSwingRendererTest extends TestCase {

	public void testDescriptor() throws Exception {
		RendererBeanDescriptor descriptor = Descriptors.ofRenderer(ListResultSwingRenderer.class);
		Class<? extends Renderable> analyzerResultType = descriptor.getRenderableType();
		assertEquals(ListResult.class, analyzerResultType);

		Class<? extends RenderingFormat<?>> renderingFormat = descriptor.getRenderingFormat();
		assertEquals(SwingRenderingFormat.class, renderingFormat);
	}
}
