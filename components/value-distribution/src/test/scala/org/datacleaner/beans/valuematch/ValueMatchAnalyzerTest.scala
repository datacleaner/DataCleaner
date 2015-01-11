package org.datacleaner.beans.valuematch
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import org.datacleaner.storage.InMemoryRowAnnotationFactory
import org.datacleaner.data.MockInputColumn
import scala.collection.JavaConversions._
import org.datacleaner.data.MockInputRow
import org.junit.Assert

class ValueMatchAnalyzerTest extends AssertionsForJUnit {

  @Test
  def testScenario = {
    val annotationFactory = new InMemoryRowAnnotationFactory()

    val analyzer = new ValueMatchAnalyzer();
    analyzer._rowAnnotationFactory = annotationFactory;
    analyzer._nonMatchingValuesAnnotation = annotationFactory.createAnnotation();
    analyzer._nullAnnotation = annotationFactory.createAnnotation();

    val col = new MockInputColumn("foo");
    analyzer.column = col;
    analyzer.expectedValues = Array("Foo", "Bar", "Baz")
    analyzer.caseSensitiveMatching = false
    analyzer.whiteSpaceSensitiveMatching = false;

    analyzer.init();

    analyzer.run(new MockInputRow().put(col, "FOO"), 1);
    analyzer.run(new MockInputRow().put(col, "FOO  "), 1);
    analyzer.run(new MockInputRow().put(col, " foo \t "), 1);
    analyzer.run(new MockInputRow().put(col, " Baaar "), 4);
    analyzer.run(new MockInputRow().put(col, " Bar "), 1);
    analyzer.run(new MockInputRow().put(col, null), 2);

    var result = analyzer.getResult();

    Assert.assertEquals("Foo,Bar,Baz", result.getValueCount().getParameterSuggestions().mkString(","));
    Assert.assertEquals(3, result.getCount("Foo"));
    Assert.assertEquals(1, result.getCount("Bar"));
    Assert.assertEquals(0, result.getCount("Baz"));
    Assert.assertEquals(null, result.getCount("baaaaz"));
    Assert.assertEquals(4, result.getUnexpectedValueCount());
    Assert.assertEquals(2, result.getNullCount());

    val annotatedRowsResult = result.getAnnotatedRowsForValue("Foo");

    val rows = annotatedRowsResult.getRows();
    
    Assert.assertEquals("FOO", rows(0).getValue(col).toString());
    Assert.assertEquals("FOO  ", rows(1).getValue(col).toString());
    Assert.assertEquals(" foo \t ", rows(2).getValue(col).toString());
  }
}
