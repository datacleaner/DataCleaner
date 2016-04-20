package org.datacleaner.visualization

import javax.swing.JFrame

import org.datacleaner.configuration.{DataCleanerConfigurationImpl, DataCleanerEnvironmentImpl}
import org.datacleaner.descriptors.{Descriptors, SimpleDescriptorProvider}
import org.datacleaner.job.builder.AnalysisJobBuilder
import org.datacleaner.job.runner.AnalysisRunnerImpl
import org.datacleaner.test.TestHelper
import org.datacleaner.util.LookAndFeelManager

object DensityAnalyzerResultSwingRendererTestApp {

  def main(args: Array[String]) {
    val descriptorProvider = new SimpleDescriptorProvider()
    descriptorProvider.addRendererBeanDescriptor(Descriptors.ofRenderer(classOf[DensityAnalyzerResultSwingRenderer]));
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
    
    val analyzerResult = analysisResult.getResults().get(0).asInstanceOf[IDensityAnalyzerResult]

    val panel = new DensityAnalyzerResultSwingRenderer().render(analyzerResult)

    LookAndFeelManager.get().init();

    val window = new JFrame("Example window")
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    window.getContentPane().add(panel)
    window.pack();
    window.setVisible(true)
  }
}
