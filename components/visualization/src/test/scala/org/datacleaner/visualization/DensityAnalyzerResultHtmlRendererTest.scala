package org.datacleaner.visualization

import java.io.File

import org.apache.metamodel.util.FileHelper
import org.datacleaner.configuration.{DataCleanerConfigurationImpl, DataCleanerEnvironmentImpl}
import org.datacleaner.descriptors.{Descriptors, SimpleDescriptorProvider}
import org.datacleaner.job.builder.AnalysisJobBuilder
import org.datacleaner.job.runner.AnalysisRunnerImpl
import org.datacleaner.result.html.HtmlAnalysisResultWriter
import org.datacleaner.test.TestHelper
import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit

class DensityAnalyzerResultHtmlRendererTest extends AssertionsForJUnit {

  val descriptorProvider = new SimpleDescriptorProvider()
  descriptorProvider.addRendererBeanDescriptor(Descriptors.ofRenderer(classOf[DensityAnalyzerResultHtmlRenderer]));
  val configuration = new DataCleanerConfigurationImpl().withEnvironment(new DataCleanerEnvironmentImpl().withDescriptorProvider(descriptorProvider));

  val orderdb = TestHelper.createSampleDatabaseDatastore("orderdb");

  @Test
  def testRenderSimple = {
    val ajb = new AnalysisJobBuilder(configuration);
    ajb.setDatastore(orderdb);
    ajb.addSourceColumns("ORDERFACT.QUANTITYORDERED", "ORDERFACT.PRICEEACH");

    val analyzer = ajb.addAnalyzer(classOf[DensityAnalyzer]);
    analyzer.setConfiguredProperty("Variable1", ajb.getSourceColumnByName("QUANTITYORDERED"));
    analyzer.setConfiguredProperty("Variable2", ajb.getSourceColumnByName("PRICEEACH"));

    val job = ajb.toAnalysisJob()

    val analysisResult = new AnalysisRunnerImpl(configuration).run(job);

    val writer = new HtmlAnalysisResultWriter()
    val fileWriter = FileHelper.getBufferedWriter(new File("target/render_density_simple.html"))
    writer.write(analysisResult, configuration, fileWriter);
    fileWriter.close();
  }
}
