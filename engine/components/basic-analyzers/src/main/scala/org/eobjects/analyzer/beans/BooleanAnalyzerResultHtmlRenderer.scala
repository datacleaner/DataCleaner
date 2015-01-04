package org.eobjects.analyzer.beans
import org.eobjects.analyzer.beans.api.Provided
import org.eobjects.analyzer.beans.api.RendererBean
import org.eobjects.analyzer.beans.api.Renderer
import org.eobjects.analyzer.beans.api.RendererPrecedence
import org.eobjects.analyzer.result.html.HtmlFragment
import org.eobjects.analyzer.result.renderer.RendererFactory
import javax.inject.Inject
import org.eobjects.analyzer.result.renderer.HtmlRenderingFormat

@RendererBean(classOf[HtmlRenderingFormat])
class BooleanAnalyzerResultHtmlRenderer(rf: RendererFactory) extends Renderer[BooleanAnalyzerResult, HtmlFragment] {

  @Inject
  @Provided
  var rendererFactory: RendererFactory = rf;

  def this() = this(null)

  override def getPrecedence(result: BooleanAnalyzerResult) = RendererPrecedence.HIGH

  override def render(result: BooleanAnalyzerResult) = new BooleanAnalyzerHtmlFragment(rendererFactory, result);
}