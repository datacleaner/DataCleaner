package org.datacleaner.visualization

import org.datacleaner.api.RendererBean
import org.datacleaner.result.html.{HtmlRenderer, HtmlRenderingContext, SimpleHtmlFragment}
import org.datacleaner.result.renderer.HtmlRenderingFormat

@RendererBean(classOf[HtmlRenderingFormat])
class StackedAreaAnalyzerResultHtmlRenderer extends HtmlRenderer[IStackedAreaAnalyzerResult] {

  override def handleFragment(frag: SimpleHtmlFragment, result: IStackedAreaAnalyzerResult, context: HtmlRenderingContext) {
    val elementId = context.createElementId()

    frag.addHeadElement(StackedAreaAnalyzerResuableChartHeadElement)
    frag.addHeadElement(new StackedAreaAnalyzerChartScriptHeadElement(result, elementId));
    
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
