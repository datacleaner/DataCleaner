package org.datacleaner.result.html
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import org.junit.Assert

class CompositeBodyElementTest extends AssertionsForJUnit {

  @Test
  def testXmlComposition = {
    val elem1 = new SimpleBodyElement("<p>hello</p>")
    val elem2 = new SimpleBodyElement("<p>world</p>")

    Assert.assertEquals("<div class=\"myClass\"><p>hello</p><p>world</p></div>", new CompositeBodyElement("myClass", Seq(elem1, elem2)).toHtml(new DefaultHtmlRenderingContext()));
  }
}
