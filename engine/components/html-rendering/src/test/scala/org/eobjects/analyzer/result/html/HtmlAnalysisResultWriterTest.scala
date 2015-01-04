package org.eobjects.analyzer.result.html
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import org.eobjects.analyzer.result.SimpleAnalysisResult
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl
import java.io.StringWriter
import org.junit.Assert
import scala.xml.XML

class HtmlAnalysisResultWriterTest extends AssertionsForJUnit {

  @Test
  def testEmptyRendering = {
    val writer = new HtmlAnalysisResultWriter();
    val analysisResult = new SimpleAnalysisResult(new java.util.HashMap());
    val configuration = new AnalyzerBeansConfigurationImpl();
    val stringWriter = new StringWriter();

    writer.write(analysisResult, configuration, stringWriter);

    val html = stringWriter.toString();

    Assert.assertEquals("""<!DOCTYPE html>
<html>
<head>
  <title>Analysis result</title>  <script type="text/javascript" src="http://eobjects.org/resources/datacleaner-html-rendering/analysis-result.js"></script>
<link rel="shortcut icon" href="http://eobjects.org/resources/datacleaner-html-rendering/analysis-result-icon.png" />
<script type="text/javascript">//<![CDATA[
  var analysisResult = {};
//]]>
</script>
</head><body>
<div class="analysisResultContainer">
<div class="analysisResultHeader"><ul class="analysisResultToc"></ul></div></div>
</body></html>""".replaceAll("\r\n", "\n"), html.replaceAll("\r\n", "\n"));

  }

}