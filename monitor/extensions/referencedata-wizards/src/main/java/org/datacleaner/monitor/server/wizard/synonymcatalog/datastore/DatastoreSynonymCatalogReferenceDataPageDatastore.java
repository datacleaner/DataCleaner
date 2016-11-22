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

import java.util.List;
import java.util.Map;

import org.datacleaner.monitor.server.wizard.shared.ReferenceDataHelper;
import org.datacleaner.monitor.server.wizard.shared.datastore.DatastorePageDatastore;
import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.wizard.WizardPageController;

final class DatastoreSynonymCatalogReferenceDataPageDatastore extends DatastorePageDatastore {

    public DatastoreSynonymCatalogReferenceDataPageDatastore(
            final DatastoreSynonymCatalogReferenceDataWizardSession session) {
        super(session);
    }

    @Override
    protected String getNameLabel() {
        return "Datastore synonym catalog name";
    }

    @Override
    public WizardPageController nextPageController(final Map<String, List<String>> formParameters)
            throws DCUserInputException {
        final String name = getString(formParameters, PROPERTY_NAME);
        final String datastore = getString(formParameters, PROPERTY_DATASTORE);
        ReferenceDataHelper.checkUniqueSynonymCatalog(name, _session.getWizardContext().getTenantContext()
                .getConfiguration().getReferenceDataCatalog());

        _session.setName(name);
        _session.setDatastore(datastore);

        return new DatastoreSynonymCatalogReferenceDataPageSchema(
                (DatastoreSynonymCatalogReferenceDataWizardSession) _session);
    }
}
