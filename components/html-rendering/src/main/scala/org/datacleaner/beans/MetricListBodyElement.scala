package org.datacleaner.beans
import org.datacleaner.result.html.BodyElement
import org.datacleaner.result.html.HtmlRenderingContext
import org.datacleaner.api.AnalyzerResult
import org.datacleaner.descriptors.HasAnalyzerResultBeanDescriptor
import org.datacleaner.job.ComponentJob
import scala.collection.JavaConversions._
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor
import org.datacleaner.api.InputColumn
import org.datacleaner.job.ConfigurableBeanJob
import org.datacleaner.descriptors.MetricParameters
import org.datacleaner.job.ConfigurableBeanJob

/**
 * Body element which simply produces a list of metrics as per the descriptor of the component job being rendered.
 */
class MetricListBodyElement(result: AnalyzerResult) extends BodyElement {

  override def toHtml(context: HtmlRenderingContext): String = {
    val componentJob = context.getComponentJob()
    if (componentJob == null) {
      return "";
    }

    componentJob match {
      // we expect the componentjob to be a ConfigurableBeanJob
      case job: ConfigurableBeanJob[_] => return renderConfigurableBeanJob(job);

      // or else we cannot handle it
      case _ => return "";
    }
  }

  def renderConfigurableBeanJob(job: ConfigurableBeanJob[_]): String = {
    val descriptor = job.getDescriptor();
    descriptor match {
      // if descriptor is an HasAnalyzerResultBeanDescriptor
      case desc: HasAnalyzerResultBeanDescriptor[_] => return renderMetrics(job, desc);

      // or else we cannot handle it
      case _ => return "";
    }
  }

  def renderMetrics(job: ConfigurableBeanJob[_], descriptor: HasAnalyzerResultBeanDescriptor[_]): String = {
    val primaryInputProperties = descriptor.getConfiguredPropertiesForInput(false)
    val columns = primaryInputProperties.flatMap(property => getInputColumns(job, property));
    val resultMetrics = descriptor.getResultMetrics()
    
    val html = <div class="analyzerResultMetrics">{
      resultMetrics.toList.map(m => {
        if (!m.isParameterizedByString()) {
          if (m.isParameterizedByInputColumn()) {
            columns.map(col => {
              <div class="metric">
                <span class="metricName">{ m.getName() } ({ col.getName() })</span>
                <span class="metricValue">{ m.getValue(result, new MetricParameters(col)) }</span>
              </div>
            });
          } else {
            <div class="metric">
              <span class="metricName">{ m.getName() }</span>
              <span class="metricValue">{ m.getValue(result, null) }</span>
            </div>
          }
        }
      })
    }</div>

    return html.toString;
  }

  def getInputColumns(componentJob: ConfigurableBeanJob[_], property: ConfiguredPropertyDescriptor): Seq[InputColumn[_]] = {
    val value = componentJob.getConfiguration().getProperty(property);
    value match {
      case value: InputColumn[_] => Seq(value);
      case value: Array[InputColumn[_]] => value.toSeq
      case _ => Nil
    }
  }
}
