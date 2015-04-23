package org.datacleaner.result.renderer

import java.lang.Integer
import org.datacleaner.result.html.DefaultHtmlRenderingContext
import org.datacleaner.result.Crosstab
import org.junit.Test
import org.junit.Assert
import org.scalatest.junit.AssertionsForJUnit
import org.datacleaner.result.ResultProducer
import org.datacleaner.storage.InMemoryRowAnnotationFactory
import org.datacleaner.api.InputRow
import org.datacleaner.data.MockInputRow
import org.datacleaner.storage.RowAnnotationImpl
import org.datacleaner.result.DefaultResultProducer
import org.datacleaner.result.CrosstabResult
import org.datacleaner.result.AnnotatedRowsResult
import org.datacleaner.result.NumberResult
import org.datacleaner.configuration.DataCleanerConfigurationImpl
import org.datacleaner.api.InputColumn
import org.datacleaner.data.MockInputColumn

class CrosstabHtmlRendererCallbackTest extends AssertionsForJUnit {
  
  val renderingContext = new DefaultHtmlRenderingContext();

  @Test
  def testOneDimension() = {
    val c = new Crosstab[java.lang.Integer](classOf[java.lang.Integer], "Region");
    c.where("Region", "EU").put(1, true);
    c.where("Region", "USA").put(2, true);
    c.where("Region", "Asia").put(3, true);

    val crosstabRenderer = new CrosstabRenderer(c);
    val result1 = crosstabRenderer.render(new HtmlCrosstabRendererCallback(null,renderingContext)).getBodyElements().get(0).toHtml(renderingContext);
    Assert.assertEquals("<table class='crosstabTable'><tr class='odd'><td class='crosstabHorizontalHeader'>EU</td><td class='crosstabHorizontalHeader'>USA</td><td class='crosstabHorizontalHeader'>Asia</td></tr><tr class='even'><td class='value'>1</td><td class='value'>2</td><td class='value'>3</td></tr></table>", result1.replaceAll("\"", "'"));
 
    crosstabRenderer.makeVertical(c.getDimension(0));
    val result2 = crosstabRenderer.render(new HtmlCrosstabRendererCallback(null,renderingContext)).getBodyElements().get(0).toHtml(renderingContext);
    Assert.assertEquals("<table class='crosstabTable'><tr class='odd'><td class='crosstabVerticalHeader'>EU</td><td class='value'>1</td></tr><tr class='even'><td class='crosstabVerticalHeader'>USA</td><td class='value'>2</td></tr><tr class='odd'><td class='crosstabVerticalHeader'>Asia</td><td class='value'>3</td></tr></table>", result2.replaceAll("\"", "'"));
  }

  @Test
  def testCellValue() = {
    val rendererFactory = new RendererFactory(new DataCleanerConfigurationImpl());
    val callback = new HtmlCrosstabRendererCallback(rendererFactory,renderingContext);
    val rowFactory = new InMemoryRowAnnotationFactory;
    val rowAnnotation = new RowAnnotationImpl;
    val row = new MockInputRow;
    row.put(new MockInputColumn("mock"), "mocktest");
    rowFactory.annotate(row, 1, rowAnnotation);

    callback.valueCell("nullResultProducer", null);
    callback.valueCell("emptyResultProducer", new DefaultResultProducer(null));
    callback.valueCell("singleResultProducer", new DefaultResultProducer(new AnnotatedRowsResult(rowAnnotation, rowFactory)));
    callback.valueCell("notAnnotatedResultProducer", new DefaultResultProducer(new NumberResult(new Integer(1))));
    
    Assert.assertEquals("<td class=\"value\">nullResultProducer</td><td class=\"value\">emptyResultProducer</td><td class=\"value\"><a class=\"drillToDetailsLink\" href=\"#\" onclick=\"drillToDetails('reselem_1'); return false;\">singleResultProducer</a></td><td class=\"value\"><a class=\"drillToDetailsLink\" href=\"#\" onclick=\"drillToDetails('reselem_2'); return false;\">notAnnotatedResultProducer</a></td>", callback.getResult().getBodyElements().get(2).toHtml(renderingContext));
  }
  
