package org.eobjects.datacleaner.monitor.jobwizard.movedata
import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit
import org.eobjects.datacleaner.monitor.jobwizard.api.JobWizardContext
import org.eobjects.datacleaner.monitor.server.JobWizardContextImpl
import org.eobjects.analyzer.test.TestHelper
import org.easymock.EasyMock
import org.eobjects.datacleaner.monitor.configuration.TenantContext
import org.junit.Assert._
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl
import scala.collection.JavaConversions._
import org.eobjects.analyzer.connection.DatastoreCatalogImpl

class MoveDataWizardTest extends AssertionsForJUnit {

  @Test
  def testScenario(): Unit = {
    val datastore = TestHelper.createSampleDatabaseDatastore("orderdb");

    val tenantContext = EasyMock.createMock(classOf[TenantContext]);
    val configuration = new AnalyzerBeansConfigurationImpl().replace(new DatastoreCatalogImpl(datastore));
    EasyMock.expect(tenantContext.getConfiguration()).andReturn(configuration).anyTimes();

    EasyMock.replay(tenantContext);

    val ctx = new JobWizardContextImpl(tenantContext, datastore, "My job");

    val session = new MoveDataWizard().start(ctx);

    assertNotNull(session);

    assertEquals(4, session.getPageCount());

    val page1 = session.firstPageController()
    assertEquals(normalize("""<div>
    <p>Please select the source table of the job:</p>
    <select name="tableName">
                    <optgroup label="INFORMATION_SCHEMA">
            </optgroup>
            <optgroup label="PUBLIC">
                    <option label="CUSTOMERS" value="PUBLIC.CUSTOMERS">
                    <option label="EMPLOYEES" value="PUBLIC.EMPLOYEES">
                    <option label="OFFICES" value="PUBLIC.OFFICES">
                    <option label="ORDERDETAILS" value="PUBLIC.ORDERDETAILS">
                    <option label="ORDERFACT" value="PUBLIC.ORDERFACT">
                    <option label="ORDERS" value="PUBLIC.ORDERS">
                    <option label="PAYMENTS" value="PUBLIC.PAYMENTS">
                    <option label="PRODUCTS" value="PUBLIC.PRODUCTS">
            </optgroup>
    </select>
</div>"""), normalize(page1.getFormInnerHtml()));

    val formParams1: Map[String, java.util.List[String]] = Map("tableName" -> List("PUBLIC.CUSTOMERS"));

    val page2 = page1.nextPageController(formParams1);

    assertEquals(normalize("""<div>
    <p>Please select the target datastore to write to:</p>
    <select name="datastoreName">
                    <option label="orderdb" value="orderdb">
    </select>
</div>"""), normalize(page2.getFormInnerHtml()));
  }

  def normalize(str: String): String = {
    return str.replaceAll("\r\n", "\n").replaceAll("\t", "    ");
  }
}