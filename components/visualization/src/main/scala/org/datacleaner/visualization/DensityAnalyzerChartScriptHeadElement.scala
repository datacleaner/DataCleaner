package org.datacleaner.visualization

import java.awt.Color

import org.datacleaner.result.html.{HeadElement, HtmlRenderingContext}

import scala.collection.JavaConversions._
import scala.collection.mutable.{ListBuffer, Map}
/**
 * Head element that writes a script specific to the rendering of a single result
 */
class DensityAnalyzerChartScriptHeadElement(result: IDensityAnalyzerResult, elementId: String) extends HeadElement {

  val series: Map[String, ListBuffer[(Int, Int)]] = Map[String, ListBuffer[(Int, Int)]]().withDefault(rgbHex => {
    val list: ListBuffer[(Int, Int)] = new ListBuffer[(Int, Int)]()
    series.put(rgbHex, list)
    list
  });

  override def toHtml(context: HtmlRenderingContext): String = {

    val annotations = result.getRowAnnotations

    val maxRowsAnnotation = annotations.values.reduce((a, b) => if (a.getRowCount() > b.getRowCount()) a else b)
    val maxRows = maxRowsAnnotation.getRowCount()

    val paintScale = DensityAnalyzerColors.getPaintScale(maxRows);

    annotations.foreach(entry => {
      val point = entry._1
      val z = entry._2.getRowCount()

      val paint = paintScale.getPaint(z)
      val color = paint.asInstanceOf[Color]
      val rgbHex = DensityAnalyzerColors.toHexString(color)
      val list = series(rgbHex)
      list += new Tuple2(point.getLeft, point.getRight)
    })

    return """<script type="text/javascript">
    //<![CDATA[
    var data = [
        """ +
      series.map(entry => {
        val rgbHex = entry._1
        val list = entry._2;
        """{
        data: [""" + list.map(coor => "[" + coor._1 + "," + coor._2 + "]").mkString(",") + """],
        color: "#""" + rgbHex + """"
              }"""
      }).mkString(",") + """
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
