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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.datacleaner.monitor.jobwizard.api.JobWizardContext;
import org.eobjects.datacleaner.monitor.jobwizard.api.JobWizardPageController;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;

/**
 * A simple {@link JobWizardPageController} that asks the user to select the
 * {@link Table} of interest.
 */
public abstract class SelectTableWizardPage extends AbstractFreemarkerWizardPage implements JobWizardPageController {

    private final Datastore _datastore;
    private final Integer _pageIndex;

    public SelectTableWizardPage(JobWizardContext context, Integer pageIndex) {
        this(context.getSourceDatastore(), pageIndex);
    }

    public SelectTableWizardPage(Datastore datastore, Integer pageIndex) {
        _datastore = datastore;
        _pageIndex = pageIndex;
    }

    @Override
    protected String getTemplateFilename() {
        return "SelectTableWizardPage.html";
    }

    @Override
    public Integer getPageIndex() {
        return _pageIndex;
    }

    @Override
    protected Class<?> getTemplateFriendlyClass() {
        return SelectTableWizardPage.class;
    }
    
    protected String getPromptText() {
        return "Please select the source table of the job:";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        final Map<String, Object> map = new HashMap<String, Object>();
        final DatastoreConnection con = _datastore.openConnection();
        try {
            final Schema[] schemas = con.getSchemaNavigator().getSchemas();
            for (Schema schema : schemas) {
                // make sure all table names are cached.
                schema.getTableNames();
            }
            map.put("schemas", Arrays.asList(schemas));
            return map;
        } finally {
            con.close();
        }
    }

    @Override
    public JobWizardPageController nextPageController(Map<String, List<String>> formParameters) {
        final String tableName = formParameters.get("tableName").get(0);
        final DatastoreConnection con = _datastore.openConnection();
        try {
            final Table selectedTable = con.getSchemaNavigator().convertToTable(tableName);
            return nextPageController(selectedTable);
        } finally {
            con.close();
        }
    }

    protected abstract JobWizardPageController nextPageController(Table selectedTable);

}
