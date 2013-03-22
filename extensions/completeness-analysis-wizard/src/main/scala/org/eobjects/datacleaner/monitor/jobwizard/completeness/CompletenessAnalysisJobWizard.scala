package org.eobjects.datacleaner.monitor.jobwizard.completeness

import org.eobjects.analyzer.job.builder.AnalysisJobBuilder
import org.eobjects.datacleaner.monitor.wizard.common.SelectTableWizardPage
import org.eobjects.datacleaner.monitor.wizard.job.JobWizard
import org.eobjects.datacleaner.monitor.wizard.job.JobWizardContext
import org.eobjects.datacleaner.monitor.wizard.job.JobWizardSession
import org.eobjects.datacleaner.monitor.wizard.WizardPageController
import org.eobjects.metamodel.schema.Table
import org.springframework.stereotype.Component
import org.eobjects.datacleaner.monitor.wizard.job.DataCleanerJobWizardSession
import org.eobjects.datacleaner.monitor.wizard.job.DataCleanerJobWizard

@Component
class CompletenessAnalysisJobWizard extends DataCleanerJobWizard {

  override def getDisplayName = "Completeness analysis";

  override def getExpectedPageCount = 4;
  
  override def startInternal(context: JobWizardContext) = new DataCleanerJobWizardSession(context) {

    val analysisJobBuilder = new AnalysisJobBuilder(context.getTenantContext().getConfiguration());
    var fieldGroupsCount = 2;

    override def getPageCount = 2 + fieldGroupsCount;
    override def createJob = analysisJobBuilder;
    override def firstPageController = new SelectTableWizardPage(context, 0) {

      override def nextPageController(selectedTable: Table) = new SelectFieldGroupsPage(1) {
        analysisJobBuilder.setDatastore(context.getSourceDatastore());

        override def nextPageController(fieldGroups: Int): WizardPageController = {
          fieldGroupsCount = fieldGroups
          return new DefineFieldGroupPage(2, 0, fieldGroupsCount, selectedTable, analysisJobBuilder);
        }
      }
    };
  };
}