package org.datacleaner.visualization

import org.datacleaner.result.html.HtmlRenderingContext
import org.datacleaner.result.html.HeadElement
import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer
import java.awt.Color
import collection.JavaConversions._

/**
 * Head element that writes a script specific to the rendering of a single result
 */
class DensityAnalyzerChartScriptHeadElementRevised(result: DensityAnalyzerResultRevised, elementId: String) extends HeadElement {

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
      val key = entry._1
      val z = entry._2.getRowCount()
      val x = key.getX().toInt
      val y = key.getY().toInt
      val point = (x,y)
      val paint = paintScale.getPaint(z)
      val color = paint.asInstanceOf[Color]
      val rgbHex = DensityAnalyzerColors.toHexString(color)
      val list = series(rgbHex)
      list += point
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
