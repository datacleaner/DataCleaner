package org.eobjects.datacleaner.visualization

import org.eobjects.analyzer.beans.api.RendererBean
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat
import org.eobjects.analyzer.beans.api.Renderer
import javax.swing.JPanel
import org.eobjects.analyzer.beans.api.RendererPrecedence
import org.jfree.chart.ChartFactory
import org.jfree.data.xy.XYDataset
import org.jfree.data.xy.XYSeriesCollection
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.ChartPanel
import org.jfree.data.xy.XYSeries
import org.eobjects.datacleaner.util.ChartUtils

@RendererBean(classOf[SwingRenderingFormat])
class ScatterAnalyzerResultSwingRenderer extends Renderer[ScatterAnalyzerResult, JPanel] {

  override def getPrecedence(result: ScatterAnalyzerResult) = RendererPrecedence.HIGH

  override def render(result: ScatterAnalyzerResult): JPanel = {
    val xAxisLabel = result.variable1.getName();
    val yAxisLabel = result.variable2.getName();
    
    val dataset: XYSeriesCollection = new XYSeriesCollection
    
    result.groups.foreach(group => {
      val xySeries = new XYSeries(group.name)
      group.getCoordinates.foreach(xy => {
        xySeries.add(xy._1, xy._2);
      })
      dataset.addSeries(xySeries)
    });
    
    val legend = result.hasGroups();
    val tooltips = true;
    val urls = false;
    val orientation = PlotOrientation.HORIZONTAL
    val chart = ChartFactory.createScatterPlot(null, xAxisLabel, yAxisLabel, dataset, orientation, legend, tooltips, urls)
    
    ChartUtils.applyStyles(chart);
    
    return new ChartPanel(chart);
  }
}