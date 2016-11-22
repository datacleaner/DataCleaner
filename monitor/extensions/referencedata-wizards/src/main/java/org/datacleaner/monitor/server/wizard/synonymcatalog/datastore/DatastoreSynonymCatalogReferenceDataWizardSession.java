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

import org.datacleaner.monitor.server.wizard.shared.datastore.DatastoreWizardSession;
import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.referencedata.ReferenceDataWizardContext;
import org.datacleaner.reference.DatastoreSynonymCatalog;
import org.datacleaner.reference.SynonymCatalog;
import org.w3c.dom.Element;

final class DatastoreSynonymCatalogReferenceDataWizardSession extends DatastoreWizardSession {

    private String _synonymColumnList;
    private String _synonymColumn;
    private String _addNextSynonymColumn;

    public DatastoreSynonymCatalogReferenceDataWizardSession(final ReferenceDataWizardContext context) {
        super(context);
    }

    @Override
    public Integer getPageCount() {
        return 5;
    }

    @Override
    public WizardPageController firstPageController() {
        return new DatastoreSynonymCatalogReferenceDataPageDatastore(this);
    }

    @Override
    protected Element addElementToConfiguration() {
        final Element synonymCatalogsElement = _writer.getSynonymCatalogsElement();
        final String fullColumnName = _schema + "." + _table + "." + _column;
        final String[] synonymColumns = createSynonymColumns();
        final SynonymCatalog catalog = new DatastoreSynonymCatalog(_name, _datastore, fullColumnName, synonymColumns);
        synonymCatalogsElement.appendChild(_writer.externalize(catalog));

        return synonymCatalogsElement;
    }

    private String[] createSynonymColumns() {
        if (_synonymColumnList == null) {
            return new String[] {};
        }

        final String[] synonymFullColumns = _synonymColumnList.split(",");
        final String prefix = _schema + "." + _table + ".";

        for (int i = 0; i < synonymFullColumns.length; i++) {
            synonymFullColumns[i] = prefix + synonymFullColumns[i];
            i++;
        }

        return synonymFullColumns;
    }

    public String getSynonymColumn() {
        return _synonymColumn;
    }

    public void setSynonymColumn(final String synonymColumn) {
        if (synonymColumn == null || synonymColumn.equals("")) {
            throw new DCUserInputException("Synonym column can not be null or empty. ");
        }

        _synonymColumn = synonymColumn;
    }

    public String getSynonymColumnList() {
        return _synonymColumnList;
    }

    public void addToSynonymColumnList(final String synonymColumnList) {
        if (_synonymColumnList == null) {
            _synonymColumnList = synonymColumnList;
        } else {
            _synonymColumnList = _synonymColumnList + "," + synonymColumnList;
        }
    }

    public String getAddNextSynonymColumn() {
        return _addNextSynonymColumn;
    }

    public void setAddNextSynonymColumn(final String addNextSynonymColumn) {
        if (addNextSynonymColumn == null) {
            _addNextSynonymColumn = "off";
        } else {
            _addNextSynonymColumn = addNextSynonymColumn;
        }
    }
}
