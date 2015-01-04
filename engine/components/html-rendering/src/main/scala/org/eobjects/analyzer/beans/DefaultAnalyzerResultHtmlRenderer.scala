package org.eobjects.analyzer.beans
import org.eobjects.analyzer.result.html.HtmlRenderer
import org.eobjects.analyzer.result.AnalyzerResult
import org.eobjects.analyzer.result.html.SimpleHtmlFragment
import org.eobjects.analyzer.beans.api.RendererPrecedence
import org.eobjects.analyzer.beans.api.RendererBean
import org.eobjects.analyzer.result.renderer.HtmlRenderingFormat
import javax.inject.Inject
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor
import scala.collection.JavaConversions._
import org.eobjects.analyzer.job.AnalyzerJob
import org.eobjects.analyzer.data.InputColumn
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor
import org.eobjects.analyzer.descriptors.MetricParameters
import org.eobjects.analyzer.beans.api.Provided
import org.eobjects.analyzer.beans.api.Renderer
import org.eobjects.analyzer.result.html.HtmlFragment

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