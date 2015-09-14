package org.datacleaner.beans.valuedist
import scala.collection.JavaConversions._
import org.datacleaner.result.html.BodyElement
import org.datacleaner.result.html.DrillToDetailsBodyElement
import org.datacleaner.result.html.HeadElement
import org.datacleaner.result.html.HtmlFragment
import org.datacleaner.result.html.HtmlRenderingContext
import org.datacleaner.result.html.SimpleHtmlFragment
import org.datacleaner.result.renderer.RendererFactory
import org.datacleaner.result.GroupedValueCountingAnalyzerResult
import org.datacleaner.result.ListResult
import org.datacleaner.result.ValueCountingAnalyzerResult
import org.datacleaner.util.LabelUtils
import org.datacleaner.result.ListResult
import org.datacleaner.result.ListResult
import org.datacleaner.result.SingleValueFrequency
import org.datacleaner.result.ValueFrequency

class ValueDistributionHtmlFragment(result: ValueCountingAnalyzerResult, rendererFactory: RendererFactory) extends HtmlFragment {

  val frag = new SimpleHtmlFragment();

  override def initialize(context: HtmlRenderingContext) {
    frag.addHeadElement(ValueDistributionReusableScriptHeadElement)

    val html = <div class="valueDistributionResultContainer">
                 {
                   if (result.isInstanceOf[GroupedValueCountingAnalyzerResult]) {
                     val groupedResult = result.asInstanceOf[GroupedValueCountingAnalyzerResult];
                     groupedResult.getGroupResults().map(r => {
                       renderResult(r, context, true)
                     })
                   } else {
                     renderResult(result, context, false);
                   }
                 }
               </div>;

    frag.addBodyElement(html.toString())
  }

  override def getHeadElements(): java.util.List[HeadElement] = {
    return frag.getHeadElements();
  }

  override def getBodyElements(): java.util.List[BodyElement] = {
    return frag.getBodyElements();
  }

  def renderResult(result: ValueCountingAnalyzerResult, context: HtmlRenderingContext, group: Boolean): scala.xml.Node = {
    val chartElementId: String = context.createElementId();

    val valueCounts = result.getValueCounts();

    frag.addHeadElement(new ValueDistributionChartScriptHeadElement(result, valueCounts, chartElementId));

    val numBars = valueCounts.size();
    val barHeight = if (numBars < 20) 40 else if (numBars < 30) 30 else 20
    val height = numBars * barHeight;
    val style = "height: " + height + "px;"

    return <div class="valueDistributionGroupPanel">
             {
               if (group && result.getName() != null) {
                 <h3>Group: { result.getName() }</h3>
               }
             }
             {
               <div class="valueDistributionChart" style={ style } id={ chartElementId }>
               </div>
             }
             {
               if (!valueCounts.isEmpty()) {
                 <table class="valueDistributionValueTable">
                   {
                     valueCounts.iterator().map(valueFreq => {
                       <tr><td>{ valueFreq.getName() }</td><td>{ getCount(result, valueFreq, context) }</td></tr>
                     })
                   }
                 </table>
               }
             }
             <table class="valueDistributionSummaryTable">
               <tr><td>Total count</td><td>{ result.getTotalCount() }</td></tr>
               {
                 if (result.getDistinctCount() != null) {
                   <tr><td>Distinct count</td><td>{ result.getDistinctCount() }</td></tr>
                 }
               }
             </table>
           </div>;
  }

  def getCount(result: ValueCountingAnalyzerResult, valueFreq: ValueFrequency, context: HtmlRenderingContext): scala.xml.Node = {
    val count = valueFreq.getCount();
    if (count == 0) {
      return <span>{ count }</span>;
    }

    if (valueFreq.isComposite()) {
      if (LabelUtils.UNIQUE_LABEL.equals(valueFreq.getName())) {
        val uniqueValues = result.getUniqueValues()
        if (uniqueValues != null && !uniqueValues.isEmpty()) {
          val elementId = context.createElementId();
          val listResult = new ListResult(uniqueValues.toList);

          val bodyElement = new DrillToDetailsBodyElement(elementId, rendererFactory, listResult);
          frag.addBodyElement(bodyElement);

          val invocation = bodyElement.toJavaScriptInvocation()

          return <a class="drillToDetailsLink" onclick={ invocation } href="#">{ count }</a>
        }
      }

      return <span>{ count }</span>;
    }

    var value = valueFreq.getValue();

    val annotatedRowsResult = result.getAnnotatedRowsForValue(value);
    if (annotatedRowsResult == null || annotatedRowsResult.getAnnotatedRowCount() == 0) {
      return <span>{ count }</span>;
    }

    val elementId = context.createElementId();

    val bodyElement = new DrillToDetailsBodyElement(elementId, rendererFactory, annotatedRowsResult);
    frag.addBodyElement(bodyElement);

    val invocation = bodyElement.toJavaScriptInvocation()

    return <a class="drillToDetailsLink" onclick={ invocation } href="#">{ count }</a>
  }
}
