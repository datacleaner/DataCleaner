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
package org.datacleaner.result.renderer;

import java.util.LinkedList;

import junit.framework.TestCase;

import org.datacleaner.beans.api.Renderer;
import org.datacleaner.configuration.AnalyzerBeansConfigurationImpl;
import org.datacleaner.descriptors.ClasspathScanDescriptorProvider;
import org.datacleaner.result.CrosstabResult;
import org.datacleaner.result.DataSetResult;
import org.datacleaner.result.NumberResult;
import org.datacleaner.test.mock.MockRenderers.BarPrecedenceRenderer;
import org.datacleaner.test.mock.MockRenderers.ConditionalPrecedenceRenderer;
import org.datacleaner.test.mock.MockRenderers.FooPrecedenceRenderer;
import org.datacleaner.test.mock.MockRenderers.RenderableString;
import org.apache.metamodel.data.Row;

public class RendererFactoryTest extends TestCase {

    public void testGetRendererByHierarchyDistance() throws Exception {
        ClasspathScanDescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider().scanPackage(
                "org.datacleaner.result.renderer", true);
        RendererFactory rendererFactory = new RendererFactory(
                new AnalyzerBeansConfigurationImpl().replace(descriptorProvider));
        Renderer<?, ? extends CharSequence> r;

        r = rendererFactory.getRenderer(new NumberResult(1), TextRenderingFormat.class);
        assertEquals(ToStringTextRenderer.class, r.getClass());

        r = rendererFactory.getRenderer(new CrosstabResult(null), TextRenderingFormat.class);
        assertEquals(CrosstabTextRenderer.class, r.getClass());

        r = rendererFactory.getRenderer(new DataSetResult(new LinkedList<Row>()), TextRenderingFormat.class);
        assertEquals(MetricBasedResultTextRenderer.class, r.getClass());
    }

    public void testGetRendererByPrecedence() throws Exception {
        ClasspathScanDescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider();
        descriptorProvider.addRendererClass(FooPrecedenceRenderer.class);
        descriptorProvider.addRendererClass(BarPrecedenceRenderer.class);

        AnalyzerBeansConfigurationImpl conf = new AnalyzerBeansConfigurationImpl().replace(descriptorProvider);

        RendererFactory factory = new RendererFactory(conf);
        assertEquals(FooPrecedenceRenderer.class,
                factory.getRenderer(new RenderableString("foobar"), TextRenderingFormat.class).getClass());
        assertEquals(FooPrecedenceRenderer.class,
                factory.getRenderer(new RenderableString("foo"), TextRenderingFormat.class).getClass());
        assertEquals(BarPrecedenceRenderer.class,
                factory.getRenderer(new RenderableString("bar"), TextRenderingFormat.class).getClass());

        descriptorProvider.addRendererClass(ConditionalPrecedenceRenderer.class);

        assertEquals(FooPrecedenceRenderer.class,
                factory.getRenderer(new RenderableString("foo"), TextRenderingFormat.class).getClass());
        assertEquals(BarPrecedenceRenderer.class,
                factory.getRenderer(new RenderableString("bar"), TextRenderingFormat.class).getClass());

        assertEquals(FooPrecedenceRenderer.class,
                factory.getRenderer(new RenderableString("MEDIUM"), TextRenderingFormat.class).getClass());
        assertEquals(ConditionalPrecedenceRenderer.class,
                factory.getRenderer(new RenderableString("HIGHEST"), TextRenderingFormat.class).getClass());
    }
}
