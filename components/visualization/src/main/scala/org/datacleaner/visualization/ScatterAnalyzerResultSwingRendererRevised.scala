package org.datacleaner.visualization

import org.datacleaner.api.RendererBean
import org.datacleaner.result.renderer.SwingRenderingFormat
import org.datacleaner.api.Renderer
import javax.swing.JPanel
import org.datacleaner.api.RendererPrecedence
import org.jfree.chart.ChartFactory
import org.jfree.data.xy.XYDataset
import org.jfree.data.xy.XYSeriesCollection
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.ChartPanel
import org.jfree.data.xy.XYSeries
import org.datacleaner.util.ChartUtils
import org.jfree.chart.ChartMouseListener
import org.jfree.chart.ChartMouseEvent
import org.jfree.chart.entity.XYItemEntity
import org.datacleaner.bootstrap.WindowContext
import javax.inject.Inject
import org.datacleaner.api.Provided
import org.datacleaner.widgets.result.DrillToDetailsCallbackImpl
import org.datacleaner.result.ResultProducer
import org.datacleaner.result.DefaultResultProducer
import org.datacleaner.result.AnnotatedRowsResult
import org.datacleaner.result.renderer.RendererFactory
import collection.JavaConversions._

@RendererBean(classOf[SwingRenderingFormat])
class ScatterAnalyzerResultSwingRendererRevised extends Renderer[ScatterAnalyzerResultRevised, JPanel] {
  
  @Inject
  @Provided
  var windowContext: WindowContext = null
  
  @Inject
  @Provided
  var rendererFactory: RendererFactory = null

  override def getPrecedence(result: ScatterAnalyzerResultRevised) = RendererPrecedence.HIGH

  override def render(result: ScatterAnalyzerResultRevised): JPanel = {
    val xAxisLabel = result.variable1.getName();
    val yAxisLabel = result.variable2.getName();

    val dataset: XYSeriesCollection = new XYSeriesCollection

    result.groups.foreach(group => {
      val xySeries = new XYSeries(group.name)
      group.getCoordinates.foreach(xy => {
        xySeries.add(xy.getX(), xy.getY());
      })
      dataset.addSeries(xySeries)
    });

    val legend = result.hasGroups();
    val tooltips = true;
    val urls = false;
    val orientation = PlotOrientation.VERTICAL
    val chart = ChartFactory.createScatterPlot(null, xAxisLabel, yAxisLabel, dataset, orientation, legend, tooltips, urls)

    ChartUtils.applyStyles(chart);

    val chartPanel = ChartUtils.createPanel(chart, true);
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
            val rowAnnotation = group.getRowAnnotation(dataItem.getX(), dataItem.getY());
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
