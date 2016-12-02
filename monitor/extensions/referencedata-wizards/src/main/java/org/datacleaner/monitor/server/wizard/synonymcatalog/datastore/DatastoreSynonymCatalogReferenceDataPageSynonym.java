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
package org.datacleaner.monitor.server.wizard.synonymcatalog.datastore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacleaner.monitor.server.wizard.shared.datastore.DatastoreHelper;
import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;

final class DatastoreSynonymCatalogReferenceDataPageSynonym extends AbstractFreemarkerWizardPage {
    protected static final String PROPERTY_SYNONYM_COLUMN = "synonymColumn";
    protected static final String PROPERTY_ADD_NEXT_SYNONYM_COLUMN = "addNextSynonymColumn";
    protected static final String PROPERTY_COLUMN_OPTIONS = "columnOptions";

    private final DatastoreSynonymCatalogReferenceDataWizardSession _session;

    public DatastoreSynonymCatalogReferenceDataPageSynonym(
            final DatastoreSynonymCatalogReferenceDataWizardSession session) {
        _session = session;
    }

    @Override
    public Integer getPageIndex() {
        return 4;
    }

    @Override
    protected String getTemplateFilename() {
        return "DatastoreSynonymCatalogReferenceDataPageSynonym.html";
    }

    @Override
    public WizardPageController nextPageController(final Map<String, List<String>> formParameters)
            throws DCUserInputException {
        _session.setSynonymColumn(getString(formParameters, PROPERTY_SYNONYM_COLUMN));
        _session.addToSynonymColumnList(_session.getSynonymColumn());
        _session.setAddNextSynonymColumn(getString(formParameters, PROPERTY_ADD_NEXT_SYNONYM_COLUMN));
        final boolean addNextSynonym =
                (_session.getAddNextSynonymColumn() != null && _session.getAddNextSynonymColumn().equals("on"));

        if (_session.getAddNextSynonymColumn() != null && addNextSynonym) {
            return new DatastoreSynonymCatalogReferenceDataPageSynonym(_session);
        } else {
            return null;
        }
    }

    @Override
    protected Map<String, Object> getFormModel() {
        final Map<String, Object> model = new HashMap<>();
        model.put(PROPERTY_SYNONYM_COLUMN, _session.getSynonymColumn());
        model.put(PROPERTY_ADD_NEXT_SYNONYM_COLUMN, _session.getAddNextSynonymColumn());
        model.put(PROPERTY_COLUMN_OPTIONS, DatastoreHelper
                .getColumnOptions(_session.getWizardContext().getTenantContext(), _session.getDatastore(),
                        _session.getSchema(), _session.getTable()));

        return model;
    }
}
