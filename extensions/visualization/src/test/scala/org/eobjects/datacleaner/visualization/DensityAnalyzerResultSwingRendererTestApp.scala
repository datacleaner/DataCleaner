package org.eobjects.datacleaner.visualization

import org.jfree.chart.plot.XYPlot
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.renderer.xy.XYBlockRenderer
import org.jfree.data.xy.DefaultXYZDataset
import org.eobjects.datacleaner.util.ChartUtils
import org.eobjects.datacleaner.util.LookAndFeelManager
import org.jfree.chart.renderer.LookupPaintScale
import javax.swing.JFrame
import java.awt.Color
import org.jfree.chart.axis.NumberAxis
import org.eobjects.datacleaner.util.WidgetUtils
import org.eobjects.analyzer.test.TestHelper
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl
import org.eobjects.analyzer.descriptors.SimpleDescriptorProvider
import org.eobjects.analyzer.descriptors.Descriptors
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder

object DensityAnalyzerResultSwingRendererTestApp {

  def main(args: Array[String]) {
    val descriptorProvider = new SimpleDescriptorProvider()
    descriptorProvider.addRendererBeanDescriptor(Descriptors.ofRenderer(classOf[DensityAnalyzerResultSwingRenderer]));
    val configuration = new AnalyzerBeansConfigurationImpl().replace(descriptorProvider);

    val orderdb = TestHelper.createSampleDatabaseDatastore("orderdb");
    
    val ajb = new AnalysisJobBuilder(configuration);
    ajb.setDatastore(orderdb);
    ajb.addSourceColumns("ORDERFACT.QUANTITYORDERED", "ORDERFACT.PRICEEACH");

    val analyzer = ajb.addAnalyzer(classOf[DensityAnalyzer]);
    analyzer.setConfiguredProperty("Variable1", ajb.getSourceColumnByName("QUANTITYORDERED"));
    analyzer.setConfiguredProperty("Variable2", ajb.getSourceColumnByName("PRICEEACH"));

    val job = ajb.toAnalysisJob()

    val analysisResult = new AnalysisRunnerImpl(configuration).run(job);
    
    val analyzerResult = analysisResult.getResults().get(0).asInstanceOf[DensityAnalyzerResult]

    val panel = new DensityAnalyzerResultSwingRenderer().render(analyzerResult)

    LookAndFeelManager.getInstance().init();

    val window = new JFrame("Example window")
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    window.getContentPane().add(panel)
    window.pack();
    window.setVisible(true)
  }
}