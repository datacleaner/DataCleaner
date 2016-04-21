package org.datacleaner.visualization

import javax.inject.Inject
import javax.swing.JPanel

import org.datacleaner.api.{Provided, Renderer, RendererBean, RendererPrecedence}
import org.datacleaner.bootstrap.WindowContext
import org.datacleaner.result.renderer.{RendererFactory, SwingRenderingFormat}
import org.datacleaner.result.{AnnotatedRowsResult, DefaultResultProducer}
import org.datacleaner.util.ChartUtils
import org.datacleaner.widgets.result.DrillToDetailsCallbackImpl
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.entity.XYItemEntity
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYBlockRenderer
import org.jfree.chart.{ChartMouseEvent, ChartMouseListener, JFreeChart}
import org.jfree.data.xy.DefaultXYZDataset

import scala.collection.JavaConversions._

@RendererBean(classOf[SwingRenderingFormat])
class DensityAnalyzerResultSwingRenderer extends Renderer[IDensityAnalyzerResult, JPanel] {

  @Inject
  @Provided
  var windowContext: WindowContext = null

  @Inject
  @Provided
  var rendererFactory: RendererFactory = null

  override def getPrecedence(r: IDensityAnalyzerResult) = RendererPrecedence.HIGH

  override def render(r: IDensityAnalyzerResult): JPanel = {
    val annotations = r.getRowAnnotations
    val xValues = Array.fill[Double](annotations.size)(0.0d)
    val yValues = Array.fill[Double](annotations.size)(0.0d)
    val zValues = Array.fill[Double](annotations.size)(0.0d)
    val arrays = Array(xValues, yValues, zValues)

    var maxZvalue = 0
    var i = 0
    annotations.foreach(entry => {
      val x = entry._1.getLeft.toDouble
      val y = entry._1.getRight.toDouble
      val z = entry._2.getRowCount
      maxZvalue = scala.math.max(z, maxZvalue)
      arrays(0).update(i, x)
      arrays(1).update(i, y)
      arrays(2).update(i, z)
      i = i + 1
    });

    val dataset = new DefaultXYZDataset()
    dataset.addSeries("Observations", arrays);

    val paintScale = DensityAnalyzerColors.getPaintScale(maxZvalue)

    val renderer = new XYBlockRenderer()
    renderer.setPaintScale(paintScale)

    val xAxis = new NumberAxis(r.getVariable1.getName());
    xAxis.setAutoRangeIncludesZero(false);
    val yAxis = new NumberAxis(r.getVariable2.getName());
    yAxis.setAutoRangeIncludesZero(false);

    val plot = new XYPlot(dataset, xAxis, yAxis, renderer)
    val title = null;
    val legend = false;
    val chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend);

    ChartUtils.applyStyles(chart);

    val chartPanel = ChartUtils.createPanel(chart, true);

    chartPanel.addChartMouseListener(new ChartMouseListener() {
      override def chartMouseClicked(event: ChartMouseEvent) = {
        val entity = event.getEntity()
        if (entity != null && entity.isInstanceOf[XYItemEntity]) {
          val xyItemEntity = entity.asInstanceOf[XYItemEntity]
          val seriesIndex = xyItemEntity.getSeriesIndex()

          val itemIndex = xyItemEntity.getItem();

          val x = arrays(0)(itemIndex);
          val y = arrays(1)(itemIndex);

          val rowAnnotation = r.getRowAnnotation(x.intValue(), y.intValue())
          val rowAnnotationFactory = r.getRowAnnotationFactory;

          val resultProducer = new DefaultResultProducer(new AnnotatedRowsResult(rowAnnotation, rowAnnotationFactory))
          val callback = new DrillToDetailsCallbackImpl(windowContext, rendererFactory);
          callback.drillToDetails("Detailed results for density plot coordinate", resultProducer);
        }
      }
      override def chartMouseMoved(event: ChartMouseEvent) = {}
    });

    return chartPanel;
  }

}
