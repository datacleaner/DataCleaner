package org.datacleaner.visualization

import javax.swing.JPanel

import org.datacleaner.api.{Renderer, RendererBean, RendererPrecedence}
import org.datacleaner.result.renderer.SwingRenderingFormat
import org.datacleaner.util.ChartUtils
import org.jfree.chart.ChartFactory
import org.jfree.chart.plot.PlotOrientation
import org.jfree.ui.RectangleEdge

@RendererBean(classOf[SwingRenderingFormat])
class StackedAreaAnalyzerResultSwingRenderer extends Renderer[IStackedAreaAnalyzerResult, JPanel] {
  
  override def getPrecedence(result: IStackedAreaAnalyzerResult) = RendererPrecedence.HIGH

  override def render(result: IStackedAreaAnalyzerResult): JPanel = {
    val categoryAxisLabel = result.getCategoryColumn.getName()
    val valueAxisLabel = null
      
    val dataset = new StackedAreaAnalyzerResultCategoryDataset(result);
    
    val legend = true;
    val tooltips = true;
    val urls = false;
    val orientation = PlotOrientation.VERTICAL
    val title = null
    
    val chart = ChartFactory.createStackedAreaChart(title, categoryAxisLabel, valueAxisLabel, dataset, orientation, legend, tooltips, urls)
    chart.getLegend().setPosition(RectangleEdge.TOP);
    
    ChartUtils.applyStyles(chart);
    
    val chartPanel = ChartUtils.createPanel(chart, true);
    
    return chartPanel
  }

}
