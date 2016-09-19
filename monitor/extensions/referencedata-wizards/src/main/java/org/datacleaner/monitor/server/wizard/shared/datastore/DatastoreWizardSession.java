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
package org.datacleaner.monitor.server.wizard.shared.datastore;

import javax.xml.parsers.DocumentBuilder;

import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.wizard.referencedata.AbstractReferenceDataWizardSession;
import org.datacleaner.monitor.wizard.referencedata.ReferenceDataWizardContext;
import org.w3c.dom.Element;

public abstract class DatastoreWizardSession extends AbstractReferenceDataWizardSession {

    protected String _name;
    protected String _datastore;
    protected String _schema;
    protected String _table;
    protected String _column;
    
    public DatastoreWizardSession(ReferenceDataWizardContext context) {
        super(context);
    }

    @Override
    public Integer getPageCount() {
        return 4;
    }

    @Override
    protected Element getUpdatedReferenceDataSubSection(final DocumentBuilder documentBuilder) {
        return addElementToConfiguration();
    }
    
    protected abstract Element addElementToConfiguration();
    
    public String getName() {
        return _name;
    }

    public void setName(final String name) {
        if (name == null || name.equals("")) {
            throw new DCUserInputException("Name can not be null or empty. ");
        }
        
        _name = name;
    }

    public String getDatastore() {
        return _datastore;
    }

    public void setDatastore(final String datastore) {
        if (datastore == null || datastore.equals("")) {
            throw new DCUserInputException("Datastore can not be null or empty. ");
        }
 
        _datastore = datastore;
    }

    public String getSchema() {
        return _schema;
    }

    public void setSchema(final String schema) {
        if (schema == null || schema.equals("")) {
            throw new DCUserInputException("Schema can not be null or empty. ");
        }
 
        _schema = schema;
    }

    public String getTable() {
        return _table;
    }

    public void setTable(final String table) {
        if (table == null || table.equals("")) {
            throw new DCUserInputException("Table can not be null or empty. ");
        }
         
        _table = table;
    }

    public String getColumn() {
        return _column;
    }

    public void setColumn(final String column) {
        if (column == null || column.equals("")) {
            throw new DCUserInputException("Column can not be null or empty. ");
        }
         
        _column = column;
    }
}
