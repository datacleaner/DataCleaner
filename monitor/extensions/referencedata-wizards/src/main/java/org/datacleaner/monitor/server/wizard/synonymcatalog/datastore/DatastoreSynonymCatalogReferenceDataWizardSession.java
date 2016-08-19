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

import javax.xml.parsers.DocumentBuilder;

import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.referencedata.AbstractReferenceDataWizardSession;
import org.datacleaner.monitor.wizard.referencedata.ReferenceDataWizardContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

final class DatastoreSynonymCatalogReferenceDataWizardSession extends AbstractReferenceDataWizardSession {

    private static final Logger logger = LoggerFactory.getLogger(DatastoreSynonymCatalogReferenceDataWizardSession.class);

    private String _name;
    private String _datastore;
    private String _termColumn;
    private String _synonymColumn;

    public DatastoreSynonymCatalogReferenceDataWizardSession(ReferenceDataWizardContext context) {
        super(context);
    }

    @Override
    public WizardPageController firstPageController() {
        return new DatastoreSynonymCatalogReferenceDataPage(this);
    }

    @Override
    public Integer getPageCount() {
        return 1;
    }

    @Override
    protected Element createReferenceDataElement(final DocumentBuilder documentBuilder) {
        final Document doc = documentBuilder.newDocument();
        final Element element = doc.createElement("datastore-synonym-catalog-reference-data");
        element.setAttribute("name", _name);
        element.setAttribute("datastore", _datastore);
        element.setAttribute("termColumn", _termColumn);
        element.setAttribute("synonymColumn", _synonymColumn);

        return element;
    }

    public String getName() {
        return _name;
    }

    public void setName(final String name) {
        _name = name;
    }

    public String getDatastore() {
        return _datastore;
    }

    public void setDatastore(final String datastore) {
        _datastore = datastore;
    }

    public String getTermColumn() {
        return _termColumn;
    }

    public void setTermColumn(final String termColumn) {
        _termColumn = termColumn;
    }

    public String getSynonymColumn() {
        return _synonymColumn;
    }

    public void setSynonymColumn(final String synonymColumn) {
        _synonymColumn = synonymColumn;
    }
}
