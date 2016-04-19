package org.datacleaner.visualization

import org.datacleaner.api.Renderer
import org.datacleaner.api.RendererBean
import org.datacleaner.api.RendererPrecedence
import org.datacleaner.api.RendererBean
import org.datacleaner.result.html.HtmlFragment
import org.datacleaner.result.renderer.HtmlRenderingFormat
import org.datacleaner.result.html.HtmlRenderingContext
import org.datacleaner.result.html.SimpleHtmlFragment
import org.datacleaner.result.renderer.HtmlRenderingFormat
import org.datacleaner.result.html.HtmlRenderer


@RendererBean(classOf[HtmlRenderingFormat])
class DensityAnalyzerResultHtmlRendererRevised extends HtmlRenderer[DensityAnalyzerResultRevised] {

  override def handleFragment(frag: SimpleHtmlFragment, result: DensityAnalyzerResultRevised, context: HtmlRenderingContext) {
    val elementId = context.createElementId()

    frag.addHeadElement(ScatterAnalyzerResuableChartHeadElement)
    frag.addHeadElement(new DensityAnalyzerChartScriptHeadElementRevised(result, elementId));

    val html =
      <div class="densityAnalyzerDiv">
        <div class="scatterChart" id={ elementId }>
        </div>
      </div>

    frag.addBodyElement(html.toString)
  }
}
