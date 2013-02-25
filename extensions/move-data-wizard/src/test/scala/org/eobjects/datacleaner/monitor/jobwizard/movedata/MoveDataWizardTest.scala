package org.eobjects.datacleaner.monitor.jobwizard.movedata
import scala.collection.JavaConversions._

import org.easymock.EasyMock
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl
import org.eobjects.analyzer.connection.DatastoreCatalogImpl
import org.eobjects.analyzer.test.TestHelper
import org.eobjects.datacleaner.monitor.configuration.TenantContext
import org.eobjects.datacleaner.monitor.server.JobWizardContextImpl
import org.junit.Assert._
import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit

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
                    <option value="PUBLIC.CUSTOMERS">CUSTOMERS</option>
                    <option value="PUBLIC.CUSTOMER_W_TER">CUSTOMER_W_TER</option>
                    <option value="PUBLIC.DEPARTMENT_MANAGERS">DEPARTMENT_MANAGERS</option>
                    <option value="PUBLIC.DIM_TIME">DIM_TIME</option>
                    <option value="PUBLIC.EMPLOYEES">EMPLOYEES</option>
                    <option value="PUBLIC.OFFICES">OFFICES</option>
                    <option value="PUBLIC.ORDERDETAILS">ORDERDETAILS</option>
                    <option value="PUBLIC.ORDERFACT">ORDERFACT</option>
                    <option value="PUBLIC.ORDERS">ORDERS</option>
                    <option value="PUBLIC.PAYMENTS">PAYMENTS</option>
                    <option value="PUBLIC.PRODUCTS">PRODUCTS</option>
                    <option value="PUBLIC.QUADRANT_ACTUALS">QUADRANT_ACTUALS</option>
                    <option value="PUBLIC.TRIAL_BALANCE">TRIAL_BALANCE</option>
            </optgroup>
    </select>
</div>"""), normalize(page1.getFormInnerHtml()));

    val formParams1: Map[String, java.util.List[String]] = Map("tableName" -> List("PUBLIC.CUSTOMERS"));

    val page2 = page1.nextPageController(formParams1);

    assertEquals(normalize("""<div>
    <p>Please select the target datastore to write to:</p>
    <select name="datastoreName">
                    <option value="orderdb">orderdb</option>
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
                    <option value="PUBLIC.CUSTOMERS">CUSTOMERS</option>
                    <option value="PUBLIC.CUSTOMER_W_TER">CUSTOMER_W_TER</option>
                    <option value="PUBLIC.DEPARTMENT_MANAGERS">DEPARTMENT_MANAGERS</option>
                    <option value="PUBLIC.DIM_TIME">DIM_TIME</option>
                    <option value="PUBLIC.EMPLOYEES">EMPLOYEES</option>
                    <option value="PUBLIC.OFFICES">OFFICES</option>
                    <option value="PUBLIC.ORDERDETAILS">ORDERDETAILS</option>
                    <option value="PUBLIC.ORDERFACT">ORDERFACT</option>
                    <option value="PUBLIC.ORDERS">ORDERS</option>
                    <option value="PUBLIC.PAYMENTS">PAYMENTS</option>
                    <option value="PUBLIC.PRODUCTS">PRODUCTS</option>
                    <option value="PUBLIC.QUADRANT_ACTUALS">QUADRANT_ACTUALS</option>
                    <option value="PUBLIC.TRIAL_BALANCE">TRIAL_BALANCE</option>
            </optgroup>
    </select>
