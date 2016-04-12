package org.datacleaner.visualization

import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import org.junit.Assert
import org.junit.Test
import org.datacleaner.data.MockInputColumn
import org.datacleaner.data.MockInputRow
import scala.collection.SortedMap
import scala.collection.immutable.TreeMap
import org.datacleaner.storage.RowAnnotations

class ScatterAnalyzerTest extends AssertionsForJUnit {

  @Test
  def testMapDefaultValue = {
    val analyzer = new ScatterAnalyzer()
    analyzer._rowAnnotationFactory = RowAnnotations.getInMemoryFactory()
    analyzer._variable1 = new MockInputColumn("foo");
    analyzer._variable2 = new MockInputColumn("bar");
    

    analyzer.run(new MockInputRow().put(analyzer._variable1, 1).put(analyzer._variable2, 1), 1);
    analyzer.run(new MockInputRow().put(analyzer._variable1, 1).put(analyzer._variable2, 2), 1);
    analyzer.run(new MockInputRow().put(analyzer._variable1, 1).put(analyzer._variable2, 1), 1);
    
    val group = analyzer.getResult.getGroups().get(0)
    val iterator = group.getAnnotations().entrySet().iterator();
    
    var str:String = new String
    while (iterator.hasNext()){
      val row = iterator.next(); 
      var rowData = "(" + row.getKey.getX().toInt + "," + row.getKey.getY().toInt + ") -> " + row.getValue().getRowCount() + ", "
      str = str.concat(rowData)
    }
    str = str.dropRight(2)
    Assert.assertEquals("(1,1) -> 2, (1,2) -> 1", str);
    
  }
}
