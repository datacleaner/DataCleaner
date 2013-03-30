package org.eobjects.datacleaner.visualization

import org.eobjects.datacleaner.bootstrap.WindowContext
import org.eobjects.analyzer.beans.api.RendererBean
import org.eobjects.analyzer.beans.api.RendererPrecedence
import javax.swing.JPanel
import org.eobjects.analyzer.beans.api.Provided
import org.eobjects.analyzer.result.renderer.RendererFactory
import javax.inject.Inject
import org.eobjects.analyzer.beans.api.Renderer
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.ChartFactory
import org.jfree.data.category.CategoryDataset
import org.jfree.data.category.DefaultCategoryDataset
import org.eobjects.datacleaner.util.ChartUtils
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
    
    val chartPanel = new ChartPanel(chart);
    
    return chartPanel
  }

}