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
package org.datacleaner.monitor.server.wizard.dictionary.datastore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.datacleaner.connection.Datastore;
import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;

final class DatastoreDictionaryReferenceDataPage3 extends AbstractFreemarkerWizardPage {

    private final DatastoreDictionaryReferenceDataWizardSession _session;

    public DatastoreDictionaryReferenceDataPage3(DatastoreDictionaryReferenceDataWizardSession session) {
        _session = session;
    }

    @Override
    public Integer getPageIndex() {
        return 2;
    }

    @Override
    public WizardPageController nextPageController(Map<String, List<String>> formParameters)
            throws DCUserInputException {
        _session.setTable(getString(formParameters, "table"));
        
        return new DatastoreDictionaryReferenceDataPage4(_session);
    }

    @Override
    protected String getTemplateFilename() {
        return "DatastoreDictionaryReferenceDataPage3.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        final Map<String, Object> model = new HashMap<>();
        model.put("table", _session.getTable());
        model.put("tableOptions", getTableOptions());

        return model;
    }
    
    private String getTableOptions() {
        final StringBuilder builder = new StringBuilder();
        final DatastoreIdentifier datastoreId = new DatastoreIdentifier(_session.getDatastore());
        final Datastore datastore = _session.getWizardContext().getTenantContext().getDatastore(datastoreId);
        final Schema schema = datastore.openConnection().getSchemaNavigator().getSchemaByName(_session.getSchema());
        
        for (Table table : schema.getTables()) {
            builder.append(String.format("<option value=\"%s\">%s</option>", table.getName(), table.getName()));
        }
        
        return builder.toString();
    }
}
