package org.eobjects.analyzer.beans
import org.eobjects.analyzer.result.html.BodyElement
import org.eobjects.analyzer.result.html.HtmlRenderingContext
import org.eobjects.analyzer.result.AnalyzerResult
import org.eobjects.analyzer.descriptors.HasAnalyzerResultBeanDescriptor
import org.eobjects.analyzer.job.ComponentJob
import scala.collection.JavaConversions._
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor
import org.eobjects.analyzer.data.InputColumn
import org.eobjects.analyzer.job.ConfigurableBeanJob
import org.eobjects.analyzer.descriptors.MetricParameters
import org.eobjects.analyzer.job.ConfigurableBeanJob

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