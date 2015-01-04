package org.eobjects.analyzer.beans.transform
import java.util.regex.Pattern

import org.eobjects.analyzer.data.MockInputColumn
import org.eobjects.analyzer.data.MockInputRow
import org.junit.Assert.assertEquals
import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit

class PlainSearchReplaceTransformerTest extends AssertionsForJUnit {

  @Test
  def testTransformMatchRegion() {
    val col: MockInputColumn[String] = new MockInputColumn[String]("foobar", classOf[String]);

    val transformer = new PlainSearchReplaceTransformer();
    transformer.valueColumn = col;
    transformer.searchString = "foo";
    transformer.replacementString = "bar";

    assertEquals("OutputColumns[foobar (replaced 'foo')]", transformer.getOutputColumns().toString());

    assertEquals("null", transformer.transform(new MockInputRow().put(col, null)).mkString(","));
    assertEquals("", transformer.transform(new MockInputRow().put(col, "")).mkString(","));
    assertEquals("bar baz bar", transformer.transform(new MockInputRow().put(col, "bar baz bar")).mkString(","));

    assertEquals("bar bar", transformer.transform(new MockInputRow().put(col, "foo foo")).mkString(","));
    assertEquals("bar Hello there world bar", transformer.transform(new MockInputRow().put(col, "foo Hello there world foo")).mkString(","));
  }

  @Test
  def testValidateReplaceTokenWithinSearchToken() {
    val col: MockInputColumn[String] = new MockInputColumn[String]("foobar", classOf[String]);

    val transformer = new PlainSearchReplaceTransformer();
    transformer.valueColumn = col;
    transformer.searchString = "foo";
    transformer.replacementString = "fooo";

    try {
      transformer.validate();
      fail("Exception expected");
    } catch {
      case e: IllegalArgumentException => assertEquals("Replacement string cannot contain the search string (implies an infinite replacement loop)", e.getMessage());
      case e: Throwable => fail("Unexpected exception: " + e);
    }
  }

  @Test
  def testTransformMatchEntireString() {
    val col: MockInputColumn[String] = new MockInputColumn[String]("foobar", classOf[String]);

    val transformer = new PlainSearchReplaceTransformer();
    transformer.valueColumn = col;
    transformer.searchString = "foo";
    transformer.replacementString = "bar";
    transformer.replaceEntireString = true

    assertEquals("OutputColumns[foobar (replaced 'foo')]", transformer.getOutputColumns().toString());

    assertEquals("null", transformer.transform(new MockInputRow().put(col, null)).mkString(","));
    assertEquals("", transformer.transform(new MockInputRow().put(col, "")).mkString(","));
    assertEquals("bar baz bar", transformer.transform(new MockInputRow().put(col, "bar baz bar")).mkString(","));

    assertEquals("bar", transformer.transform(new MockInputRow().put(col, "foo foo")).mkString(","));
    assertEquals("bar", transformer.transform(new MockInputRow().put(col, "hello foo")).mkString(","));
  }
}