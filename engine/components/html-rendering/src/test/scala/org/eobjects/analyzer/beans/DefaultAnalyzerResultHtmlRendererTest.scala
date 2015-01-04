package org.eobjects.analyzer.beans
import org.eobjects.analyzer.beans.api.Analyzer
import org.eobjects.analyzer.beans.api.AnalyzerBean
import org.eobjects.analyzer.beans.api.Configured
import org.eobjects.analyzer.beans.api.RendererBean
import org.eobjects.analyzer.data.InputColumn
import org.eobjects.analyzer.data.InputRow
import org.eobjects.analyzer.descriptors.Descriptors
import org.eobjects.analyzer.job.ImmutableAnalyzerJob
import org.eobjects.analyzer.job.ImmutableBeanConfiguration
import org.eobjects.analyzer.result.html.DefaultHtmlRenderingContext
import org.eobjects.analyzer.result.Metric
import org.eobjects.analyzer.result.AnalyzerResult
import org.junit.Test
import org.junit.Assert
import org.scalatest.junit.AssertionsForJUnit
import org.eobjects.analyzer.result.html.ComponentHtmlRenderingContext

class DefaultAnalyzerResultHtmlRendererTest extends AssertionsForJUnit {

  @AnalyzerBean("Example analyzer")
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
    
    val job = new ImmutableAnalyzerJob(null, descriptor, new ImmutableBeanConfiguration(null),
      null, null)

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