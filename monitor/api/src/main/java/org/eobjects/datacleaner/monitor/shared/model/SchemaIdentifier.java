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
package org.eobjects.datacleaner.monitor.shared.model;

import java.io.Serializable;

/**
 * Represents a schema of a datastore
 */
public class SchemaIdentifier implements Serializable, HasName {

    private static final long serialVersionUID = 1L;

    private String _name;
    private DatastoreIdentifier _datastore;

    public SchemaIdentifier(DatastoreIdentifier datastore, String name) {
        _name = name;
        _datastore = datastore;
    }

    public SchemaIdentifier() {
        this(null, null);
    }

    public DatastoreIdentifier getDatastore() {
        return _datastore;
    }

    public void setDatastore(DatastoreIdentifier datastore) {
        _datastore = datastore;
    }

    @Override
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }
}
