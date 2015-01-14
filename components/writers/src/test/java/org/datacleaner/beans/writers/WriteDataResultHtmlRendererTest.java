package org.datacleaner.beans.writers;

import java.util.Collection;
import java.util.TreeSet;

import org.datacleaner.api.Renderer;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.configuration.AnalyzerBeansConfigurationImpl;
import org.datacleaner.descriptors.ClasspathScanDescriptorProvider;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.RendererBeanDescriptor;
import org.datacleaner.result.html.DefaultHtmlRenderingContext;
import org.datacleaner.result.html.HtmlFragment;
import org.datacleaner.result.html.HtmlRenderingContext;
import org.datacleaner.result.renderer.HtmlRenderingFormat;
import org.datacleaner.result.renderer.RendererFactory;
import org.junit.Assert;
import org.junit.Test;

public class WriteDataResultHtmlRendererTest {

    @Test
    public void testRendering() {
        WriteDataResult result = new WriteDataResultImpl(2, 3, "datastore", "schema", "table");
        WriteDataResultHtmlRenderer renderer = new WriteDataResultHtmlRenderer();
        HtmlFragment htmlFragment = renderer.render(result);

        Assert.assertEquals(0, htmlFragment.getHeadElements().size());
        Assert.assertEquals(1, htmlFragment.getBodyElements().size());

        HtmlRenderingContext context = new DefaultHtmlRenderingContext();

        Assert.assertEquals("<div>\n  <p>Executed 2 inserts</p>\n  <p>Executed 3 updates</p>\n</div>", htmlFragment
                .getBodyElements().get(0).toHtml(context).replaceAll("\r\n", "\n"));
    }

    @Test
    public void testClasspathDiscovery() {
        final DescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider().scanPackage(
                "org.datacleaner.beans", true);

        final Collection<RendererBeanDescriptor<?>> htmlRenderers = descriptorProvider
                .getRendererBeanDescriptorsForRenderingFormat(HtmlRenderingFormat.class);
        final TreeSet<RendererBeanDescriptor<?>> sorted = new TreeSet<>(htmlRenderers);

        Assert.assertEquals(
                "[AnnotationBasedRendererBeanDescriptor[org.datacleaner.beans.DefaultAnalyzerResultHtmlRenderer], "
                        + "AnnotationBasedRendererBeanDescriptor[org.datacleaner.beans.writers.WriteDataResultHtmlRenderer]]",
                sorted.toString());

        final AnalyzerBeansConfiguration conf = new AnalyzerBeansConfigurationImpl().replace(descriptorProvider);
        final RendererFactory rendererFactory = new RendererFactory(conf);

        final Renderer<? super WriteDataResultImpl, ? extends HtmlFragment> renderer = rendererFactory.getRenderer(
                new WriteDataResultImpl(2, 3, "datastore", "schema", "table"), HtmlRenderingFormat.class);

        Assert.assertEquals(WriteDataResultHtmlRenderer.class, renderer.getClass());
    }
}
