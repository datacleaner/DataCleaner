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
package org.datacleaner.monitor.wizard.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.job.JobWizardContext;
import org.datacleaner.util.SchemaComparator;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.Predicate;

/**
 * A simple {@link WizardPageController} that asks the user to select the
 * {@link Table} of interest.
 */
public abstract class SelectTableWizardPage extends AbstractFreemarkerWizardPage {

    private final Datastore _datastore;
    private final Integer _pageIndex;

    private String _selectedTableName = "";

    public SelectTableWizardPage(JobWizardContext context, Integer pageIndex) {
        this(context.getSourceDatastore(), pageIndex);
    }

    public SelectTableWizardPage(Datastore datastore, Integer pageIndex) {
        _datastore = datastore;
        _pageIndex = pageIndex;
    }

    public String getSelectedTableName() {
        return _selectedTableName;
    }

    public void setSelectedTableName(String selectedTableName) {
        _selectedTableName = selectedTableName;
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
        map.put("promptText", getPromptText());
        map.put("selectedTableName", _selectedTableName);
        try (final DatastoreConnection con = _datastore.openConnection()) {
            final Schema[] schemas = con.getSchemaNavigator().getSchemas();
            final List<Schema> schemaList = CollectionUtils.filter(schemas, new Predicate<Schema>() {
                @Override
                public Boolean eval(Schema schema) {
                    final boolean isInformationSchema = SchemaComparator.isInformationSchema(schema);
                    return !isInformationSchema;
                }
            });
            for (Schema schema : schemaList) {
                // make sure all table names are cached.
                schema.getTableNames();
            }
            map.put("schemas", schemaList);
            return map;
        }
    }

    @Override
    public WizardPageController nextPageController(Map<String, List<String>> formParameters) {
        _selectedTableName = getString(formParameters, "tableName");
        try (final DatastoreConnection con = _datastore.openConnection()) {
            final Table selectedTable = con.getSchemaNavigator().convertToTable(_selectedTableName);
            return nextPageController(selectedTable);
        }
    }

    protected abstract WizardPageController nextPageController(Table selectedTable);

}
