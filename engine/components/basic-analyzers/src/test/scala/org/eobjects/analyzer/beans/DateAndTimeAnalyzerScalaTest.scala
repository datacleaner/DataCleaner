package org.eobjects.analyzer.beans

import java.util.Date

import org.apache.metamodel.util.DateUtils
import org.apache.metamodel.util.Month
import org.eobjects.analyzer.data.MockInputColumn
import org.eobjects.analyzer.data.MockInputRow
import org.eobjects.analyzer.result.renderer.CrosstabTextRenderer
import org.eobjects.analyzer.storage.InMemoryRowAnnotationFactory
import org.junit.Assert
import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit

class DateAndTimeAnalyzerScalaTest extends AssertionsForJUnit {

  @Test
  def testDescriptiveStats() = {
    val col1 = new MockInputColumn("date", classOf[Date]);

    val analyzer = new DateAndTimeAnalyzer()
    analyzer.descriptiveStatistics = true;
    analyzer._columns = Array(col1);
    analyzer._annotationFactory = new InMemoryRowAnnotationFactory()

    analyzer.init()

    analyzer.run(new MockInputRow().put(col1, DateUtils.get(2013, Month.JANUARY, 1)), 1);
    analyzer.run(new MockInputRow().put(col1, DateUtils.get(2013, Month.JANUARY, 2)), 1);
    analyzer.run(new MockInputRow().put(col1, DateUtils.get(2013, Month.JANUARY, 3)), 1);
    analyzer.run(new MockInputRow().put(col1, DateUtils.get(2013, Month.JANUARY, 4)), 1);
    analyzer.run(new MockInputRow().put(col1, DateUtils.get(2013, Month.JANUARY, 5)), 1);

    val result = analyzer.getResult();

    val text = new CrosstabTextRenderer().render(result)

    Assert.assertEquals("""                date   
Row count            5 
Null count           0 
Highest date    2013-01-05 
Lowest date     2013-01-01 
Highest time    00:00:00.000 
Lowest time     00:00:00.000 
Mean            2013-01-03 00:00 
Median          2013-01-03 00:00 
25th percentile 2013-01-01 12:00 
75th percentile 2013-01-04 12:00 
Skewness             0 
Kurtosis          -1.2 
""".replaceAll("\r\n", "\n"), text.replaceAll("\r\n", "\n"));
  }

  @Test
  def testMetricParsing() = {
    val col1 = new MockInputColumn("date", classOf[Date]);

    val analyzer = new DateAndTimeAnalyzer()
    analyzer.descriptiveStatistics = true;
    analyzer._columns = Array(col1);
    analyzer._annotationFactory = new InMemoryRowAnnotationFactory()

    analyzer.init()

    analyzer.run(new MockInputRow().put(col1, DateUtils.get(2013, Month.JANUARY, 1)), 1);
    analyzer.run(new MockInputRow().put(col1, DateUtils.get(2013, Month.JANUARY, 2)), 1);
    analyzer.run(new MockInputRow().put(col1, DateUtils.get(2013, Month.JANUARY, 3)), 1);
    analyzer.run(new MockInputRow().put(col1, DateUtils.get(2013, Month.JANUARY, 4)), 1);
    analyzer.run(new MockInputRow().put(col1, DateUtils.get(2013, Month.JANUARY, 5)), 1);

    val result = analyzer.getResult();

    Assert.assertEquals(0, result.getNullCount(col1));
    Assert.assertEquals(15708, result.getMean(col1));
    Assert.assertEquals(15710, result.getHighestDate(col1));
    Assert.assertEquals(15706, result.getLowestDate(col1));
    Assert.assertEquals(15708, result.getMedian(col1));
    Assert.assertEquals(15706, result.getPercentile25(col1));
    Assert.assertEquals(15709, result.getPercentile75(col1));

    Assert.assertEquals(0.0, result.getSkewness(col1).doubleValue(), 0.001);
    Assert.assertEquals(-1.1999, result.getKurtosis(col1).doubleValue(), 0.001);
  }
}