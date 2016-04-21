package org.datacleaner.visualization

import javax.inject.Inject
import javax.swing.JPanel

import org.datacleaner.api.{Provided, Renderer, RendererBean, RendererPrecedence}
import org.datacleaner.bootstrap.WindowContext
import org.datacleaner.result.renderer.{RendererFactory, SwingRenderingFormat}
import org.datacleaner.result.{AnnotatedRowsResult, DefaultResultProducer}
import org.datacleaner.util.ChartUtils
import org.datacleaner.widgets.result.DrillToDetailsCallbackImpl
import org.jfree.chart.entity.XYItemEntity
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.{ChartFactory, ChartMouseEvent, ChartMouseListener}
import org.jfree.data.xy.{XYSeries, XYSeriesCollection}

import scala.collection.JavaConverters._

@RendererBean(classOf[SwingRenderingFormat])
class ScatterAnalyzerResultSwingRenderer extends Renderer[IScatterAnalyzerResult, JPanel] {
  
  @Inject
  @Provided
  var windowContext: WindowContext = null
  
  @Inject
  @Provided
  var rendererFactory: RendererFactory = null

  override def getPrecedence(result: IScatterAnalyzerResult) = RendererPrecedence.HIGH

  override def render(result: IScatterAnalyzerResult): JPanel = {
    val xAxisLabel = result.getVariable1.getName
    val yAxisLabel = result.getVariable2.getName

    val dataset: XYSeriesCollection = new XYSeriesCollection

    result.getGroups.asScala.foreach(group => {
      val xySeries = new XYSeries(group.getName)
      group.getCoordinates.asScala.foreach(xy => {
        xySeries.add(xy.getLeft, xy.getRight)
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

            val group = result.getGroups.asScala(seriesIndex)
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
