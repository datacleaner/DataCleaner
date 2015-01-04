package org.eobjects.analyzer.beans.writers
import org.scalatest.junit.AssertionsForJUnit
import scala.collection.JavaConversions._
import org.junit.Test
import org.junit.Assert
import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl
import org.eobjects.analyzer.result.renderer.RendererFactory
import org.eobjects.analyzer.result.renderer.HtmlRenderingFormat
import org.eobjects.analyzer.result.html.DefaultHtmlRenderingContext

class WriteDataResultHtmlRendererTest extends AssertionsForJUnit {

  @Test
  def testRendering() = {
    val result = new WriteDataResultImpl(2, 3, "datastore", "schema", "table");
    val renderer = new WriteDataResultHtmlRenderer();
    val htmlFragment = renderer.render(result);

    assert(0 == htmlFragment.getHeadElements().size());
    assert(1 == htmlFragment.getBodyElements().size(), { "Found " + htmlFragment });
    
    val context = new DefaultHtmlRenderingContext();

    Assert.assertEquals("""<div>
                 
                 <p>Executed 2 inserts</p>
                 <p>Executed 3 updates</p>
                 
               </div>""".replaceAll("\r\n", "\n"), htmlFragment.getBodyElements().get(0).toHtml(context).replaceAll("\r\n", "\n"));
  }

  @Test
  def testClasspathDiscovery = {
    val descriptorProvider = new ClasspathScanDescriptorProvider().scanPackage("org.eobjects.analyzer.beans", true);

    val htmlRenderers = descriptorProvider.getRendererBeanDescriptorsForRenderingFormat(classOf[HtmlRenderingFormat]);
    Assert.assertEquals("AnnotationBasedRendererBeanDescriptor[org.eobjects.analyzer.beans.writers.WriteDataResultHtmlRenderer]," +
      "AnnotationBasedRendererBeanDescriptor[org.eobjects.analyzer.beans.DefaultAnalyzerResultHtmlRenderer]", htmlRenderers.mkString(","))

    val conf = new AnalyzerBeansConfigurationImpl().replace(descriptorProvider);
    val rendererFactory = new RendererFactory(conf);

    val renderer = rendererFactory.getRenderer(new WriteDataResultImpl(2, 3, "datastore", "schema", "table"), classOf[HtmlRenderingFormat])

    Assert.assertEquals(classOf[WriteDataResultHtmlRenderer], renderer.getClass());
  }
}