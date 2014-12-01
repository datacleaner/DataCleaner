package org.eobjects.datacleaner.visualization

import java.awt.Color
import org.eobjects.analyzer.beans.api.Renderer
import org.eobjects.analyzer.beans.api.RendererBean
import org.eobjects.analyzer.beans.api.RendererPrecedence
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat
import org.eobjects.datacleaner.util.ChartUtils
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.LookupPaintScale
import org.jfree.chart.renderer.xy.XYBlockRenderer
import org.jfree.data.xy.DefaultXYZDataset
import javax.swing.JPanel
import org.eobjects.datacleaner.util.WidgetUtils
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.ChartMouseListener
import org.jfree.chart.ChartMouseEvent
import org.jfree.chart.entity.XYItemEntity
import org.eobjects.analyzer.result.DefaultResultProducer
import org.eobjects.analyzer.result.AnnotatedRowsResult
import org.eobjects.datacleaner.widgets.result.DrillToDetailsCallbackImpl
import org.eobjects.analyzer.beans.api.Provided
import org.eobjects.analyzer.result.renderer.RendererFactory
import org.eobjects.datacleaner.bootstrap.WindowContext
import javax.inject.Inject

@RendererBean(classOf[SwingRenderingFormat])
class DensityAnalyzerResultSwingRenderer extends Renderer[DensityAnalyzerResult, JPanel] {

  @Inject
  @Provided
  var windowContext: WindowContext = null

  @Inject
  @Provided
  var rendererFactory: RendererFactory = null

  override def getPrecedence(r: DensityAnalyzerResult) = RendererPrecedence.HIGH

  override def render(r: DensityAnalyzerResult): JPanel = {
    val annotations = r.getRowAnnotations
    val xValues = Array.fill[Double](annotations.size)(0.0d)
    val yValues = Array.fill[Double](annotations.size)(0.0d)
    val zValues = Array.fill[Double](annotations.size)(0.0d)
    val arrays = Array(xValues, yValues, zValues)

    var maxZvalue = 0
    var i = 0
    annotations.foreach(entry => {
      val x = entry._1._1
      val y = entry._1._2
      val z = entry._2.getRowCount()
      maxZvalue = scala.math.max(z, maxZvalue);
      arrays(0).update(i, x);
      arrays(1).update(i, y);
      arrays(2).update(i, z);
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

    val chartPanel = new ChartPanel(chart)

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