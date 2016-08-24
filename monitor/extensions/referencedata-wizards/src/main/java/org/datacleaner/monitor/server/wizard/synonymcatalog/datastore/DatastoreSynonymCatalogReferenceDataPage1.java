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

import org.datacleaner.monitor.server.wizard.shared.datastore.DatastorePage1;
import org.datacleaner.monitor.server.wizard.shared.datastore.DatastoreWizardSession;
import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.wizard.WizardPageController;

final class DatastoreSynonymCatalogReferenceDataPage1 extends DatastorePage1 {

    public DatastoreSynonymCatalogReferenceDataPage1(DatastoreWizardSession session) {
        super(session);
    }

    @Override
    protected String getNameLabel() {
        return "Datastore synonym catalog name";
    }

    @Override
    public WizardPageController nextPageController(Map<String, List<String>> formParameters)
            throws DCUserInputException {
        _session.setName(getString(formParameters, PROPERTY_NAME));
        _session.setDatastore(getString(formParameters, PROPERTY_DATASTORE));

        return new DatastoreSynonymCatalogReferenceDataPage2(_session);
    }
}
