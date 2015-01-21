package org.datacleaner.monitor.jobwizard.completeness
import org.apache.metamodel.schema.Table
import scala.collection.JavaConversions._
import scala.collection.JavaConversions
import org.datacleaner.monitor.wizard.WizardPageController
import org.datacleaner.job.builder.AnalysisJobBuilder
import org.datacleaner.beans.CompletenessAnalyzer
import org.datacleaner.api.InputColumn
import org.datacleaner.monitor.shared.model.DCUserInputException
import org.datacleaner.monitor.server.wizard.JobNameWizardPage
import org.datacleaner.monitor.wizard.job.JobWizardContext

abstract class DefineFieldGroupPage(pageIndex: Int, fieldGroupIndex: Int, fieldGroupCount: Int, selectedTable: Table, analysisJobBuilder: AnalysisJobBuilder) extends WizardPageController {

  override def getPageIndex = pageIndex

  override def getFormInnerHtml: String = {
    return <div>
             <p>{
               "Please define the name and the columns of field group no. " + (fieldGroupIndex + 1) + ":"
             }</p>
             <p>Field group name: <input name="field_group_name" value={ "Field group no. " + (fieldGroupIndex + 1) }/></p>
             <p>Please select the source columns of the job:</p>
             <table>
               <tr>
                 <th>&nbsp;</th>
                 <th>Name</th>
                 <th>Type</th>
               </tr>
               {
                 selectedTable.getColumns().map(column => {
                   <tr>
                     <td><input type="checkbox" name="columns" id={ "column_checkbox_" + column.getColumnNumber() } value={ column.getName() } title={ column.getName() }/></td>
                     <td><label for={ "column_checkbox_" + column.getColumnNumber() }>{ column.getName() }</label></td>
                     <td>{ column.getType() }</td>
                   </tr>
                 })
               }
             </table>
           </div>.toString()
  }

  override def nextPageController(formParameters: java.util.Map[String, java.util.List[String]]): WizardPageController = {
    val fieldGroupName = formParameters.get("field_group_name").get(0);
    val columnNames = formParameters.get("columns");
    
    if (columnNames == null || columnNames.size() == 0) {
      throw new DCUserInputException("Please select at least a single column for the field group");
    }
    
    val columns = columnNames.map(name => selectedTable.getColumnByName(name));

    val completenessAnalyzer = analysisJobBuilder.addAnalyzer(classOf[CompletenessAnalyzer]);
    completenessAnalyzer.setName(fieldGroupName);
    
    val conditions: Array[CompletenessAnalyzer.Condition] = Array();

    var i = 0;
    val inputColumns: Array[InputColumn[_]] = Array.fill(columns.length) {
      val column = columns.get(i)
      analysisJobBuilder.addSourceColumn(column)
      val inputColumn = analysisJobBuilder.getSourceColumnByName(column.getName())
      i = i + 1;
      inputColumn
    }

    completenessAnalyzer.getConfigurableBean().setValueColumns(inputColumns);
    completenessAnalyzer.getConfigurableBean().fillAllConditions(CompletenessAnalyzer.Condition.NOT_BLANK_OR_NULL);

    // validate just to fail fast
    completenessAnalyzer.isConfigured(true)

    if (fieldGroupCount - 1 == fieldGroupIndex) {
      // done with field groups
      return nextPageController();
    }

    val parent = this;
    return new DefineFieldGroupPage(pageIndex + 1, fieldGroupIndex + 1, fieldGroupCount, selectedTable, analysisJobBuilder) {
      override def nextPageController(): WizardPageController = {
        return parent.nextPageController();
      }
    }
  }
  
  def nextPageController(): WizardPageController;
  
}
