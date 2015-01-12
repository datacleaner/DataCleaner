package org.datacleaner.monitor.jobwizard.completeness

import org.datacleaner.job.builder.AnalysisJobBuilder
import org.datacleaner.monitor.wizard.common.SelectTableWizardPage
import org.datacleaner.monitor.wizard.job.JobWizard
import org.datacleaner.monitor.wizard.job.JobWizardContext
import org.datacleaner.monitor.wizard.job.JobWizardSession
import org.datacleaner.monitor.wizard.WizardPageController
import org.apache.metamodel.schema.Table
import org.springframework.stereotype.Component
import org.datacleaner.monitor.wizard.job.DataCleanerJobWizardSession
import org.datacleaner.monitor.wizard.job.DataCleanerJobWizard
import org.datacleaner.monitor.server.wizard.JobNameWizardPage

@Component
class CompletenessAnalysisJobWizard extends DataCleanerJobWizard {

  override def getDisplayName = "Completeness analysis";

  override def getExpectedPageCount = 5;

  override def startInternal(context: JobWizardContext) = new DataCleanerJobWizardSession(context) {

    val analysisJobBuilder = new AnalysisJobBuilder(context.getTenantContext().getConfiguration());
    var fieldGroupsCount = 2;
    var jobName: String = "";

    override def getPageCount = 3 + fieldGroupsCount;
    override def createJob = analysisJobBuilder;
    override def firstPageController = new SelectTableWizardPage(context, 0) {

      override def nextPageController(selectedTable: Table) = new SelectFieldGroupsPage(1) {
        analysisJobBuilder.setDatastore(context.getSourceDatastore());
        
        // add all primary keys as source columns - for reference
        selectedTable.getPrimaryKeys().foreach(col => {
          analysisJobBuilder.addSourceColumn(col);
        });

        override def nextPageController(fieldGroups: Int): WizardPageController = {
          fieldGroupsCount = fieldGroups
          return new DefineFieldGroupPage(2, 0, fieldGroupsCount, selectedTable, analysisJobBuilder) {
            override def nextPageController(): WizardPageController = {
              return new JobNameWizardPage(context, 2 + fieldGroupsCount, selectedTable.getName() + " completeness") {
                override def nextPageController(name: String): WizardPageController = {
                  setJobName(name);
                  return null;
                }
              }
            }
          };
        }
      }
    };
  };
}
