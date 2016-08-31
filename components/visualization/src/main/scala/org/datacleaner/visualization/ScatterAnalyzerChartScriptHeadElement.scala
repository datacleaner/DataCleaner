package org.datacleaner.visualization

import org.datacleaner.result.html.{HeadElement, HtmlRenderingContext}

import scala.collection.JavaConverters._
/**
 * Head element that writes a script specific to the rendering of a single result
 */
class ScatterAnalyzerChartScriptHeadElement(result: IScatterAnalyzerResult, elementId: String) extends HeadElement {

  override def toHtml(context: HtmlRenderingContext): String = {
    val dataId = "data" + elementId;

    return """<script type="text/javascript">
    //<![CDATA[
    var """ + dataId + """ = [
        """ +
      result.getGroups.asScala.map(group => """{
        data: [""" + group.getCoordinates.asScala.map(coor => "[" + coor.getLeft + "," + coor.getRight + "]").mkString(",") + """],
        label: """" + group.getName + """"
              }""").mkString(",") + """
    ];
    require(['jquery'], function ($) {
      $(function() {
        draw_scatter_chart('""" + elementId + """', """ + dataId + """, 2);
      });
    });
    //]]>
</script>
"""
  }
}
