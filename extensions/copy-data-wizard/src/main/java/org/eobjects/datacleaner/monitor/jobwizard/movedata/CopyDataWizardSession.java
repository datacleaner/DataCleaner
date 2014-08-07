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
package org.eobjects.datacleaner.monitor.jobwizard.movedata;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.common.SelectTableWizardPage;
import org.eobjects.datacleaner.monitor.wizard.job.DataCleanerJobWizardSession;
import org.eobjects.datacleaner.monitor.wizard.job.JobWizardContext;
import org.apache.metamodel.schema.Table;

final class CopyDataWizardSession extends DataCleanerJobWizardSession {

    private final AnalysisJobBuilder _analysisJobBuilder;

    public CopyDataWizardSession(JobWizardContext context) {
        super(context);

        _analysisJobBuilder = new AnalysisJobBuilder(context.getTenantContext().getConfiguration());
        _analysisJobBuilder.setDatastore(context.getSourceDatastore());
    }

    @Override
    public WizardPageController firstPageController() {
        return new SelectTableWizardPage(getWizardContext(), 0) {
            @Override
            protected WizardPageController nextPageController(Table selectedTable) {
                return new SelectDatastoreWizardPage(CopyDataWizardSession.this, _analysisJobBuilder, selectedTable);
            }
        };
    }

    @Override
    public AnalysisJobBuilder createJob() {
        boolean configured = _analysisJobBuilder.isConfigured(true);
        assert configured;
        return _analysisJobBuilder;
    }

    public String[] getDatastoreNames() {
        final TenantContext tenantContext = getWizardContext().getTenantContext();
        final AnalyzerBeansConfiguration configuration = tenantContext.getConfiguration();
        return configuration.getDatastoreCatalog().getDatastoreNames();
    }

    public Datastore getDatastore(String name) {
        final TenantContext tenantContext = getWizardContext().getTenantContext();
        final AnalyzerBeansConfiguration configuration = tenantContext.getConfiguration();
        return configuration.getDatastoreCatalog().getDatastore(name);
    }
}
