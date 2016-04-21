package org.datacleaner.visualization

import org.datacleaner.data.{MockInputColumn, MockInputRow}
import org.datacleaner.storage.RowAnnotations
import org.junit.{Assert, Test}
import org.scalatest.junit.AssertionsForJUnit

import scala.collection.JavaConverters._
class ScatterAnalyzerTest extends AssertionsForJUnit {

  @Test
  def testMapDefaultValue = {
    val analyzer = new ScatterAnalyzer()
    analyzer.rowAnnotationFactory = RowAnnotations.getInMemoryFactory()
    analyzer.variable1 = new MockInputColumn("foo");
    analyzer.variable2 = new MockInputColumn("bar");

    analyzer.run(new MockInputRow().put(analyzer.variable1, 1).put(analyzer.variable2, 1), 1);
    analyzer.run(new MockInputRow().put(analyzer.variable1, 1).put(analyzer.variable2, 2), 1);
    analyzer.run(new MockInputRow().put(analyzer.variable1, 1).put(analyzer.variable2, 1), 1);
    
    val group = analyzer.getResult.getGroups.asScala.head

    Assert.assertEquals(2, group.getRowAnnotations.size());
    Assert.assertEquals(2, group.getRowAnnotation(1,1).getRowCount);
    Assert.assertEquals(1, group.getRowAnnotation(1,2).getRowCount);
  }
}
