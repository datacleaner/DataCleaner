package org.datacleaner.beans
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import org.datacleaner.data.MockInputColumn
import org.datacleaner.data.MockInputRow
import org.datacleaner.result.renderer.CrosstabTextRenderer
import org.junit.Assert
import org.datacleaner.storage.InMemoryRowAnnotationFactory

class NumberAnalyzerScalaTest extends AssertionsForJUnit {

  @Test
  def testDescriptiveStats() = {
    val col1 = new MockInputColumn("number", classOf[Number]);

    val analyzer = new NumberAnalyzer()
    analyzer.descriptiveStatistics = true;
    analyzer._columns = Array(col1);
    analyzer._annotationFactory = new InMemoryRowAnnotationFactory()

    analyzer.init()

    analyzer.run(new MockInputRow().put(col1, 1), 1);
    analyzer.run(new MockInputRow().put(col1, 2), 1);
    analyzer.run(new MockInputRow().put(col1, 3), 1);
    analyzer.run(new MockInputRow().put(col1, 4), 1);
    analyzer.run(new MockInputRow().put(col1, 5), 1);

    val result = analyzer.getResult();

    val text = new CrosstabTextRenderer().render(result)

    Assert.assertEquals("""                   number 
Row count               5 
Null count              0 
Highest value           5 
Lowest value            1 
Sum                    15 
Mean                    3 
Geometric mean       2.61 
Standard deviation   1.58 
Variance              2.5 
Second moment          10 
Sum of squares         55 
Median                  3 
25th percentile       1.5 
75th percentile       4.5 
Skewness                0 
Kurtosis             -1.2 
""".replaceAll("\r\n", "\n"), text.replaceAll("\r\n", "\n"));
  }
}
