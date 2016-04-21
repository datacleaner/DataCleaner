package org.datacleaner.visualization

import org.datacleaner.api.RendererBean
import org.datacleaner.result.html.{HtmlRenderer, HtmlRenderingContext, SimpleHtmlFragment}
import org.datacleaner.result.renderer.HtmlRenderingFormat

@RendererBean(classOf[HtmlRenderingFormat])
class DensityAnalyzerResultHtmlRenderer extends HtmlRenderer[IDensityAnalyzerResult] {

  override def handleFragment(frag: SimpleHtmlFragment, result: IDensityAnalyzerResult, context: HtmlRenderingContext) {
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
