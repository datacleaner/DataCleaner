package org.datacleaner.visualization

import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import org.datacleaner.data.MockInputColumn
import org.datacleaner.api.InputColumn
import org.datacleaner.data.MockInputRow
import org.datacleaner.job.builder.AnalysisJobBuilder
import org.datacleaner.job.runner.AnalysisRunnerImpl
import org.datacleaner.result.html.HtmlAnalysisResultWriter
import org.datacleaner.test.TestHelper
import org.datacleaner.descriptors.SimpleDescriptorProvider
import org.datacleaner.descriptors.Descriptors
import org.datacleaner.configuration.DataCleanerConfigurationImpl
import org.datacleaner.configuration.DataCleanerEnvironmentImpl
import org.datacleaner.components.maxrows.MaxRowsFilter
import org.apache.metamodel.util.FileHelper
import org.junit.Assert
import java.io.File

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
