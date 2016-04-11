package org.datacleaner.visualization

import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import org.datacleaner.api.InputColumn
import org.datacleaner.data.MockInputColumn
import org.datacleaner.data.MockInputRow
import org.junit.Assert
import java.util.stream.Collectors

class StackedAreaAnalyzerTest extends AssertionsForJUnit {

  @Test
  def testBasicScenario = {
    val analyzer = new StackedAreaAnalyzer()

    val col1 = new MockInputColumn("foo", classOf[Number])
    val col2 = new MockInputColumn("bar", classOf[Number])
    analyzer.measureColumns = Array[InputColumn[Number]](col1, col2)
    analyzer.categoryColumn = new MockInputColumn("category", classOf[Number]);

    analyzer.initialize()
    analyzer.validate()

    analyzer.run(new MockInputRow().put(analyzer.categoryColumn, 1).put(col1, 10).put(col2, 10), 1);
    analyzer.run(new MockInputRow().put(analyzer.categoryColumn, 2).put(col1, 15).put(col2, 10), 1);
    analyzer.run(new MockInputRow().put(analyzer.categoryColumn, 2).put(col1, 5), 1);
    analyzer.run(new MockInputRow().put(col1, 5), 1);
    
    val result = analyzer.getResult()
    Assert.assertNotNull(result.getMeasureColumns())
    Assert.assertEquals(2, result.getMeasureColumns().size)
    Assert.assertEquals("[1, 2]", result.getCategories().toString());
    
    Assert.assertNull(result.getMeasures(1337))
    Assert.assertEquals("[10, 10]", result.getMeasures(1).toString());
    Assert.assertEquals("[20.0, 10]", result.getMeasures(2).toString());
    
  }
}
