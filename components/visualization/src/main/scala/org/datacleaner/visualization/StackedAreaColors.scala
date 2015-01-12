package org.datacleaner.visualization

import java.awt.Color
import org.datacleaner.util.WidgetUtils

object StackedAreaColors {

  def getColors(): List[Color] = List(
    WidgetUtils.ADDITIONAL_COLOR_GREEN_BRIGHT,
    WidgetUtils.ADDITIONAL_COLOR_RED_BRIGHT,
    WidgetUtils.BG_COLOR_BLUE_BRIGHT,
    WidgetUtils.BG_COLOR_ORANGE_BRIGHT,
    WidgetUtils.ADDITIONAL_COLOR_PURPLE_BRIGHT,
    WidgetUtils.ADDITIONAL_COLOR_CYAN_BRIGHT)

  def toHexString(c: Color): String = {
    var rgb = c.getRGB();
    return Integer.toHexString(rgb).substring(2)
  }
}
