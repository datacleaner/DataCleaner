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

import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;

final class DatastoreDictionaryReferenceDataPage1 extends AbstractFreemarkerWizardPage {

    private final DatastoreDictionaryReferenceDataWizardSession _session;

    public DatastoreDictionaryReferenceDataPage1(DatastoreDictionaryReferenceDataWizardSession session) {
        _session = session;
    }

    @Override
    public Integer getPageIndex() {
        return 0;
    }

    @Override
    public WizardPageController nextPageController(Map<String, List<String>> formParameters)
            throws DCUserInputException {
        _session.setName(getString(formParameters, "name"));
        _session.setDatastore(getString(formParameters, "datastore"));
        
        return new DatastoreDictionaryReferenceDataPage2(_session);
    }

    @Override
    protected String getTemplateFilename() {
        return "DatastoreDictionaryReferenceDataPage1.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        final Map<String, Object> model = new HashMap<>();
        model.put("name", _session.getName());
        model.put("datastore", _session.getDatastore());
        model.put("datastoreOptions", getDatastoreOptions());

        return model;
    }
    
    private String getDatastoreOptions() {
        final StringBuilder builder = new StringBuilder();
        
        for (DatastoreIdentifier id : _session.getWizardContext().getTenantContext().getDatastores()) {
            builder.append(String.format("<option value=\"%s\">%s</option>", id.getName(), id.getName()));
        }
        
        return builder.toString();
    }
}
