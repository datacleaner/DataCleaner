package org.datacleaner.visualization

import org.datacleaner.result.html.HtmlRenderingContext
import org.datacleaner.result.html.HeadElement
import collection.JavaConversions._

/**
 * Head element that writes a script specific to the rendering of a single result
 */
class ScatterAnalyzerChartScriptHeadElementRevised(result: ScatterAnalyzerResultRevised, elementId: String) extends HeadElement {

  override def toHtml(context: HtmlRenderingContext): String = {

    return """<script type="text/javascript">
    //<![CDATA[
    var data = [
        """ +
      result.groups.map(group => """{
        data: [""" + group.getCoordinates().map(coor => "[" +  coor.getX() + "," +  coor.getY() + "]").mkString(",") + """],
        label: """" + group.name + """"
              }""").mkString(",") + """
    ];
    wait_for_script_load('jQuery', function() {
      $(function(){
        draw_scatter_chart('""" + elementId + """', data, 2);
      });
    });
    //]]>
</script>
"""
  }
}
