package org.datacleaner.beans.transform
import junit.framework.TestCase
import org.scalatest.junit.AssertionsForJUnit
import org.datacleaner.data.MockInputColumn
import org.datacleaner.data.MockInputRow
import org.junit.Assert._
import org.junit.Test

class StringLengthTransformerTest extends AssertionsForJUnit {

  @Test
  def testTransform() {
    val col: MockInputColumn[String] = new MockInputColumn[String]("foobar", classOf[String]);

    val transformer: StringLengthTransformer = new StringLengthTransformer(col);

    assertEquals(1, transformer.getOutputColumns().getColumnCount());

    var result = transformer.transform(new MockInputRow().put(col, "hello"))
    assertEquals(1, result.length);
    assertEquals(5, result(0));

    result = transformer.transform(new MockInputRow().put(col, null));
    assertEquals(1, result.length);
    assertEquals(null, result(0));

    result = transformer.transform(new MockInputRow().put(col, ""));
    assertEquals(1, result.length);
    assertEquals(0, result(0));
  }
}
