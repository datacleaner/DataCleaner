package org.datacleaner.beans.valuedist
import org.datacleaner.api.Provided
import org.datacleaner.api.RendererBean
import org.datacleaner.api.Renderer
import org.datacleaner.api.RendererPrecedence
import org.datacleaner.result.html.HtmlFragment
import org.datacleaner.result.renderer.HtmlRenderingFormat
import org.datacleaner.result.renderer.RendererFactory
import javax.inject.Inject
import org.datacleaner.result.ValueCountingAnalyzerResult

@RendererBean(classOf[HtmlRenderingFormat])
class ValueDistributionResultHtmlRenderer(rf: RendererFactory) extends Renderer[ValueCountingAnalyzerResult, HtmlFragment] {

  @Inject
  @Provided
  var rendererFactory: RendererFactory = rf;

  def this() = this(null)

  override def getPrecedence(result: ValueCountingAnalyzerResult) = RendererPrecedence.HIGH;

  override def render(result: ValueCountingAnalyzerResult): HtmlFragment = new ValueDistributionHtmlFragment(result, rendererFactory);
}
