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

import junit.framework.TestCase;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.Renderable;
import org.datacleaner.result.CrosstabResult;
import org.datacleaner.result.renderer.CrosstabTextRenderer;
import org.datacleaner.result.renderer.ToStringTextRenderer;
import org.datacleaner.result.renderer.TextRenderingFormat;
import org.datacleaner.test.mock.MockRenderers.InvalidRenderer1;
import org.datacleaner.test.mock.MockRenderers.InvalidRenderer2;
import org.datacleaner.test.mock.MockRenderers.InvalidRenderer3;
import org.datacleaner.test.mock.MockRenderers.InvalidRenderer4;

public class AnnotationBasedRendererBeanDescriptorTest extends TestCase {

	private RendererBeanDescriptor<ToStringTextRenderer> descriptor = Descriptors.ofRenderer(ToStringTextRenderer.class);

	public void testGetRenderingFormat() throws Exception {
		assertEquals(TextRenderingFormat.class, descriptor.getRenderingFormat());
	}

	public void testGetRenderableType() throws Exception {
		Class<? extends Renderable> renderableType = descriptor.getRenderableType();
		assertEquals(AnalyzerResult.class, renderableType);

		RendererBeanDescriptor<CrosstabTextRenderer> desc2 = Descriptors.ofRenderer(CrosstabTextRenderer.class);
		assertEquals(CrosstabResult.class, desc2.getRenderableType());
	}

	public void testIsOutputApplicableFor() throws Exception {
		assertTrue(descriptor.isOutputApplicableFor(CharSequence.class));
		assertTrue(descriptor.isOutputApplicableFor(String.class));

		assertFalse(descriptor.isOutputApplicableFor(Number.class));
		assertFalse(descriptor.isOutputApplicableFor(StringBuilder.class));
	}
	
	public void testInvalidRendererAnnotations() throws Exception {
		try {
			Descriptors.ofRenderer(InvalidRenderer1.class);
			fail("Exception expected");
		} catch (DescriptorException e) {
			assertEquals("The renderer output type (class java.lang.Object) is not a valid instance or sub-class "
					+ "of format output type (interface java.lang.CharSequence)", e.getMessage());
		}

		try {
			Descriptors.ofRenderer(InvalidRenderer2.class);
			fail("Exception expected");
		} catch (DescriptorException e) {
			assertEquals(
					"class org.datacleaner.test.mock.MockRenderers$InvalidRenderer2 doesn't implement the RendererBean annotation",
					e.getMessage());
		}

		try {
			Descriptors.ofRenderer(InvalidRenderer3.class);
			fail("Exception expected");
		} catch (DescriptorException e) {
			assertEquals(
			        "Component (interface org.datacleaner.test.mock.MockRenderers$InvalidRenderer3) is not a non-abstract class",
					e.getMessage());
		}

		try {
			Descriptors.ofRenderer(InvalidRenderer4.class);
			fail("Exception expected");
		} catch (DescriptorException e) {
			assertEquals(
					"Rendering format (class org.datacleaner.test.mock.MockRenderers$InvalidRenderingFormat) is not a non-abstract class",
					e.getMessage());
		}
	}
}
