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
package org.eobjects.datacleaner.monitor.jobwizard.movedata;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.jobwizard.api.JobWizardContext;
import org.eobjects.datacleaner.monitor.jobwizard.api.JobWizardPageController;
import org.eobjects.datacleaner.monitor.jobwizard.api.JobWizardSession;
import org.eobjects.datacleaner.monitor.jobwizard.common.SelectTableWizardPage;
import org.eobjects.metamodel.schema.Table;

final class MoveDataWizardSession implements JobWizardSession {

    private final JobWizardContext _context;
    private final AnalysisJobBuilder _analysisJobBuilder;

    public MoveDataWizardSession(JobWizardContext context) {
        _context = context;

        _analysisJobBuilder = new AnalysisJobBuilder(context.getTenantContext().getConfiguration());
        _analysisJobBuilder.setDatastore(context.getSourceDatastore());
    }

    @Override
    public JobWizardPageController firstPageController() {
        return new SelectTableWizardPage(_context, 0) {
            @Override
            protected JobWizardPageController nextPageController(Table selectedTable) {
                return new SelectDatastoreWizardPage(MoveDataWizardSession.this, _analysisJobBuilder, selectedTable);
            }
        };

    }

    @Override
    public AnalysisJobBuilder createJob() {

        return _analysisJobBuilder;
    }

    @Override
    public Integer getPageCount() {
        return 4;
    }

    public String[] getDatastoreNames() {
        final TenantContext tenantContext = _context.getTenantContext();
        final AnalyzerBeansConfiguration configuration = tenantContext.getConfiguration();
        return configuration.getDatastoreCatalog().getDatastoreNames();
    }

    public Datastore getDatastore(String name) {
        final TenantContext tenantContext = _context.getTenantContext();
        final AnalyzerBeansConfiguration configuration = tenantContext.getConfiguration();
        return configuration.getDatastoreCatalog().getDatastore(name);
    }
}
