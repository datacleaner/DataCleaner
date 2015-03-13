package org.eobjects.datacleaner.visualization

import java.io.File
import scala.collection.JavaConversions.mapAsJavaMap
import org.eobjects.analyzer.beans.api.AnalyzerBean
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl
import org.eobjects.analyzer.data.MockInputColumn
import org.eobjects.analyzer.data.MockInputRow
import org.eobjects.analyzer.descriptors.Descriptors
import org.eobjects.analyzer.descriptors.SimpleDescriptorProvider
import org.eobjects.analyzer.job.ComponentJob
import org.eobjects.analyzer.job.ImmutableAnalyzerJob
import org.eobjects.analyzer.result.SimpleAnalysisResult
import org.eobjects.analyzer.result.html.HtmlAnalysisResultWriter
import org.eobjects.analyzer.storage.InMemoryRowAnnotationFactory
import org.apache.metamodel.util.FileHelper
import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder
import org.eobjects.analyzer.test.TestHelper
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl

class DensityAnalyzerResultHtmlRendererTest extends AssertionsForJUnit {

  val descriptorProvider = new SimpleDescriptorProvider()
  descriptorProvider.addRendererBeanDescriptor(Descriptors.ofRenderer(classOf[DensityAnalyzerResultHtmlRenderer]));
  val configuration = new AnalyzerBeansConfigurationImpl().replace(descriptorProvider);

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