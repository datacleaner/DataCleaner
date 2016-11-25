package org.datacleaner.beans.valuedist
import org.datacleaner.result.html.{HeadElement, HtmlRenderingContext}
import org.datacleaner.result.{ValueCountingAnalyzerResult, ValueFrequency}
import org.datacleaner.util.LabelUtils

class ValueDistributionChartScriptHeadElement(result: ValueCountingAnalyzerResult, valueCounts: Iterable[ValueFrequency], chartElementId: String) extends HeadElement {

  override def toHtml(context: HtmlRenderingContext): String = {
    // will be used to plot the y-axis value. Descending/negative because we want them to go from top to bottom.
    var negativeIndex = 0;
    val dataId = "data" + chartElementId;

    return """<script type="text/javascript">
    //<![CDATA[
    var """ + dataId + """ = [
        """ +
      valueCounts.map(vc => {
        val color = getColor(vc);
        negativeIndex = negativeIndex - 1;
        "{label:\"" + escapeLabel(context, vc.getName()) + "\", " +
          "data:[[" + vc.getCount() + "," + negativeIndex + "]]" +
          { if (color == null) "" else ", color:\"" + color + "\"" } +
          "}" + "";
      }).mkString(",") + """
    ];
    require(['jquery'], function ($) {
      $(function() {
        draw_value_distribution_bar('""" + chartElementId + """', """ + dataId + """, 2);
      });
    });
    //]]>
</script>
"""
  }

  def getColor(vc: ValueFrequency): String = {
    val name = vc.getName();
    name match {
      case LabelUtils.UNIQUE_LABEL => return "#ccc";
      case LabelUtils.BLANK_LABEL => return "#eee";
      case LabelUtils.UNEXPECTED_LABEL => return "#333";
      case LabelUtils.NULL_LABEL => return "#111";
      case _ => name.toLowerCase() match {
        case "red" | "blue" | "green" | "yellow" | "orange" | "black" => return name.toLowerCase();
        case "not_processed" => return "#333";
        case "failure" => return "#000";
        case _ => return null;
      }
    }
  }

  def escapeLabel(context: HtmlRenderingContext, name: String): String = {
    val escaped = context.escapeJson(name)
    return escaped.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
  }
}
