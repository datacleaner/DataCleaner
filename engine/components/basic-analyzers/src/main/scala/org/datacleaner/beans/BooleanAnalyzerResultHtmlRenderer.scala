package org.datacleaner.beans
import org.datacleaner.beans.api.Provided
import org.datacleaner.beans.api.RendererBean
import org.datacleaner.beans.api.Renderer
import org.datacleaner.beans.api.RendererPrecedence
import org.datacleaner.result.html.HtmlFragment
import org.datacleaner.result.renderer.RendererFactory
import javax.inject.Inject
import org.datacleaner.result.renderer.HtmlRenderingFormat

@RendererBean(classOf[HtmlRenderingFormat])
class BooleanAnalyzerResultHtmlRenderer(rf: RendererFactory) extends Renderer[BooleanAnalyzerResult, HtmlFragment] {

  @Inject
  @Provided
  var rendererFactory: RendererFactory = rf;

  def this() = this(null)

  override def getPrecedence(result: BooleanAnalyzerResult) = RendererPrecedence.HIGH

  override def render(result: BooleanAnalyzerResult) = new BooleanAnalyzerHtmlFragment(rendererFactory, result);
}
