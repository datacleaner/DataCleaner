/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.monitor.jobwizard.movedata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacleaner.connection.Datastore;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;
import org.datacleaner.monitor.wizard.common.SelectTableWizardPage;
import org.apache.metamodel.schema.Table;

class SelectDatastoreWizardPage extends AbstractFreemarkerWizardPage {

    private final CopyDataWizardSession _session;
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final Table _sourceTable;

    public SelectDatastoreWizardPage(CopyDataWizardSession session, AnalysisJobBuilder analysisJobBuilder,
            Table sourceTable) {
        _session = session;
        _analysisJobBuilder = analysisJobBuilder;
        _sourceTable = sourceTable;
    }

    @Override
    public Integer getPageIndex() {
        return 1;
    }

    @Override
    public WizardPageController nextPageController(Map<String, List<String>> formParameters) {
        final String name = formParameters.get("datastoreName").get(0);
        final Datastore datastore = _session.getDatastore(name);

        return new SelectTableWizardPage(datastore, 2) {

            @Override
            protected String getPromptText() {
                return "Select the target table to write to:";
            }

            @Override
            protected WizardPageController nextPageController(Table selectedTable) {
                return new ColumnMappingPage(_session, _analysisJobBuilder, _sourceTable, datastore, selectedTable);
            }
        };
    }

    @Override
    protected String getTemplateFilename() {
        return "SelectDatastoreWizardPage.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        final String[] datastoreNames = _session.getDatastoreNames();
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("datastoreNames", datastoreNames);
        return map;
    }

}
