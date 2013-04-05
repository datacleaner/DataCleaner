package org.eobjects.datacleaner.visualization

import org.eobjects.analyzer.result.html.HeadElement
import org.eobjects.analyzer.result.html.HtmlRenderingContext
import org.eobjects.metamodel.util.NumberComparator
import java.util.Date

class StackedAreaAnalyzerChartScriptHeadElement(result: StackedAreaAnalyzerResult, elementId: String) extends HeadElement {

  override def toHtml(context: HtmlRenderingContext): String = {
    val html = new StringBuilder

    html.append("""
  <script type="text/javascript">
  //<![CDATA[""");

    val measureColumns = result.getMeasureColumns
    val categories = result.getCategories

    // stack height will contain the offset of each stacked value and will be updated through the rendering of new points..
    val stackHeight: Array[Number] = categories.map(_ => 0.asInstanceOf[Number]).toArray;
    
    val colors = StackedAreaColors.getColors()

    for (i <- 0 to measureColumns.size - 1) {
      val measureColumn = measureColumns(i)
      html.append("\nvar d");
      html.append(i);
      html.append("={label:\"");
      html.append(context.escapeJson(measureColumn.getName()));
      if (i < colors.size) {
          html.append("\",color:\"#");
          html.append(StackedAreaColors.toHexString(colors(i)))
      }
      html.append("\",data:[");
      for (j <- 0 to categories.size - 1) {
        if (j != 0) {
          html.append(",");
        }
        val cat = categories(j)
        val measures = result.getMeasures(cat)
        val measure = measures(i)
        val pointJson = new StringBuilder()
        if (measure != null) {
          val offset = stackHeight(j)
          val measureValue = result.sum(offset, measure)
          html.append("[");
          html.append(toNumber(cat));
          html.append(",");
          html.append(measureValue)
          if (i != 0) {
            html.append(",");
            html.append(offset);
          }
          html.append("]");
          stackHeight.update(j, measureValue);
        }
      }

      html.append("]};\n");
    }

    html.append("\nvar data = [");
    for (i <- 0 to measureColumns.size - 1) {
      if (i != 0) {
        html.append(",");
      }
      html.append("d");
      html.append(i);
    }
    html.append("];");

    html.append("""
  draw_stacked_area_analyzer_chart('reselem_1', data, 2);
  //]]>
  </script>""");

    return html.toString
  }
  
  def toNumber(x: Any): Number = {
    if (x.isInstanceOf[Number]) {
      return x.asInstanceOf[Number]
    }
    if (x.isInstanceOf[Date]) {
      return x.asInstanceOf[Date].getTime()
    }
    throw new UnsupportedOperationException("Encountered unexpected non-number: " + x)
  }
}