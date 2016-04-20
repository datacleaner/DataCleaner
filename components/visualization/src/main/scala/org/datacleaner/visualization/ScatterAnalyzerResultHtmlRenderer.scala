package org.datacleaner.visualization

import org.datacleaner.api.RendererBean
import org.datacleaner.result.html.{HtmlRenderer, HtmlRenderingContext, SimpleHtmlFragment}
import org.datacleaner.result.renderer.HtmlRenderingFormat

@RendererBean(classOf[HtmlRenderingFormat])
class ScatterAnalyzerResultHtmlRenderer extends HtmlRenderer[IScatterAnalyzerResult] {

  override def handleFragment(frag: SimpleHtmlFragment, result: IScatterAnalyzerResult, context: HtmlRenderingContext) {
    val elementId = context.createElementId()

    frag.addHeadElement(ScatterAnalyzerResuableChartHeadElement)
    frag.addHeadElement(new ScatterAnalyzerChartScriptHeadElement(result, elementId));

    val html =
      <div class="scatterAnalyzerDiv">
        <div class="scatterChart" id={ elementId }>
        </div>
      </div>

    frag.addBodyElement(html.toString)
  }
}
