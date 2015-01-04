package org.datacleaner.beans
import org.datacleaner.result.html.HtmlRenderer
import org.datacleaner.result.AnalyzerResult
import org.datacleaner.result.html.SimpleHtmlFragment
import org.datacleaner.beans.api.RendererPrecedence
import org.datacleaner.beans.api.RendererBean
import org.datacleaner.result.renderer.HtmlRenderingFormat
import javax.inject.Inject
import org.datacleaner.descriptors.AnalyzerBeanDescriptor
import scala.collection.JavaConversions._
import org.datacleaner.job.AnalyzerJob
import org.datacleaner.data.InputColumn
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor
import org.datacleaner.descriptors.MetricParameters
import org.datacleaner.beans.api.Provided
import org.datacleaner.beans.api.Renderer
import org.datacleaner.result.html.HtmlFragment

/**
 * The default HTML renderer for any AnalyzerResult.
 */
@RendererBean(classOf[HtmlRenderingFormat])
class DefaultAnalyzerResultHtmlRenderer extends Renderer[AnalyzerResult, HtmlFragment] {

  override def getPrecedence(renderable: AnalyzerResult) = RendererPrecedence.LOWEST;

  override def render(result: AnalyzerResult): HtmlFragment = {
    val frag = new SimpleHtmlFragment();
    frag.addBodyElement(new MetricListBodyElement(result));
    return frag;
  }
}
