package org.eobjects.analyzer.beans
import org.eobjects.analyzer.result.html.HtmlFragment
import org.eobjects.analyzer.result.html.HtmlRenderingContext
import org.eobjects.analyzer.result.html.SimpleHtmlFragment
import org.eobjects.analyzer.result.renderer.RendererFactory
import org.eobjects.analyzer.result.renderer.CrosstabHtmlRenderer
import scala.collection.JavaConversions._
import org.eobjects.analyzer.result.html.BodyElement
import org.eobjects.analyzer.result.html.CompositeBodyElement

class BooleanAnalyzerHtmlFragment(rendererFactory: RendererFactory, result: BooleanAnalyzerResult) extends HtmlFragment {

  val frag = new SimpleHtmlFragment();

  override def initialize(context: HtmlRenderingContext) {
    // render the two crosstabs in this result
    val crosstabRenderer = new CrosstabHtmlRenderer(rendererFactory)

    val columnStatisticsCrosstab = result.getColumnStatisticsCrosstab()
    val columnStatisticsHtmlFragment = if (columnStatisticsCrosstab == null) null else crosstabRenderer.render(columnStatisticsCrosstab)

    val valueCombinationCrosstab = result.getValueCombinationCrosstab()
    val valueCombinationHtmlFragment = if (valueCombinationCrosstab == null) null else crosstabRenderer.render(valueCombinationCrosstab)

    // add all head elements to the html fragment
    if (columnStatisticsHtmlFragment != null) {
      columnStatisticsHtmlFragment.initialize(context)
      columnStatisticsHtmlFragment.getHeadElements().foreach(frag.addHeadElement(_))
    }
    if (valueCombinationHtmlFragment != null) {
      valueCombinationHtmlFragment.initialize(context)
      valueCombinationHtmlFragment.getHeadElements().foreach(frag.addHeadElement(_))
    }

    // make a composite body element
    var bodyElements = Seq[BodyElement]();
    if (columnStatisticsHtmlFragment != null) {
      bodyElements = bodyElements ++ columnStatisticsHtmlFragment.getBodyElements()
    }
    if (valueCombinationHtmlFragment != null) {
      bodyElements = bodyElements ++ valueCombinationHtmlFragment.getBodyElements()
    }
    val composite = new CompositeBodyElement("booleanAnalyzerResult", bodyElements);
    frag.addBodyElement(composite);
  }
  
  override def getHeadElements() = frag.getHeadElements();
  
  override def getBodyElements() = frag.getBodyElements();
}