/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.datacleaner.monitor.jobwizard.common;

import java.util.List;

import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.monitor.server.wizard.JobNameWizardPage;
import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.common.SelectColumnsWizardPage;
import org.eobjects.datacleaner.monitor.wizard.common.SelectTableWizardPage;
import org.eobjects.datacleaner.monitor.wizard.job.DataCleanerJobWizardSession;
import org.eobjects.datacleaner.monitor.wizard.job.JobWizardContext;
import org.eobjects.datacleaner.user.QuickAnalysisStrategy;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;

/**
 * Session implementation for the Quick Analysis wizard.
 */
final class MockWizardSession extends DataCleanerJobWizardSession {

    private final AnalysisJobBuilder _analysisJobBuilder;

    public MockWizardSession(JobWizardContext context) {
        super(context);
        _analysisJobBuilder = new AnalysisJobBuilder(getWizardContext().getTenantContext().getConfiguration());
        _analysisJobBuilder.setDatastore(getWizardContext().getSourceDatastore());
    }

    @Override
    public WizardPageController firstPageController() {
        return new SelectTableWizardPage(getWizardContext(), 0) {
            @Override
            protected WizardPageController nextPageController(Table selectedTable) {
                return new SelectColumnsWizardPage(1, selectedTable) {
                    @Override
                    protected WizardPageController nextPageController(List<Column> selectedColumns) {
                        _analysisJobBuilder.addSourceColumns(selectedColumns);
                        return new JobNameWizardPage(getWizardContext(), 2) {
                            @Override
                            protected WizardPageController nextPageController(String name) {
                                setJobName(name);
                                return null;
                            }
                        };
                    }
                };
            }
        };
    }

    @Override
    public Integer getPageCount() {
        return 2;
    }

    @Override
    public AnalysisJobBuilder createJob() {
        final QuickAnalysisStrategy quickAnalysisStrategy = new QuickAnalysisStrategy(100, true, true);
        quickAnalysisStrategy.configureAnalysisJobBuilder(_analysisJobBuilder);
        return _analysisJobBuilder;
    }
}
