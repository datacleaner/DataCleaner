package org.eobjects.datacleaner.visualization

import org.eobjects.datacleaner.util.WidgetUtils
import java.awt.Color
import org.jfree.chart.renderer.LookupPaintScale
import org.jfree.chart.renderer.PaintScale

object DensityAnalyzerColors {
  
  def toHexString(c: Color): String = {
    var rgb = c.getRGB();
    return Integer.toHexString(rgb).substring(2)
  }
  
  def getPaintScale(maxZvalue : Int): PaintScale = {
    val minimumColor = WidgetUtils.BG_COLOR_BRIGHT
    val paintScale = new LookupPaintScale(0.0, 100.0, minimumColor)
    
    val colors = getColors()
    val zIncrement = scala.math.max(1.0, maxZvalue * 1.0 / colors.length)
    for (i <- 0 to colors.length - 1) {
      val color = colors(i)
      val zValue = zIncrement * (i + 0.5)
      paintScale.add(zValue, color);
    }
    
    return paintScale;
  }

  /**
   * Builds a list of colors to use in the density plot
   */
  def getColors(): Array[Color] = Array(
    WidgetUtils.BG_COLOR_ORANGE_BRIGHT,
    WidgetUtils.BG_COLOR_ORANGE_MEDIUM,
    WidgetUtils.BG_COLOR_ORANGE_DARK,
    WidgetUtils.BG_COLOR_BLUE_BRIGHT,
    WidgetUtils.BG_COLOR_BLUE_MEDIUM,
    WidgetUtils.BG_COLOR_BLUE_DARK,
    WidgetUtils.BG_COLOR_DARKEST);
}