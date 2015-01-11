package org.datacleaner.result.html
import scala.xml.XML

/**
 * A body element which wraps several other body elements in a div
 */
class CompositeBodyElement(cssClassName: String, children: Seq[BodyElement]) extends BodyElement {

  override def toHtml(context: HtmlRenderingContext): String = {
    val innerHtml = children.map(_.toHtml(context)).mkString("");
    return "<div class=\"" + cssClassName + "\">" + innerHtml + "</div>";
  }

}
