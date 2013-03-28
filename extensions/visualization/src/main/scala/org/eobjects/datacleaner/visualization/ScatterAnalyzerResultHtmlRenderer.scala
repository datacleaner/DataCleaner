package org.eobjects.datacleaner.visualization

import org.eobjects.analyzer.beans.api.Renderer
import org.eobjects.analyzer.result.html.HtmlFragment
import org.eobjects.analyzer.beans.api.RendererBean
import org.eobjects.analyzer.result.renderer.HtmlRenderingFormat
import org.eobjects.analyzer.beans.api.RendererPrecedence
import org.eobjects.analyzer.result.html.HtmlRenderingContext
import org.eobjects.analyzer.result.html.SimpleHtmlFragment
import org.eobjects.analyzer.beans.api.RendererBean
import org.eobjects.analyzer.result.renderer.HtmlRenderingFormat

@RendererBean(classOf[HtmlRenderingFormat])
class ScatterAnalyzerResultHtmlRenderer extends Renderer[ScatterAnalyzerResult, HtmlFragment] {

  override def getPrecedence(result: ScatterAnalyzerResult) = RendererPrecedence.HIGH

  override def render(result: ScatterAnalyzerResult) = new HtmlFragment() {
    val frag = new SimpleHtmlFragment();

    override def initialize(context: HtmlRenderingContext) = {
      val elementId = context.createElementId()

      frag.addHeadElement(ScatterAnalyzerResuableChartHeadElement)
      frag.addHeadElement(new ScatterAnalyzerChartScriptHeadElement(result, elementId));

      val html =
        <div class="scatterAnalyzerDiv">
          <div class="scatterAnalyzerChart" id={ elementId }>
          </div>
        </div>

      frag.addBodyElement(html.toString)
    }

    override def getHeadElements() = frag.getHeadElements();

    override def getBodyElements() = frag.getBodyElements();
  }
}