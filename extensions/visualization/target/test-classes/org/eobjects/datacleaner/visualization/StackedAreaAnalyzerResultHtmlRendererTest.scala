package org.eobjects.datacleaner.visualization

import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import org.eobjects.analyzer.data.MockInputColumn
import org.eobjects.analyzer.data.InputColumn
import org.eobjects.analyzer.data.MockInputRow
import org.junit.Assert
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl
import org.eobjects.analyzer.result.html.HtmlAnalysisResultWriter
import org.eobjects.analyzer.test.TestHelper
import org.eobjects.analyzer.descriptors.SimpleDescriptorProvider
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl
import org.apache.metamodel.util.FileHelper
import java.io.File
import org.eobjects.analyzer.descriptors.Descriptors
import org.eobjects.analyzer.beans.filter.MaxRowsFilter

class StackedAreaAnalyzerResultHtmlRendererTest extends AssertionsForJUnit {

  val descriptorProvider = new SimpleDescriptorProvider()
  descriptorProvider.addRendererBeanDescriptor(Descriptors.ofRenderer(classOf[StackedAreaAnalyzerResultHtmlRenderer]));
  val configuration = new AnalyzerBeansConfigurationImpl().replace(descriptorProvider);

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