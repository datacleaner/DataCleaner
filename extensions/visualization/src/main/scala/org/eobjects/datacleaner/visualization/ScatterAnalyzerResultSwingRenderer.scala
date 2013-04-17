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
import org.jfree.chart.ChartMouseListener
import org.jfree.chart.ChartMouseEvent
import org.jfree.chart.entity.XYItemEntity
import org.eobjects.datacleaner.bootstrap.WindowContext
import javax.inject.Inject
import org.eobjects.analyzer.beans.api.Provided
import org.eobjects.datacleaner.widgets.result.DrillToDetailsCallbackImpl
import org.eobjects.analyzer.result.ResultProducer
import org.eobjects.analyzer.result.DefaultResultProducer
import org.eobjects.analyzer.result.AnnotatedRowsResult
import org.eobjects.analyzer.result.renderer.RendererFactory

@RendererBean(classOf[SwingRenderingFormat])
class ScatterAnalyzerResultSwingRenderer extends Renderer[ScatterAnalyzerResult, JPanel] {
  
  @Inject
  @Provided
  var windowContext: WindowContext = null
  
  @Inject
  @Provided
  var rendererFactory: RendererFactory = null

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
    val orientation = PlotOrientation.VERTICAL
    val chart = ChartFactory.createScatterPlot(null, xAxisLabel, yAxisLabel, dataset, orientation, legend, tooltips, urls)

    ChartUtils.applyStyles(chart);

    val chartPanel = new ChartPanel(chart)
    chartPanel.addChartMouseListener(new ChartMouseListener() {
      override def chartMouseClicked(event: ChartMouseEvent) = {
        val entity = event.getEntity()
        if (entity != null && entity.isInstanceOf[XYItemEntity]) {
            val xyItemEntity = entity.asInstanceOf[XYItemEntity]
            val seriesIndex = xyItemEntity.getSeriesIndex()
            val series = dataset.getSeries(seriesIndex)
            val itemIndex = xyItemEntity.getItem();
            val dataItem = series.getDataItem(itemIndex);

            val group = result.groups()(seriesIndex)
            val rowAnnotation = group.getRowAnnotation((dataItem.getX(), dataItem.getY()));
            val rowAnnotationFactory = group.getRowAnnotationFactory();
            
            val resultProducer = new DefaultResultProducer(new AnnotatedRowsResult(rowAnnotation, rowAnnotationFactory))
            val callback = new DrillToDetailsCallbackImpl(windowContext, rendererFactory);
            callback.drillToDetails("Detailed results for scatter plot coordinate", resultProducer);
        }
      }
      override def chartMouseMoved(event: ChartMouseEvent) = {}
    });

    return chartPanel;
  }
}