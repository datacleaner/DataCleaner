package org.datacleaner.visualization

import org.datacleaner.bootstrap.WindowContext
import org.datacleaner.api.RendererBean
import org.datacleaner.api.RendererPrecedence
import org.datacleaner.api.Provided
import org.datacleaner.api.Renderer
import org.datacleaner.result.renderer.RendererFactory
import org.datacleaner.result.renderer.SwingRenderingFormat
import org.datacleaner.util.ChartUtils
import javax.swing.JPanel
import javax.inject.Inject
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.ChartFactory
import org.jfree.data.category.CategoryDataset
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.chart.ChartPanel
import org.jfree.ui.RectangleEdge

@RendererBean(classOf[SwingRenderingFormat])
class StackedAreaAnalyzerResultSwingRenderer extends Renderer[StackedAreaAnalyzerResult, JPanel] {
  
  override def getPrecedence(result: StackedAreaAnalyzerResult) = RendererPrecedence.HIGH

  override def render(result: StackedAreaAnalyzerResult): JPanel = {
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
