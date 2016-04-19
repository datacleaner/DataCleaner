package org.datacleaner.visualization

import org.jfree.chart.plot.XYPlot
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.renderer.xy.XYBlockRenderer
import org.jfree.data.xy.DefaultXYZDataset
import org.datacleaner.util.ChartUtils
import org.datacleaner.util.LookAndFeelManager
import org.jfree.chart.renderer.LookupPaintScale
import javax.swing.JFrame
import java.awt.Color
import org.jfree.chart.axis.NumberAxis
import org.datacleaner.util.WidgetUtils
import org.datacleaner.test.TestHelper
import org.datacleaner.configuration.DataCleanerConfigurationImpl
import org.datacleaner.configuration.DataCleanerEnvironmentImpl
import org.datacleaner.descriptors.SimpleDescriptorProvider
import org.datacleaner.descriptors.Descriptors
import org.datacleaner.job.runner.AnalysisRunnerImpl
import org.datacleaner.job.builder.AnalysisJobBuilder

object DensityAnalyzerResultSwingRendererRevisedTestApp {

  def main(args: Array[String]) {
    val descriptorProvider = new SimpleDescriptorProvider()
    descriptorProvider.addRendererBeanDescriptor(Descriptors.ofRenderer(classOf[DensityAnalyzerResultSwingRendererRevised]));
    val configuration = new DataCleanerConfigurationImpl().withEnvironment(new DataCleanerEnvironmentImpl().withDescriptorProvider(descriptorProvider));

    val orderdb = TestHelper.createSampleDatabaseDatastore("orderdb");
    
    val ajb = new AnalysisJobBuilder(configuration);
    ajb.setDatastore(orderdb);
    ajb.addSourceColumns("ORDERFACT.QUANTITYORDERED", "ORDERFACT.PRICEEACH");

    val analyzer = ajb.addAnalyzer(classOf[DensityAnalyzer]);
    analyzer.setConfiguredProperty("Variable1", ajb.getSourceColumnByName("QUANTITYORDERED"));
    analyzer.setConfiguredProperty("Variable2", ajb.getSourceColumnByName("PRICEEACH"));

    val job = ajb.toAnalysisJob()

    val analysisResult = new AnalysisRunnerImpl(configuration).run(job);
    
    val analyzerResult = analysisResult.getResults().get(0).asInstanceOf[DensityAnalyzerResultRevised]

    val panel = new DensityAnalyzerResultSwingRendererRevised().render(analyzerResult)

    LookAndFeelManager.get().init();

    val window = new JFrame("Example window")
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    window.getContentPane().add(panel)
    window.pack();
    window.setVisible(true)
  }
}
