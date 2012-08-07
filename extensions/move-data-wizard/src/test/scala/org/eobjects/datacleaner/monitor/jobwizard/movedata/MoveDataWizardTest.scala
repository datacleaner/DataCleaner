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
    val datastore = new MockUpdateableDatastore(TestHelper.createSampleDatabaseDatastore("orderdb"));

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

    val formParams2: Map[String, java.util.List[String]] = Map("datastoreName" -> List("orderdb"));

    val page3 = page2.nextPageController(formParams2);

    assertEquals(normalize("""<div>
    <p>Select the target table to write to:</p>
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
</div>"""), normalize(page3.getFormInnerHtml()));

    val formParams3: Map[String, java.util.List[String]] = Map("tableName" -> List("PUBLIC.EMPLOYEES"));

    val page4 = page3.nextPageController(formParams3);

    assertEquals(normalize("""<div>
    <div>
        <input id="move_data_update_primary_keys" type="checkbox" name="updatePrimaryKeys" value="true" checked="checked" />
        <label for="move_data_update_primary_keys">Use UPDATEs when primary key already exists?</label>
    </div>

    <table>
        <tr>
            <th>ID?</th>
            <th>Source</th>
            <th>Target</th>
        </tr>
                <tr>
            <td>
                <input type="checkbox" name="id_0" value="true" checked="checked" />
            </td>
            <td>
                CUSTOMERNUMBER
            </td>
            <td>
                <select name="mapping_0">
                    <option label="- none -" value="">
                        <option label="EMPLOYEENUMBER" value="EMPLOYEENUMBER">
                        <option label="LASTNAME" value="LASTNAME">
                        <option label="FIRSTNAME" value="FIRSTNAME">
                        <option label="EXTENSION" value="EXTENSION">
                        <option label="EMAIL" value="EMAIL">
                        <option label="OFFICECODE" value="OFFICECODE">
                        <option label="REPORTSTO" value="REPORTSTO">
                        <option label="JOBTITLE" value="JOBTITLE">
                </select>
            </td>
        </tr>
        <tr>
            <td>
                <input type="checkbox" name="id_1" value="true" />
            </td>
            <td>
                CUSTOMERNAME
            </td>
            <td>
                <select name="mapping_1">
                    <option label="- none -" value="">
                        <option label="EMPLOYEENUMBER" value="EMPLOYEENUMBER">
                        <option label="LASTNAME" value="LASTNAME">
                        <option label="FIRSTNAME" value="FIRSTNAME">
                        <option label="EXTENSION" value="EXTENSION">
                        <option label="EMAIL" value="EMAIL">
                        <option label="OFFICECODE" value="OFFICECODE">
                        <option label="REPORTSTO" value="REPORTSTO">
                        <option label="JOBTITLE" value="JOBTITLE">
                </select>
            </td>
        </tr>
        <tr>
            <td>
                <input type="checkbox" name="id_2" value="true" />
            </td>
            <td>
                CONTACTLASTNAME
            </td>
            <td>
                <select name="mapping_2">
                    <option label="- none -" value="">
                        <option label="EMPLOYEENUMBER" value="EMPLOYEENUMBER">
                        <option label="LASTNAME" value="LASTNAME">
                        <option label="FIRSTNAME" value="FIRSTNAME">
                        <option label="EXTENSION" value="EXTENSION">
                        <option label="EMAIL" value="EMAIL">
                        <option label="OFFICECODE" value="OFFICECODE">
                        <option label="REPORTSTO" value="REPORTSTO">
                        <option label="JOBTITLE" value="JOBTITLE">
                </select>
            </td>
        </tr>
        <tr>
            <td>
                <input type="checkbox" name="id_3" value="true" />
            </td>
            <td>
                CONTACTFIRSTNAME
            </td>
            <td>
                <select name="mapping_3">
                    <option label="- none -" value="">
                        <option label="EMPLOYEENUMBER" value="EMPLOYEENUMBER">
                        <option label="LASTNAME" value="LASTNAME">
                        <option label="FIRSTNAME" value="FIRSTNAME">
                        <option label="EXTENSION" value="EXTENSION">
                        <option label="EMAIL" value="EMAIL">
                        <option label="OFFICECODE" value="OFFICECODE">
                        <option label="REPORTSTO" value="REPORTSTO">
                        <option label="JOBTITLE" value="JOBTITLE">
                </select>
            </td>
        </tr>
        <tr>
            <td>
                <input type="checkbox" name="id_4" value="true" />
            </td>
            <td>
                PHONE
            </td>
            <td>
                <select name="mapping_4">
                    <option label="- none -" value="">
                        <option label="EMPLOYEENUMBER" value="EMPLOYEENUMBER">
                        <option label="LASTNAME" value="LASTNAME">
                        <option label="FIRSTNAME" value="FIRSTNAME">
                        <option label="EXTENSION" value="EXTENSION">
                        <option label="EMAIL" value="EMAIL">
                        <option label="OFFICECODE" value="OFFICECODE">
                        <option label="REPORTSTO" value="REPORTSTO">
                        <option label="JOBTITLE" value="JOBTITLE">
                </select>
            </td>
        </tr>
        <tr>
            <td>
                <input type="checkbox" name="id_5" value="true" />
            </td>
            <td>
                ADDRESSLINE1
            </td>
            <td>
                <select name="mapping_5">
                    <option label="- none -" value="">
                        <option label="EMPLOYEENUMBER" value="EMPLOYEENUMBER">
                        <option label="LASTNAME" value="LASTNAME">
                        <option label="FIRSTNAME" value="FIRSTNAME">
                        <option label="EXTENSION" value="EXTENSION">
                        <option label="EMAIL" value="EMAIL">
                        <option label="OFFICECODE" value="OFFICECODE">
                        <option label="REPORTSTO" value="REPORTSTO">
                        <option label="JOBTITLE" value="JOBTITLE">
                </select>
            </td>
        </tr>
        <tr>
            <td>
                <input type="checkbox" name="id_6" value="true" />
            </td>
            <td>
                ADDRESSLINE2
            </td>
            <td>
                <select name="mapping_6">
                    <option label="- none -" value="">
                        <option label="EMPLOYEENUMBER" value="EMPLOYEENUMBER">
                        <option label="LASTNAME" value="LASTNAME">
                        <option label="FIRSTNAME" value="FIRSTNAME">
                        <option label="EXTENSION" value="EXTENSION">
                        <option label="EMAIL" value="EMAIL">
                        <option label="OFFICECODE" value="OFFICECODE">
                        <option label="REPORTSTO" value="REPORTSTO">
                        <option label="JOBTITLE" value="JOBTITLE">
                </select>
            </td>
        </tr>
        <tr>
            <td>
                <input type="checkbox" name="id_7" value="true" />
            </td>
            <td>
                CITY
            </td>
            <td>
                <select name="mapping_7">
                    <option label="- none -" value="">
                        <option label="EMPLOYEENUMBER" value="EMPLOYEENUMBER">
                        <option label="LASTNAME" value="LASTNAME">
                        <option label="FIRSTNAME" value="FIRSTNAME">
                        <option label="EXTENSION" value="EXTENSION">
                        <option label="EMAIL" value="EMAIL">
                        <option label="OFFICECODE" value="OFFICECODE">
                        <option label="REPORTSTO" value="REPORTSTO">
                        <option label="JOBTITLE" value="JOBTITLE">
                </select>
            </td>
        </tr>
        <tr>
            <td>
                <input type="checkbox" name="id_8" value="true" />
            </td>
            <td>
                STATE
            </td>
            <td>
                <select name="mapping_8">
                    <option label="- none -" value="">
                        <option label="EMPLOYEENUMBER" value="EMPLOYEENUMBER">
                        <option label="LASTNAME" value="LASTNAME">
                        <option label="FIRSTNAME" value="FIRSTNAME">
                        <option label="EXTENSION" value="EXTENSION">
                        <option label="EMAIL" value="EMAIL">
                        <option label="OFFICECODE" value="OFFICECODE">
                        <option label="REPORTSTO" value="REPORTSTO">
                        <option label="JOBTITLE" value="JOBTITLE">
                </select>
            </td>
        </tr>
        <tr>
            <td>
                <input type="checkbox" name="id_9" value="true" />
            </td>
            <td>
                POSTALCODE
            </td>
            <td>
                <select name="mapping_9">
                    <option label="- none -" value="">
                        <option label="EMPLOYEENUMBER" value="EMPLOYEENUMBER">
                        <option label="LASTNAME" value="LASTNAME">
                        <option label="FIRSTNAME" value="FIRSTNAME">
                        <option label="EXTENSION" value="EXTENSION">
                        <option label="EMAIL" value="EMAIL">
                        <option label="OFFICECODE" value="OFFICECODE">
                        <option label="REPORTSTO" value="REPORTSTO">
                        <option label="JOBTITLE" value="JOBTITLE">
                </select>
            </td>
        </tr>
        <tr>
            <td>
                <input type="checkbox" name="id_10" value="true" />
            </td>
            <td>
                COUNTRY
            </td>
            <td>
                <select name="mapping_10">
                    <option label="- none -" value="">
                        <option label="EMPLOYEENUMBER" value="EMPLOYEENUMBER">
                        <option label="LASTNAME" value="LASTNAME">
                        <option label="FIRSTNAME" value="FIRSTNAME">
                        <option label="EXTENSION" value="EXTENSION">
                        <option label="EMAIL" value="EMAIL">
                        <option label="OFFICECODE" value="OFFICECODE">
                        <option label="REPORTSTO" value="REPORTSTO">
                        <option label="JOBTITLE" value="JOBTITLE">
                </select>
            </td>
        </tr>
        <tr>
            <td>
                <input type="checkbox" name="id_11" value="true" />
            </td>
            <td>
                SALESREPEMPLOYEENUMBER
            </td>
            <td>
                <select name="mapping_11">
                    <option label="- none -" value="">
                        <option label="EMPLOYEENUMBER" value="EMPLOYEENUMBER">
                        <option label="LASTNAME" value="LASTNAME">
                        <option label="FIRSTNAME" value="FIRSTNAME">
                        <option label="EXTENSION" value="EXTENSION">
                        <option label="EMAIL" value="EMAIL">
                        <option label="OFFICECODE" value="OFFICECODE">
                        <option label="REPORTSTO" value="REPORTSTO">
                        <option label="JOBTITLE" value="JOBTITLE">
                </select>
            </td>
        </tr>
        <tr>
            <td>
                <input type="checkbox" name="id_12" value="true" />
            </td>
            <td>
                CREDITLIMIT
            </td>
            <td>
                <select name="mapping_12">
                    <option label="- none -" value="">
                        <option label="EMPLOYEENUMBER" value="EMPLOYEENUMBER">
                        <option label="LASTNAME" value="LASTNAME">
                        <option label="FIRSTNAME" value="FIRSTNAME">
                        <option label="EXTENSION" value="EXTENSION">
                        <option label="EMAIL" value="EMAIL">
                        <option label="OFFICECODE" value="OFFICECODE">
                        <option label="REPORTSTO" value="REPORTSTO">
                        <option label="JOBTITLE" value="JOBTITLE">
                </select>
            </td>
        </tr>
    </table>
</div>"""), normalize(page4.getFormInnerHtml()));

    // map EMPLOYEENUMBER to CUSTOMERNUMBER and contact name to employee name
    val formParams4: Map[String, java.util.List[String]] = Map("updatePrimaryKeys" -> List("true"), "id_0" -> List("true"), "mapping_0" -> List("EMPLOYEENUMBER"), "mapping_2" -> List("LASTNAME"), "mapping_3" -> List("FIRSTNAME"));
    
    val page5 = page4.nextPageController(formParams4)
    assertNull(page5);
    
    val job = session.createJob()
    assertNotNull(job);
    
    val columnNames = job.getSourceColumns().map(_.getName());
    assertEquals("CUSTOMERNUMBER,CONTACTLASTNAME,CONTACTFIRSTNAME", columnNames.mkString(","));
    
    val analyzerNames = job.getAnalyzerJobBuilders().map(_.getDescriptor().getDisplayName())
    assertEquals("Insert into table,Update table", analyzerNames.mkString(","));
    
    val transformerNames = job.getTransformerJobBuilders().map(_.getDescriptor().getDisplayName())
    assertEquals("Table lookup", transformerNames.mkString(","));
    
    val filterNames = job.getFilterJobBuilders().map(_.getDescriptor().getDisplayName())
    assertEquals("Null check", filterNames.mkString(","));
    
    val configured = job.isConfigured(true)
    assertTrue(configured);
  }

  def normalize(str: String): String = {
    return str.replaceAll("\r\n", "\n").replaceAll("\t", "    ");
  }
}