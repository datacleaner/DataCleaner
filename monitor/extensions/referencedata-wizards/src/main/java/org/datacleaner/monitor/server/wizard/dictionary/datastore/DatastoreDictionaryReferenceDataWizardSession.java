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

import javax.xml.parsers.DocumentBuilder;

import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.referencedata.AbstractReferenceDataWizardSession;
import org.datacleaner.monitor.wizard.referencedata.ReferenceDataWizardContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

final class DatastoreDictionaryReferenceDataWizardSession extends AbstractReferenceDataWizardSession {

    private static final Logger logger = LoggerFactory.getLogger(DatastoreDictionaryReferenceDataWizardSession.class);

    private String _name;
    private String _datastore;
    private String _schema;
    private String _table;
    private String _column;

    public DatastoreDictionaryReferenceDataWizardSession(ReferenceDataWizardContext context) {
        super(context);
    }

    @Override
    public WizardPageController firstPageController() {
        return new DatastoreDictionaryReferenceDataPage1(this);
    }

    @Override
    public Integer getPageCount() {
        return 4;
    }

    @Override
    protected Element createReferenceDataElement(final DocumentBuilder documentBuilder) {
        return null;// mytodo
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

    public String getSchema() {
        return _schema;
    }

    public void setSchema(final String schema) {
        _schema = schema;
    }

    public String getTable() {
        return _table;
    }

    public void setTable(final String table) {
        _table = table;
    }

    public String getColumn() {
        return _column;
    }

    public void setColumn(final String column) {
        _column = column;
    }
}
