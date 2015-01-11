package org.datacleaner.beans.transform
import java.util.regex.Pattern

import org.datacleaner.data.MockInputColumn
import org.datacleaner.data.MockInputRow
import org.junit.Assert.assertEquals
import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit

class RegexSearchReplaceTransformerTest extends AssertionsForJUnit {

  @Test
  def testTransform() {
    val col: MockInputColumn[String] = new MockInputColumn[String]("foobar", classOf[String]);

    val transformer = new RegexSearchReplaceTransformer();
    transformer.valueColumn = col;
    transformer.searchPattern = Pattern.compile("foo ([\\w ]+) foo");
    transformer.replacementPattern = Pattern.compile("bar $1 bar");

    assertEquals("OutputColumns[foobar (replaced 'foo ([\\w ]+) foo')]", transformer.getOutputColumns().toString());

    assertEquals("null", transformer.transform(new MockInputRow().put(col, null)).mkString(","));
    assertEquals("", transformer.transform(new MockInputRow().put(col, "")).mkString(","));
    assertEquals("foo foo", transformer.transform(new MockInputRow().put(col, "foo foo")).mkString(","));

    assertEquals("bar baz bar", transformer.transform(new MockInputRow().put(col, "foo baz foo")).mkString(","));
    assertEquals("bar Hello there world bar", transformer.transform(new MockInputRow().put(col, "foo Hello there world foo")).mkString(","));
  }
}