</div>"""), normalize(page3.getFormInnerHtml()));

    val formParams3: Map[String, java.util.List[String]] = Map("tableName" -> List("PUBLIC.EMPLOYEES"));

    val page4 = page3.nextPageController(formParams3);

    assertEquals(normalize("""<div>
    <p>Please map source and target columns:</p>

    <table>
        <tr>
            <th class="center">ID?</th>
            <th>Source</th>
            <th>Target</th>
        </tr>
                <tr>
            <td class="center">
                <input type="checkbox" id="checkbox_input_id_0" name="id_0" value="true" checked="checked" />
            </td>
            <td>
                <label for="checkbox_input_id_0">CUSTOMERNUMBER</label>
            </td>
            <td>
                <select name="mapping_0">
                    <option label="- none -" value=""></option>
                        <option value="EMPLOYEENUMBER">EMPLOYEENUMBER</option>
                        <option value="LASTNAME">LASTNAME</option>
                        <option value="FIRSTNAME">FIRSTNAME</option>
                        <option value="EXTENSION">EXTENSION</option>
                        <option value="EMAIL">EMAIL</option>
                        <option value="OFFICECODE">OFFICECODE</option>
                        <option value="REPORTSTO">REPORTSTO</option>
                        <option value="JOBTITLE">JOBTITLE</option>
                </select>
            </td>
        </tr>
        <tr>
            <td class="center">
                <input type="checkbox" name="id_1" value="true" />
            </td>
            <td>
                <label for="checkbox_input_id_1">CUSTOMERNAME</label>
            </td>
            <td>
                <select name="mapping_1">
                    <option label="- none -" value=""></option>
                        <option value="EMPLOYEENUMBER">EMPLOYEENUMBER</option>
                        <option value="LASTNAME">LASTNAME</option>
                        <option value="FIRSTNAME">FIRSTNAME</option>
                        <option value="EXTENSION">EXTENSION</option>
                        <option value="EMAIL">EMAIL</option>
                        <option value="OFFICECODE">OFFICECODE</option>
                        <option value="REPORTSTO">REPORTSTO</option>
                        <option value="JOBTITLE">JOBTITLE</option>
                </select>
            </td>
        </tr>
        <tr>
            <td class="center">
                <input type="checkbox" name="id_2" value="true" />
            </td>
            <td>
                <label for="checkbox_input_id_2">CONTACTLASTNAME</label>
            </td>
            <td>
                <select name="mapping_2">
                    <option label="- none -" value=""></option>
                        <option value="EMPLOYEENUMBER">EMPLOYEENUMBER</option>
                        <option value="LASTNAME">LASTNAME</option>
                        <option value="FIRSTNAME">FIRSTNAME</option>
                        <option value="EXTENSION">EXTENSION</option>
                        <option value="EMAIL">EMAIL</option>
                        <option value="OFFICECODE">OFFICECODE</option>
                        <option value="REPORTSTO">REPORTSTO</option>
                        <option value="JOBTITLE">JOBTITLE</option>
                </select>
            </td>
        </tr>
        <tr>
            <td class="center">
                <input type="checkbox" name="id_3" value="true" />
            </td>
            <td>
                <label for="checkbox_input_id_3">CONTACTFIRSTNAME</label>
            </td>
            <td>
                <select name="mapping_3">
                    <option label="- none -" value=""></option>
                        <option value="EMPLOYEENUMBER">EMPLOYEENUMBER</option>
                        <option value="LASTNAME">LASTNAME</option>
                        <option value="FIRSTNAME">FIRSTNAME</option>
                        <option value="EXTENSION">EXTENSION</option>
                        <option value="EMAIL">EMAIL</option>
                        <option value="OFFICECODE">OFFICECODE</option>
                        <option value="REPORTSTO">REPORTSTO</option>
                        <option value="JOBTITLE">JOBTITLE</option>
                </select>
            </td>
        </tr>
        <tr>
            <td class="center">
                <input type="checkbox" name="id_4" value="true" />
            </td>
            <td>
                <label for="checkbox_input_id_4">PHONE</label>
            </td>
            <td>
                <select name="mapping_4">
                    <option label="- none -" value=""></option>
                        <option value="EMPLOYEENUMBER">EMPLOYEENUMBER</option>
                        <option value="LASTNAME">LASTNAME</option>
                        <option value="FIRSTNAME">FIRSTNAME</option>
                        <option value="EXTENSION">EXTENSION</option>
                        <option value="EMAIL">EMAIL</option>
                        <option value="OFFICECODE">OFFICECODE</option>
                        <option value="REPORTSTO">REPORTSTO</option>
                        <option value="JOBTITLE">JOBTITLE</option>
                </select>
            </td>
        </tr>
        <tr>
            <td class="center">
                <input type="checkbox" name="id_5" value="true" />
            </td>
            <td>
                <label for="checkbox_input_id_5">ADDRESSLINE1</label>
            </td>
            <td>
                <select name="mapping_5">
                    <option label="- none -" value=""></option>
                        <option value="EMPLOYEENUMBER">EMPLOYEENUMBER</option>
                        <option value="LASTNAME">LASTNAME</option>
                        <option value="FIRSTNAME">FIRSTNAME</option>
                        <option value="EXTENSION">EXTENSION</option>
                        <option value="EMAIL">EMAIL</option>
                        <option value="OFFICECODE">OFFICECODE</option>
                        <option value="REPORTSTO">REPORTSTO</option>
                        <option value="JOBTITLE">JOBTITLE</option>
                </select>
            </td>
        </tr>
        <tr>
            <td class="center">
                <input type="checkbox" name="id_6" value="true" />
            </td>
            <td>
                <label for="checkbox_input_id_6">ADDRESSLINE2</label>
            </td>
            <td>
                <select name="mapping_6">
                    <option label="- none -" value=""></option>
                        <option value="EMPLOYEENUMBER">EMPLOYEENUMBER</option>
                        <option value="LASTNAME">LASTNAME</option>
                        <option value="FIRSTNAME">FIRSTNAME</option>
                        <option value="EXTENSION">EXTENSION</option>
                        <option value="EMAIL">EMAIL</option>
                        <option value="OFFICECODE">OFFICECODE</option>
                        <option value="REPORTSTO">REPORTSTO</option>
                        <option value="JOBTITLE">JOBTITLE</option>
                </select>
            </td>
        </tr>
        <tr>
            <td class="center">
                <input type="checkbox" name="id_7" value="true" />
            </td>
            <td>
                <label for="checkbox_input_id_7">CITY</label>
            </td>
            <td>
                <select name="mapping_7">
                    <option label="- none -" value=""></option>
                        <option value="EMPLOYEENUMBER">EMPLOYEENUMBER</option>
                        <option value="LASTNAME">LASTNAME</option>
                        <option value="FIRSTNAME">FIRSTNAME</option>
                        <option value="EXTENSION">EXTENSION</option>
                        <option value="EMAIL">EMAIL</option>
                        <option value="OFFICECODE">OFFICECODE</option>
                        <option value="REPORTSTO">REPORTSTO</option>
                        <option value="JOBTITLE">JOBTITLE</option>
                </select>
            </td>
        </tr>
        <tr>
            <td class="center">
                <input type="checkbox" name="id_8" value="true" />
            </td>
            <td>
                <label for="checkbox_input_id_8">STATE</label>
            </td>
            <td>
                <select name="mapping_8">
                    <option label="- none -" value=""></option>
                        <option value="EMPLOYEENUMBER">EMPLOYEENUMBER</option>
                        <option value="LASTNAME">LASTNAME</option>
                        <option value="FIRSTNAME">FIRSTNAME</option>
                        <option value="EXTENSION">EXTENSION</option>
                        <option value="EMAIL">EMAIL</option>
                        <option value="OFFICECODE">OFFICECODE</option>
                        <option value="REPORTSTO">REPORTSTO</option>
                        <option value="JOBTITLE">JOBTITLE</option>
                </select>
            </td>
        </tr>
        <tr>
            <td class="center">
                <input type="checkbox" name="id_9" value="true" />
            </td>
            <td>
                <label for="checkbox_input_id_9">POSTALCODE</label>
            </td>
            <td>
                <select name="mapping_9">
                    <option label="- none -" value=""></option>
                        <option value="EMPLOYEENUMBER">EMPLOYEENUMBER</option>
                        <option value="LASTNAME">LASTNAME</option>
                        <option value="FIRSTNAME">FIRSTNAME</option>
                        <option value="EXTENSION">EXTENSION</option>
                        <option value="EMAIL">EMAIL</option>
                        <option value="OFFICECODE">OFFICECODE</option>
                        <option value="REPORTSTO">REPORTSTO</option>
                        <option value="JOBTITLE">JOBTITLE</option>
                </select>
            </td>
        </tr>
        <tr>
            <td class="center">
                <input type="checkbox" name="id_10" value="true" />
            </td>
            <td>
                <label for="checkbox_input_id_10">COUNTRY</label>
            </td>
            <td>
                <select name="mapping_10">
                    <option label="- none -" value=""></option>
                        <option value="EMPLOYEENUMBER">EMPLOYEENUMBER</option>
                        <option value="LASTNAME">LASTNAME</option>
                        <option value="FIRSTNAME">FIRSTNAME</option>
                        <option value="EXTENSION">EXTENSION</option>
                        <option value="EMAIL">EMAIL</option>
                        <option value="OFFICECODE">OFFICECODE</option>
                        <option value="REPORTSTO">REPORTSTO</option>
                        <option value="JOBTITLE">JOBTITLE</option>
                </select>
            </td>
        </tr>
        <tr>
            <td class="center">
                <input type="checkbox" name="id_11" value="true" />
            </td>
            <td>
                <label for="checkbox_input_id_11">SALESREPEMPLOYEENUMBER</label>
            </td>
            <td>
                <select name="mapping_11">
                    <option label="- none -" value=""></option>
                        <option value="EMPLOYEENUMBER">EMPLOYEENUMBER</option>
                        <option value="LASTNAME">LASTNAME</option>
                        <option value="FIRSTNAME">FIRSTNAME</option>
                        <option value="EXTENSION">EXTENSION</option>
                        <option value="EMAIL">EMAIL</option>
                        <option value="OFFICECODE">OFFICECODE</option>
                        <option value="REPORTSTO">REPORTSTO</option>
                        <option value="JOBTITLE">JOBTITLE</option>
                </select>
            </td>
        </tr>
        <tr>
            <td class="center">
                <input type="checkbox" name="id_12" value="true" />
            </td>
            <td>
                <label for="checkbox_input_id_12">CREDITLIMIT</label>
            </td>
            <td>
                <select name="mapping_12">
                    <option label="- none -" value=""></option>
                        <option value="EMPLOYEENUMBER">EMPLOYEENUMBER</option>
                        <option value="LASTNAME">LASTNAME</option>
                        <option value="FIRSTNAME">FIRSTNAME</option>
                        <option value="EXTENSION">EXTENSION</option>
                        <option value="EMAIL">EMAIL</option>
                        <option value="OFFICECODE">OFFICECODE</option>
                        <option value="REPORTSTO">REPORTSTO</option>
                        <option value="JOBTITLE">JOBTITLE</option>
                </select>
            </td>
        </tr>
    </table>
        
    <p>
        <input id="move_data_update_primary_keys" type="checkbox" name="updatePrimaryKeys" value="true" checked="checked" />
        <label for="move_data_update_primary_keys">Use UPDATEs when primary key already exists?</label>
    </p>
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