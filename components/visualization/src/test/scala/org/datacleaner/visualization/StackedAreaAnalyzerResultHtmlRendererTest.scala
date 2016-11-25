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

class StackedAreaAnalyzerResultHtmlRendererTest extends AssertionsForJUnit {

  val descriptorProvider = new SimpleDescriptorProvider()
  descriptorProvider.addRendererBeanDescriptor(Descriptors.ofRenderer(classOf[StackedAreaAnalyzerResultHtmlRenderer]));
  val configuration = new DataCleanerConfigurationImpl().withEnvironment(new DataCleanerEnvironmentImpl().withDescriptorProvider(descriptorProvider));

  val orderdb = TestHelper.createSampleDatabaseDatastore("orderdb");

  @Test
  def testRenderSimple = {
    val ajb = new AnalysisJobBuilder(configuration);
    ajb.setDatastore(orderdb);
    ajb.addSourceColumns("ORDERFACT.ORDERNUMBER", "ORDERFACT.YEAR_ID", "ORDERFACT.TOTALPRICE", "ORDERFACT.CUSTOMERNUMBER");
    
    val analyzer = ajb.addAnalyzer(classOf[StackedAreaAnalyzer]);
    analyzer.setConfiguredProperty("Category column", ajb.getSourceColumnByName("ORDERNUMBER"));
    analyzer.setConfiguredProperty("Measure columns", Array(ajb.getSourceColumnByName("TOTALPRICE"),ajb.getSourceColumnByName("YEAR_ID"),ajb.getSourceColumnByName("CUSTOMERNUMBER")));

    val job = ajb.toAnalysisJob()

    val analysisResult = new AnalysisRunnerImpl(configuration).run(job);

    val writer = new HtmlAnalysisResultWriter()
    val fileWriter = FileHelper.getBufferedWriter(new File("target/render_stack_simple.html"))
    writer.write(analysisResult, configuration, fileWriter);
    fileWriter.close();
  }
}
