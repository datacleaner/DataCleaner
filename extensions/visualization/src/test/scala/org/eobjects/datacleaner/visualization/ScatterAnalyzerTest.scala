package org.eobjects.datacleaner.visualization

import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import org.junit.Assert
import org.junit.Test
import org.eobjects.analyzer.data.MockInputColumn
import org.eobjects.analyzer.data.MockInputRow
import org.eobjects.analyzer.storage.InMemoryRowAnnotationFactory

class ScatterAnalyzerTest extends AssertionsForJUnit {

  @Test
  def testMapDefaultValue = {
    val analyzer = new ScatterAnalyzer()
    analyzer.rowAnnotationFactory = new InMemoryRowAnnotationFactory()
    analyzer.variable1 = new MockInputColumn("foo");
    analyzer.variable2 = new MockInputColumn("bar");

    analyzer.run(new MockInputRow().put(analyzer.variable1, 1).put(analyzer.variable2, 1), 1);
    analyzer.run(new MockInputRow().put(analyzer.variable1, 1).put(analyzer.variable2, 2), 1);
    analyzer.run(new MockInputRow().put(analyzer.variable1, 1).put(analyzer.variable2, 1), 1);
    
    val group = analyzer.getResult.groups.head
    val str = group.annotations.map(entry => (entry._1, entry._2.getRowCount())).mkString(", ");
    Assert.assertEquals("(1,2) -> 1, (1,1) -> 2", str);
  }
}