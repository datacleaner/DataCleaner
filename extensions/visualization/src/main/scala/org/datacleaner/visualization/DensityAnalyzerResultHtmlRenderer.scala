package org.datacleaner.visualization

import org.datacleaner.beans.api.Renderer
import org.datacleaner.result.html.HtmlFragment
import org.datacleaner.beans.api.RendererBean
import org.datacleaner.result.renderer.HtmlRenderingFormat
import org.datacleaner.beans.api.RendererPrecedence
import org.datacleaner.result.html.HtmlRenderingContext
import org.datacleaner.result.html.SimpleHtmlFragment
import org.datacleaner.beans.api.RendererBean
import org.datacleaner.result.renderer.HtmlRenderingFormat
import org.datacleaner.result.html.HtmlRenderer

@RendererBean(classOf[HtmlRenderingFormat])
class DensityAnalyzerResultHtmlRenderer extends HtmlRenderer[DensityAnalyzerResult] {

  override def handleFragment(frag: SimpleHtmlFragment, result: DensityAnalyzerResult, context: HtmlRenderingContext) {
    val elementId = context.createElementId()

    frag.addHeadElement(ScatterAnalyzerResuableChartHeadElement)
    frag.addHeadElement(new DensityAnalyzerChartScriptHeadElement(result, elementId));

    val html =
      <div class="densityAnalyzerDiv">
        <div class="scatterChart" id={ elementId }>
        </div>
      </div>

    frag.addBodyElement(html.toString)
  }
}
