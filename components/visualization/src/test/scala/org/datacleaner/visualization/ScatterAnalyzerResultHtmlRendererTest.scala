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

class ScatterAnalyzerResultHtmlRendererTest extends AssertionsForJUnit {

  val descriptorProvider = new SimpleDescriptorProvider()
  descriptorProvider.addRendererBeanDescriptor(Descriptors.ofRenderer(classOf[ScatterAnalyzerResultHtmlRenderer]));
  val configuration = new DataCleanerConfigurationImpl().withEnvironment(new DataCleanerEnvironmentImpl().withDescriptorProvider(descriptorProvider));

  val orderdb = TestHelper.createSampleDatabaseDatastore("orderdb");

  @Test
  def testRenderSimple = {
    val ajb = new AnalysisJobBuilder(configuration);
    ajb.setDatastore(orderdb);
    ajb.addSourceColumns("ORDERFACT.QUANTITYORDERED", "ORDERFACT.TOTALPRICE");

    val analyzer1 = ajb.addAnalyzer(classOf[ScatterAnalyzer]);
    analyzer1.setName("quantity vs price");
    analyzer1.setConfiguredProperty("Variable1", ajb.getSourceColumnByName("QUANTITYORDERED"));
    analyzer1.setConfiguredProperty("Variable2", ajb.getSourceColumnByName("TOTALPRICE"));
    
    val analyzer2 = ajb.addAnalyzer(classOf[ScatterAnalyzer]);
    analyzer2.setName("price vs quantity");
    analyzer2.setConfiguredProperty("Variable1", ajb.getSourceColumnByName("TOTALPRICE"));
    analyzer2.setConfiguredProperty("Variable2", ajb.getSourceColumnByName("QUANTITYORDERED"));

    val job = ajb.toAnalysisJob()

    val analysisResult = new AnalysisRunnerImpl(configuration).run(job);

    val writer = new HtmlAnalysisResultWriter()
    val fileWriter = FileHelper.getBufferedWriter(new File("target/render_scatter_simple.html"))
    writer.write(analysisResult, configuration, fileWriter);
    fileWriter.close();
  }
  
  @Test
  def testRenderWithGroup = {
    val ajb = new AnalysisJobBuilder(configuration);
    ajb.setDatastore(orderdb);
    ajb.addSourceColumns("ORDERFACT.QUANTITYORDERED", "ORDERFACT.TOTALPRICE", "ORDERFACT.YEAR_ID");

    val analyzer = ajb.addAnalyzer(classOf[ScatterAnalyzer]);
    analyzer.setConfiguredProperty("Variable1", ajb.getSourceColumnByName("QUANTITYORDERED"));
    analyzer.setConfiguredProperty("Variable2", ajb.getSourceColumnByName("TOTALPRICE"));
    analyzer.setConfiguredProperty("Group column", ajb.getSourceColumnByName("YEAR_ID"));

    val job = ajb.toAnalysisJob()

    val analysisResult = new AnalysisRunnerImpl(configuration).run(job);

    val writer = new HtmlAnalysisResultWriter()
    val fileWriter = FileHelper.getBufferedWriter(new File("target/render_scatter_with_group.html"))
    writer.write(analysisResult, configuration, fileWriter);
    fileWriter.close();
  }
}
