package org.eobjects.datacleaner.visualization

import org.eobjects.analyzer.result.html.HtmlRenderer
import org.eobjects.analyzer.result.html.HtmlRenderingContext
import org.eobjects.analyzer.result.html.SimpleHtmlFragment
import org.eobjects.analyzer.beans.api.RendererBean
import org.eobjects.analyzer.result.renderer.HtmlRenderingFormat

@RendererBean(classOf[HtmlRenderingFormat])
class StackedAreaAnalyzerResultHtmlRenderer extends HtmlRenderer[StackedAreaAnalyzerResult] {

  override def handleFragment(frag: SimpleHtmlFragment, result: StackedAreaAnalyzerResult, context: HtmlRenderingContext) {
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