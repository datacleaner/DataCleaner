package org.datacleaner.beans.valuedist
import javax.inject.Inject

import org.datacleaner.api.{Provided, Renderer, RendererBean, RendererPrecedence}
import org.datacleaner.result.ValueCountingAnalyzerResult
import org.datacleaner.result.html.HtmlFragment
import org.datacleaner.result.renderer.{HtmlRenderingFormat, RendererFactory}

@RendererBean(classOf[HtmlRenderingFormat])
class ValueDistributionResultHtmlRenderer(rf: RendererFactory) extends Renderer[ValueCountingAnalyzerResult, HtmlFragment] {

  @Inject
  @Provided
  var rendererFactory: RendererFactory = rf;

  def this() = this(null)

  override def getPrecedence(result: ValueCountingAnalyzerResult) = RendererPrecedence.HIGH;

  override def render(result: ValueCountingAnalyzerResult): HtmlFragment = new ValueDistributionHtmlFragment(result, rendererFactory);
}
