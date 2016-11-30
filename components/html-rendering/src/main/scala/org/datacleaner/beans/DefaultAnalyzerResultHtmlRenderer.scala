package org.datacleaner.beans
import org.datacleaner.api.{AnalyzerResult, Renderer, RendererBean, RendererPrecedence}
import org.datacleaner.result.html.{HtmlFragment, SimpleHtmlFragment}
import org.datacleaner.result.renderer.HtmlRenderingFormat

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
