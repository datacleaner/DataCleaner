/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
import org.eobjects.datacleaner.monitor.jobwizard.api.JobWizardContext;
import org.eobjects.datacleaner.monitor.jobwizard.api.JobWizardPageController;
import org.eobjects.datacleaner.monitor.jobwizard.api.JobWizardSession;
import org.eobjects.datacleaner.user.QuickAnalysisStrategy;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;

/**
 * Session implementation for the Quick Analysis wizard.
 */
final class MockWizardSession implements JobWizardSession {

    private final JobWizardContext _context;
    private final AnalysisJobBuilder _analysisJobBuilder;

    public MockWizardSession(JobWizardContext context) {
        _context = context;
        _analysisJobBuilder = new AnalysisJobBuilder(_context.getTenantContext().getConfiguration());
        _analysisJobBuilder.setDatastore(_context.getSourceDatastore());
    }

    @Override
    public JobWizardPageController firstPageController() {
        return new SelectTableWizardPage(_context, 0) {
            @Override
            protected JobWizardPageController nextPageController(Table selectedTable) {
                return new SelectColumnsWizardPage(1, selectedTable) {
                    @Override
                    protected JobWizardPageController nextPageController(List<Column> selectedColumns) {
                        _analysisJobBuilder.addSourceColumns(selectedColumns);
                        return null;
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
