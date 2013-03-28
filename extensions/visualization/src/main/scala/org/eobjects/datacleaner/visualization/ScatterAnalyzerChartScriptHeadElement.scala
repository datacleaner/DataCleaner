package org.eobjects.datacleaner.visualization

import org.eobjects.analyzer.result.html.HtmlRenderingContext
import org.eobjects.analyzer.result.html.HeadElement

/**
 * Head element that writes a script specific to the rendering of a single result
 */
class ScatterAnalyzerChartScriptHeadElement(result: ScatterAnalyzerResult, elementId: String) extends HeadElement {

  override def toHtml(context: HtmlRenderingContext): String = {

    return """<script type="text/javascript">
    //<![CDATA[
    var data = [
        """ +
      result.groups.map(group => """{
        data: [""" + group.getCoordinates().map(coor => "[" + coor._1 + "," + coor._2 + "]").mkString(",") + """],
        label: """" + group.name + """"
              }""").mkString(",") + """
    ];
    draw_scatter_analyzer_chart('""" + elementId + """', data, 2);
    //]]>
</script>
"""
  }
}