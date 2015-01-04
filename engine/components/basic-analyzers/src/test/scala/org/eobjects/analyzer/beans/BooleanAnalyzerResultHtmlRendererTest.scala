package org.eobjects.analyzer.beans
import org.eobjects.analyzer.beans.api.RendererBean
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl
import org.eobjects.analyzer.data.MockInputColumn
import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider
import org.eobjects.analyzer.result.html.DefaultHtmlRenderingContext
import org.eobjects.analyzer.result.renderer.RendererFactory
import org.junit.Test
import org.junit.Assert
import org.scalatest.junit.AssertionsForJUnit

class BooleanAnalyzerResultHtmlRendererTest extends AssertionsForJUnit {
  
  val context = new DefaultHtmlRenderingContext();

  @Test
  def testRender = {
    val descriptorProvider = new ClasspathScanDescriptorProvider().scanPackage("org.eobjects.analyzer.beans", false)
    val conf = new AnalyzerBeansConfigurationImpl().replace(descriptorProvider);
    val renderer = new BooleanAnalyzerResultHtmlRenderer(new RendererFactory(conf))

    val column = new MockInputColumn[java.lang.Boolean]("my bool")
    val analyzer = new BooleanAnalyzer(Array(column));
    analyzer.init();
    val htmlFragment = renderer.render(analyzer.getResult())
    
    htmlFragment.initialize(new DefaultHtmlRenderingContext());

    assert(1 == htmlFragment.getBodyElements().size())

    Assert.assertEquals("""<div class="booleanAnalyzerResult"><table class="crosstabTable"><tr class="odd"><td class="empty"></td><td class="crosstabHorizontalHeader">my bool</td></tr><tr class="even"><td class="crosstabVerticalHeader">Row count</td><td class="value">0</td></tr><tr class="odd"><td class="crosstabVerticalHeader">Null count</td><td class="value">0</td></tr><tr class="even"><td class="crosstabVerticalHeader">True count</td><td class="value">0</td></tr><tr class="odd"><td class="crosstabVerticalHeader">False count</td><td class="value">0</td></tr></table></div>""", htmlFragment.getBodyElements().get(0).toHtml(context));
  }
}