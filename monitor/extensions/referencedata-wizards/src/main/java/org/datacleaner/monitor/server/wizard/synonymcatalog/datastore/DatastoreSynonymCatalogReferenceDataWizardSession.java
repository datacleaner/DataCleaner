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

import java.util.ArrayList;
import java.util.List;

import org.datacleaner.monitor.server.wizard.shared.datastore.DatastoreWizardSession;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.referencedata.ReferenceDataWizardContext;
import org.datacleaner.reference.DatastoreSynonymCatalog;
import org.datacleaner.reference.SynonymCatalog;
import org.w3c.dom.Element;

final class DatastoreSynonymCatalogReferenceDataWizardSession extends DatastoreWizardSession {

    private List<String> _synonymColumns = new ArrayList<>();
    private boolean _addNextSynonymColumn;
    
    public DatastoreSynonymCatalogReferenceDataWizardSession(ReferenceDataWizardContext context) {
        super(context);
    }

    @Override
    public WizardPageController firstPageController() {
        return new DatastoreSynonymCatalogReferenceDataPage1(this);
    }

    @Override
    protected Element addElementToConfiguration() {
        final Element synonymCatalogsElement = _writer.getSynonymCatalogsElement();
        final String fullColumnName = _schema + "." + _table + "." + _column;
        final SynonymCatalog catalog = new DatastoreSynonymCatalog(_name, _datastore, fullColumnName, 
                _synonymColumns.toArray(new String[_synonymColumns.size()]));
        synonymCatalogsElement.appendChild(_writer.externalize(catalog));

        return synonymCatalogsElement;
    }

    public List<String> getSynonymColumns() {
        return _synonymColumns;
    }

    public boolean isAddNextSynonymColumn() {
        return _addNextSynonymColumn;
    }

    public void setAddNextSynonymColumn(final boolean addNextSynonymColumn) {
        _addNextSynonymColumn = addNextSynonymColumn;
    }
}
