package org.datacleaner.monitor.jobwizard.movedata
import scala.collection.JavaConversions._

import org.easymock.EasyMock
import org.datacleaner.configuration.AnalyzerBeansConfigurationImpl
import org.datacleaner.connection.DatastoreCatalogImpl
import org.datacleaner.test.TestHelper
import org.datacleaner.monitor.configuration.TenantContext
import org.datacleaner.monitor.server.JobWizardContextImpl
import org.junit.Assert._
import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit

class CopyDataWizardTest extends AssertionsForJUnit {

  @Test
  def testScenario(): Unit = {
    val datastore = new MockUpdateableDatastore(TestHelper.createSampleDatabaseDatastore("orderdb"));

    val tenantContext = EasyMock.createMock(classOf[TenantContext]);
    val configuration = new AnalyzerBeansConfigurationImpl().replace(new DatastoreCatalogImpl(datastore));
    EasyMock.expect(tenantContext.getConfiguration()).andReturn(configuration).anyTimes();
    EasyMock.expect(tenantContext.containsJob("copy_data_job")).andReturn(false);
    EasyMock.replay(tenantContext);

    val wizard = new CopyDataWizard();

    val ctx = new JobWizardContextImpl(wizard, tenantContext, datastore, null, null);

    val session = new CopyDataWizard().start(ctx);

    assertNotNull(session);

    assertEquals(6, session.getPageCount());

    val page1 = session.firstPageController()
    assertEquals(normalize("""<div>
    <p>Please select the source table of the job:</p>
    <select name="tableName">
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
    <p>Please map source columns to their targets:</p>

    <table>
        <tr>
            <th>Source</th>
            <th>Target</th>
        </tr>
                <tr>
            <td>
                <label for="checkbox_input_id_0">CUSTOMERNUMBER</label>
            </td>
            <td>
                <select name="mapping_0">
                    <option label="- not copied -" value=""></option>
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
            <td>
                <label for="checkbox_input_id_1">CUSTOMERNAME</label>
            </td>
            <td>
                <select name="mapping_1">
                    <option label="- not copied -" value=""></option>
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
            <td>
                <label for="checkbox_input_id_2">CONTACTLASTNAME</label>
            </td>
            <td>
                <select name="mapping_2">
                    <option label="- not copied -" value=""></option>
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
            <td>
                <label for="checkbox_input_id_3">CONTACTFIRSTNAME</label>
            </td>
            <td>
                <select name="mapping_3">
                    <option label="- not copied -" value=""></option>
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
            <td>
                <label for="checkbox_input_id_4">PHONE</label>
            </td>
            <td>
                <select name="mapping_4">
                    <option label="- not copied -" value=""></option>
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
            <td>
                <label for="checkbox_input_id_5">ADDRESSLINE1</label>
            </td>
            <td>
                <select name="mapping_5">
                    <option label="- not copied -" value=""></option>
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
            <td>
                <label for="checkbox_input_id_6">ADDRESSLINE2</label>
            </td>
            <td>
                <select name="mapping_6">
                    <option label="- not copied -" value=""></option>
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
            <td>
                <label for="checkbox_input_id_7">CITY</label>
            </td>
            <td>
                <select name="mapping_7">
                    <option label="- not copied -" value=""></option>
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
            <td>
                <label for="checkbox_input_id_8">STATE</label>
            </td>
            <td>
                <select name="mapping_8">
                    <option label="- not copied -" value=""></option>
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
            <td>
                <label for="checkbox_input_id_9">POSTALCODE</label>
            </td>
            <td>
                <select name="mapping_9">
                    <option label="- not copied -" value=""></option>
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
            <td>
                <label for="checkbox_input_id_10">COUNTRY</label>
            </td>
            <td>
                <select name="mapping_10">
                    <option label="- not copied -" value=""></option>
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
            <td>
                <label for="checkbox_input_id_11">SALESREPEMPLOYEENUMBER</label>
            </td>
            <td>
                <select name="mapping_11">
                    <option label="- not copied -" value=""></option>
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
            <td>
                <label for="checkbox_input_id_12">CREDITLIMIT</label>
            </td>
            <td>
                <select name="mapping_12">
                    <option label="- not copied -" value=""></option>
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
</div>"""), normalize(page4.getFormInnerHtml()));

    // map EMPLOYEENUMBER to CUSTOMERNUMBER and contact name to employee name
    val formParams4: Map[String, java.util.List[String]] = Map("updatePrimaryKeys" -> List("true"), "id_0" -> List("true"), "mapping_0" -> List("EMPLOYEENUMBER"), "mapping_2" -> List("LASTNAME"), "mapping_3" -> List("FIRSTNAME"));

    val page5 = page4.nextPageController(formParams4)

    assertEquals(normalize("""<div>
    <p>How should the job handle updates to data over time?</p>

    <input type="radio" name="update_strategy" value="truncate"
        checked="checked" id="update_strategy_checkbox_truncate" /> <label class="blue"
        for="update_strategy_checkbox_truncate">Truncate target table</label>
    <div style="margin: 20px; margin-top: 4px;">
        <p>Using this option you truncate (delete all existing records)
            the target table every time, before inserting the records from the
            source table.</p>
    </div>

    <input type="radio" name="update_strategy" value="lookup_and_update"
        id="update_strategy_checkbox_lookup_and_update" /> <label class="blue"
        for="update_strategy_checkbox_lookup_and_update">Toggle insert
        and update based on primary key lookup</label>
    <div style="margin: 20px; margin-top: 4px;">
        <p>Use a primary key column to look up if a record already has
            been copied or not. If the record exists in the target already, issue
            an UPDATE instead of an INSERT.</p>
        <p>Primary key column: <select name="lookup_and_update_column_select">
                            <option value="CUSTOMERNUMBER">CUSTOMERNUMBER</option>
                <option value="CONTACTLASTNAME">CONTACTLASTNAME</option>
                <option value="CONTACTFIRSTNAME">CONTACTFIRSTNAME</option>
        </select> </p>
    </div>

    <input type="radio" name="update_strategy" value="no_strategy"
        id="update_strategy_checkbox_no_strategy" /> <label class="blue"
        for="update_strategy_checkbox_no_strategy">No update strategy</label>
    <div style="margin: 20px; margin-top: 4px;">
        <p>Do not handle update logic in the job / handled elsewhere
            (running the job several times may cause duplicates).</p>
    </div>
</div>"""), normalize(page5.getFormInnerHtml()));

    val formParams5: Map[String, java.util.List[String]] = Map("update_strategy" -> List("lookup_and_update"), "lookup_and_update_column_select" -> List("CUSTOMERNUMBER"))

    val page6 = page5.nextPageController(formParams5)

    assertNotNull(page6);

    assertEquals(normalize("""<div>
	<h1>Provide Job name</h1>
	<div class="alert alert-information">
		<p>The name is the unique identifier for your job. It should be
			concise as well as descriptive to help provide both overview and
			transparency to your solution.</p>
		<p>Should you decide, you can also rename the job later.</p>
	</div>
	<div>
		<label>Please provide a name for the new job </label>
		<div>
			<input type="text" maxlength="64" size="30" value="Copy data" name="name" />
		</div>
	</div>
</div>"""), normalize(page6.getFormInnerHtml()));
    
    val formParams6: Map[String, java.util.List[String]] = Map("name" -> List("copy_data_job"))

    val page7 = page6.nextPageController(formParams6)
    assertNull(page7);

    val job = session.createJob()
    assertNotNull(job);

    val columnNames = job.getSourceColumns().map(_.getName());
    assertEquals("CUSTOMERNUMBER,CONTACTLASTNAME,CONTACTFIRSTNAME", columnNames.mkString(","));

    val analyzerNames = job.getAnalyzerComponentBuilders().map(_.getDescriptor().getDisplayName())
    assertEquals("Insert into table,Update table", analyzerNames.mkString(","));

    val transformerNames = job.getTransformerComponentBuilders().map(_.getDescriptor().getDisplayName())
    assertEquals("Table lookup", transformerNames.mkString(","));

    val filterNames = job.getFilterComponentBuilders().map(_.getDescriptor().getDisplayName())
    assertEquals("Null check", filterNames.mkString(","));

    val configured = job.isConfigured(true)
    assertTrue(configured);
  }

  def normalize(str: String): String = {
    return str.replaceAll("\r\n", "\n").replaceAll("\t", "    ");
  }
}
