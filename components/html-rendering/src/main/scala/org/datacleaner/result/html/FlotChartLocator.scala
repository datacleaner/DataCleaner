package org.datacleaner.result.html

/**
 * Object responsible for locating the javascript URLs of Flot charts (used for most of the charts/graphs in HTML rendered analyzer results).
 */
object FlotChartLocator {

  val SYSTEM_PROPERTY_FLOT_HOME = "org.datacleaner.flot.home"
  val SYSTEM_PROPERTY_FLOT_HOME_DEPRECATED = "org.datacleaner.valuedist.flotLibraryLocation"
  val DEFAULT_FLOT_HOME = "http://cdnjs.cloudflare.com/ajax/libs/flot/0.7"

  /**
   * Gets the URL for the base flot library, typically named as "jquery.flot.min.js"
   */
  def getFlotBaseUrl: String = getFlotHome() + "/jquery.flot.min.js";

  /**
   * Gets the URL for the flot plugin for pie charts, typically named as "jquery.flot.pie.min.js"
   */
  def getFlotPieUrl: String = getFlotHome() + "/jquery.flot.pie.min.js";

  /**
   * Gets the URL for the flot plugin for selecting parts of the plot, typically named as "jquery.flot.selection.min.js"
   */
  def getFlotSelectionUrl: String = getFlotHome() + "/jquery.flot.selection.min.js";
  
  /**
   * Gets the URL for the flot plugin for additional point symbols, typically named as "jquery.flot.symbol.min.js"
   */
  def getFlotSymbolUrl: String = getFlotHome() + "/jquery.flot.symbol.min.js";

  /**
   * Gets the URL for the flot plugin for "fill between" effect, typically named as "jquery.flot.fillbetween.min.js"
   */
  def getFlotFillBetweenUrl: String = getFlotHome() + "/jquery.flot.fillbetween.min.js";

  /**
   * Gets the URL for the flot plugin for navigating (zoom/pan), typically named as "jquery.flot.navigate.min.js"
   */
  def getFlotNavigateUrl: String = getFlotHome() + "/jquery.flot.navigate.min.js";

  /**
   * Gets the URL for the flot plugin for automatically resizing charts, typically named as "jquery.flot.resize.min.js"
   */
  def getFlotResizeUrl: String = getFlotHome() + "/jquery.flot.resize.min.js";

  /**
   * Gets the URL for the flot plugin for stacked charts, typically named as "jquery.flot.stack.min.js"
   */
  def getFlotStackUrl: String = getFlotHome() + "/jquery.flot.stack.min.js";

  /**
   * Gets the URL for the flot plugin for threshold effect, typically named as "jquery.flot.threshold.min.js"
   */
  def getFlotThresholdUrl: String = getFlotHome() + "/jquery.flot.threshold.min.js";

  /**
   * Gets the URL for the flot plugin for plotting categories instead of numbers on an axis, typically named as "jquery.flot.categories.js"
   */
  def getFlotCategoriesUrl: String = {
    val flotHome = getFlotHome
    if (DEFAULT_FLOT_HOME.equals(flotHome)) {
      // TODO: Not hosted via the default CDN - we only have this non-secure HTTP link to provide
      return "http://www.flotcharts.org/flot/jquery.flot.categories.js";
    }
    return flotHome + "/jquery.flot.categories.min.js";
  }

  /**
   * Gets the home folder of all flot javascript files
   */
  protected def getFlotHome(): String = {
    return getSystemProperty(SYSTEM_PROPERTY_FLOT_HOME) match {
      case Some(str) => str;
      case None => getSystemProperty(SYSTEM_PROPERTY_FLOT_HOME_DEPRECATED) match {
        case Some(str) => str
        case None => DEFAULT_FLOT_HOME
      }
    }
  }

  /**
   * Sets the home folder of all flot javascript files
   */
  def setFlotHome(flotHome: String) {
    if (flotHome == null || flotHome.trim().isEmpty()) {
      System.clearProperty(SYSTEM_PROPERTY_FLOT_HOME)
      System.clearProperty(SYSTEM_PROPERTY_FLOT_HOME_DEPRECATED)
    } else {
      val propValue = if (flotHome.endsWith("/")) flotHome.substring(0, flotHome.length() - 1) else flotHome
      System.setProperty(SYSTEM_PROPERTY_FLOT_HOME, propValue)
    }
  }

  /**
   * Helper method to get a system property as an option
   */
  private def getSystemProperty(systemProp: String): Option[String] = {
    val propValue = System.getProperty(systemProp);
    if (propValue == null || propValue.trim().isEmpty()) {
      return None
    }
    return Some(propValue);
  }
}
