package org.eobjects.datacleaner.monitor.jobwizard.completeness

import org.eobjects.datacleaner.monitor.wizard.WizardPageController
import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException

abstract class SelectFieldGroupsPage(pageIndex: Int) extends WizardPageController {

  override def getPageIndex = pageIndex

  override def getFormInnerHtml: String = {
    return <div>
             <p>
               Checking completeness can be done with different levels of granularity. Either you can choose to have just one
               big group of fields which you want to check for completeness, or you can choose to have individual groups of
               fields for sets of connected fields. For instance, it might be meaningful ...
             </p>
             <ul>
               <li>to consider fields for "given name" and "family name" as a single field group with the title "name".</li>
               <li>to specify a field group for all required information and a field group for optional information.</li>
             </ul>
             <p>Please specify how many groups of fields you wish to add to your analysis:</p>
             <input type="text" value="2" name="num_field_groups"/>
           </div>.toString()
  }

  override def nextPageController(formParameters: java.util.Map[String, java.util.List[String]]): WizardPageController = {
    val fieldGroupsStr = formParameters.get("num_field_groups").get(0);
    try {
      val fieldGroups = Integer.parseInt(fieldGroupsStr);
      if (fieldGroups <= 0) {
        throw new DCUserInputException("Number of field groups must be a positive integer");
      }
      return nextPageController(fieldGroups);
    } catch {
      case e: NumberFormatException =>
        throw new DCUserInputException("Please provide a valid number of field groups");
    }
  }

  def nextPageController(fieldGroups: Int): WizardPageController;
}