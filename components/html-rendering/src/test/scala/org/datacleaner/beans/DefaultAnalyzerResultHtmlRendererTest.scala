package org.datacleaner.beans

import javax.inject.Named

import org.datacleaner.api._
import org.datacleaner.descriptors.Descriptors
import org.datacleaner.job.{ImmutableAnalyzerJob, ImmutableComponentConfiguration}
import org.datacleaner.result.html.{ComponentHtmlRenderingContext, DefaultHtmlRenderingContext}
import org.junit.{Assert, Test}
import org.scalatest.junit.AssertionsForJUnit

class DefaultAnalyzerResultHtmlRendererTest extends AssertionsForJUnit {

  @Named("Example analyzer")
  class ExampleAnalyzer(col: InputColumn[_]) extends Analyzer[ExampleResult] {

    @Configured
    val column: InputColumn[_] = col;
    def run(row: InputRow, rowCount: Int) = {}
    def getResult() = new ExampleResult
  }

  class ExampleResult extends AnalyzerResult {
    @Metric("Elite")
    def getEliteMetric = 1337

    @Metric("Foo")
    def getFooMetric = 500;
  }

  @Test
  def testRenderResult = {
    val descriptor = Descriptors.ofAnalyzer(classOf[ExampleAnalyzer])
    
    val resultMetrics = descriptor.getResultMetrics();
    Assert.assertEquals("[MetricDescriptorImpl[name=Elite], MetricDescriptorImpl[name=Foo]]", resultMetrics.toString());
    
    val job = new ImmutableAnalyzerJob(null, descriptor, new ImmutableComponentConfiguration(null),
      null, null, null)

    val renderer = new DefaultAnalyzerResultHtmlRenderer();

    val html = renderer.render(new ExampleResult())
    val context = new ComponentHtmlRenderingContext(new DefaultHtmlRenderingContext(), job);
    
    html.initialize(context);

    Assert.assertEquals("""<div class="analyzerResultMetrics"><div class="metric">
              <span class="metricName">Elite</span>
              <span class="metricValue">1337</span>
            </div><div class="metric">
              <span class="metricName">Foo</span>
              <span class="metricValue">500</span>
            </div></div>""".replaceAll("\r\n", "\n"), html.getBodyElements().get(0).toHtml(context).replaceAll("\r\n", "\n"));

  }
}
