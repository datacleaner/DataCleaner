package org.datacleaner.visualization

import org.datacleaner.api.RendererBean
import org.datacleaner.result.html.HtmlRenderer
import org.datacleaner.result.html.HtmlRenderingContext
import org.datacleaner.result.html.SimpleHtmlFragment
import org.datacleaner.result.renderer.HtmlRenderingFormat


@RendererBean(classOf[HtmlRenderingFormat])
class StackedAreaAnalyzerResultHtmlRendererRevised extends HtmlRenderer[StackedAreaAnalyzerResultRevised] {

  override def handleFragment(frag: SimpleHtmlFragment, result: StackedAreaAnalyzerResultRevised, context: HtmlRenderingContext) {
    val elementId = context.createElementId()

    frag.addHeadElement(StackedAreaAnalyzerResuableChartHeadElement)
    frag.addHeadElement(new StackedAreaAnalyzerChartScriptHeadElementRevised(result, elementId));
    
    val width: Int = scala.math.max(result.getCategoryCount * 4, 700);
    
    val style = "min-width: " + width + "px";

    val html =
      <div class="stackedAreaAnalyzerDiv">
        <div class="stackedAreaAnalyzerChart" id={ elementId } style={style}>
        </div>
      </div>

    frag.addBodyElement(html.toString)
  }
}