  @Test
  def testMultipleDimensions() = {
    // creates a crosstab of some metric (simply iterated for simplicity)
    // based on person characteristica, examplified with Region (EU and
    // USA), Age-group (children, teenagers and adult)
    // and Gender (male and female)

    val c = new Crosstab[java.lang.Integer](classOf[java.lang.Integer], "Region", "Age-group", "Gender", "Native");
    val genderValues = Array[String]("Male", "Female");
    val regionValues = Array[String]("EU", "USA");
    val ageGroupValues = Array[String]("Child", "Teenager", "Adult");
    val nativeValues = Array[String]("Yes", "No, immigrant", "No, second-generation");

    var i = 0;
    for (gender <- genderValues) {
      for (region <- regionValues) {
        for (ageGroup <- ageGroupValues) {
          for (nativeValue <- nativeValues) {
            c.where("Region", region).where("Age-group", ageGroup).where("Gender", gender)
              .where("Native", nativeValue).put(i, true);
            i = i + 1;
          }
        }
      }
    }

    val dimensionNames: Array[String] = c.getDimensionNames();
    Assert.assertEquals("Array(Region, Age-group, Gender, Native)", dimensionNames.deep.toString());

    val crosstabRenderer = new CrosstabRenderer(c);

    // auto-assigned axises
    Assert.assertEquals(
      "<table class='crosstabTable'><tr class='odd'><td class='empty'></td><td class='empty'></td><td class='crosstabHorizontalHeader' colspan='3'>EU</td><td class='crosstabHorizontalHeader' colspan='3'>USA</td></tr><tr class='even'><td class='empty'></td><td class='empty'></td><td class='crosstabHorizontalHeader'>Child</td><td class='crosstabHorizontalHeader'>Teenager</td><td class='crosstabHorizontalHeader'>Adult</td><td class='crosstabHorizontalHeader'>Child</td><td class='crosstabHorizontalHeader'>Teenager</td><td class='crosstabHorizontalHeader'>Adult</td></tr><tr class='odd'><td class='crosstabVerticalHeader' rowspan='3'>Male</td><td class='crosstabVerticalHeader'>Yes</td><td class='value'>0</td><td class='value'>3</td><td class='value'>6</td><td class='value'>9</td><td class='value'>12</td><td class='value'>15</td></tr><tr class='even'><td class='crosstabVerticalHeader'>No, immigrant</td><td class='value'>1</td><td class='value'>4</td><td class='value'>7</td><td class='value'>10</td><td class='value'>13</td><td class='value'>16</td></tr><tr class='odd'><td class='crosstabVerticalHeader'>No, second-generation</td><td class='value'>2</td><td class='value'>5</td><td class='value'>8</td><td class='value'>11</td><td class='value'>14</td><td class='value'>17</td></tr><tr class='even'><td class='crosstabVerticalHeader' rowspan='3'>Female</td><td class='crosstabVerticalHeader'>Yes</td><td class='value'>18</td><td class='value'>21</td><td class='value'>24</td><td class='value'>27</td><td class='value'>30</td><td class='value'>33</td></tr><tr class='odd'><td class='crosstabVerticalHeader'>No, immigrant</td><td class='value'>19</td><td class='value'>22</td><td class='value'>25</td><td class='value'>28</td><td class='value'>31</td><td class='value'>34</td></tr><tr class='even'><td class='crosstabVerticalHeader'>No, second-generation</td><td class='value'>20</td><td class='value'>23</td><td class='value'>26</td><td class='value'>29</td><td class='value'>32</td><td class='value'>35</td></tr></table>",
      crosstabRenderer.render(new HtmlCrosstabRendererCallback(null,renderingContext)).getBodyElements().get(0).toHtml(renderingContext)
        .replaceAll("\"", "'"));

    // try all vertical
    crosstabRenderer.makeVertical(c.getDimension(0));
    crosstabRenderer.makeVertical(c.getDimension(1));
    crosstabRenderer.makeVertical(c.getDimension(2));
    crosstabRenderer.makeVertical(c.getDimension(3));
    Assert.assertEquals(
      "<table class='crosstabTable'><tr class='odd'><td class='crosstabVerticalHeader' rowspan='18'>Male</td><td class='crosstabVerticalHeader' rowspan='6'>Yes</td><td class='crosstabVerticalHeader' rowspan='3'>EU</td><td class='crosstabVerticalHeader'>Child</td><td class='value'>0</td></tr><tr class='even'><td class='crosstabVerticalHeader'>Teenager</td><td class='value'>3</td></tr><tr class='odd'><td class='crosstabVerticalHeader'>Adult</td><td class='value'>6</td></tr><tr class='even'><td class='crosstabVerticalHeader' rowspan='3'>USA</td><td class='crosstabVerticalHeader'>Child</td><td class='value'>9</td></tr><tr class='odd'><td class='crosstabVerticalHeader'>Teenager</td><td class='value'>12</td></tr><tr class='even'><td class='crosstabVerticalHeader'>Adult</td><td class='value'>15</td></tr><tr class='odd'><td class='crosstabVerticalHeader' rowspan='6'>No, immigrant</td><td class='crosstabVerticalHeader' rowspan='3'>EU</td><td class='crosstabVerticalHeader'>Child</td><td class='value'>1</td></tr><tr class='even'><td class='crosstabVerticalHeader'>Teenager</td><td class='value'>4</td></tr><tr class='odd'><td class='crosstabVerticalHeader'>Adult</td><td class='value'>7</td></tr><tr class='even'><td class='crosstabVerticalHeader' rowspan='3'>USA</td><td class='crosstabVerticalHeader'>Child</td><td class='value'>10</td></tr><tr class='odd'><td class='crosstabVerticalHeader'>Teenager</td><td class='value'>13</td></tr><tr class='even'><td class='crosstabVerticalHeader'>Adult</td><td class='value'>16</td></tr><tr class='odd'><td class='crosstabVerticalHeader' rowspan='6'>No, second-generation</td><td class='crosstabVerticalHeader' rowspan='3'>EU</td><td class='crosstabVerticalHeader'>Child</td><td class='value'>2</td></tr><tr class='even'><td class='crosstabVerticalHeader'>Teenager</td><td class='value'>5</td></tr><tr class='odd'><td class='crosstabVerticalHeader'>Adult</td><td class='value'>8</td></tr><tr class='even'><td class='crosstabVerticalHeader' rowspan='3'>USA</td><td class='crosstabVerticalHeader'>Child</td><td class='value'>11</td></tr><tr class='odd'><td class='crosstabVerticalHeader'>Teenager</td><td class='value'>14</td></tr><tr class='even'><td class='crosstabVerticalHeader'>Adult</td><td class='value'>17</td></tr><tr class='odd'><td class='crosstabVerticalHeader' rowspan='18'>Female</td><td class='crosstabVerticalHeader' rowspan='6'>Yes</td><td class='crosstabVerticalHeader' rowspan='3'>EU</td><td class='crosstabVerticalHeader'>Child</td><td class='value'>18</td></tr><tr class='even'><td class='crosstabVerticalHeader'>Teenager</td><td class='value'>21</td></tr><tr class='odd'><td class='crosstabVerticalHeader'>Adult</td><td class='value'>24</td></tr><tr class='even'><td class='crosstabVerticalHeader' rowspan='3'>USA</td><td class='crosstabVerticalHeader'>Child</td><td class='value'>27</td></tr><tr class='odd'><td class='crosstabVerticalHeader'>Teenager</td><td class='value'>30</td></tr><tr class='even'><td class='crosstabVerticalHeader'>Adult</td><td class='value'>33</td></tr><tr class='odd'><td class='crosstabVerticalHeader' rowspan='6'>No, immigrant</td><td class='crosstabVerticalHeader' rowspan='3'>EU</td><td class='crosstabVerticalHeader'>Child</td><td class='value'>19</td></tr><tr class='even'><td class='crosstabVerticalHeader'>Teenager</td><td class='value'>22</td></tr><tr class='odd'><td class='crosstabVerticalHeader'>Adult</td><td class='value'>25</td></tr><tr class='even'><td class='crosstabVerticalHeader' rowspan='3'>USA</td><td class='crosstabVerticalHeader'>Child</td><td class='value'>28</td></tr><tr class='odd'><td class='crosstabVerticalHeader'>Teenager</td><td class='value'>31</td></tr><tr class='even'><td class='crosstabVerticalHeader'>Adult</td><td class='value'>34</td></tr><tr class='odd'><td class='crosstabVerticalHeader' rowspan='6'>No, second-generation</td><td class='crosstabVerticalHeader' rowspan='3'>EU</td><td class='crosstabVerticalHeader'>Child</td><td class='value'>20</td></tr><tr class='even'><td class='crosstabVerticalHeader'>Teenager</td><td class='value'>23</td></tr><tr class='odd'><td class='crosstabVerticalHeader'>Adult</td><td class='value'>26</td></tr><tr class='even'><td class='crosstabVerticalHeader' rowspan='3'>USA</td><td class='crosstabVerticalHeader'>Child</td><td class='value'>29</td></tr><tr class='odd'><td class='crosstabVerticalHeader'>Teenager</td><td class='value'>32</td></tr><tr class='even'><td class='crosstabVerticalHeader'>Adult</td><td class='value'>35</td></tr></table>",
      crosstabRenderer.render(new HtmlCrosstabRendererCallback(null,renderingContext)).getBodyElements().get(0).toHtml(renderingContext)
        .replaceAll("\"", "'"));
  }
}
