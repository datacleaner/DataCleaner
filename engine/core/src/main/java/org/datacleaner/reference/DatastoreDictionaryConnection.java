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
package org.datacleaner.reference;

import java.util.Iterator;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.schema.Column;
import org.datacleaner.connection.DatastoreConnection;

final class DatastoreDictionaryConnection implements DictionaryConnection {

    private final DatastoreConnection _datastoreConnection;
    private final DatastoreDictionary _dictionary;

    public DatastoreDictionaryConnection(final DatastoreDictionary dictionary,
            final DatastoreConnection datastoreConnection) {
        _dictionary = dictionary;
        _datastoreConnection = datastoreConnection;
    }

    @Override
    public boolean containsValue(final String value) {
        final DataContext dataContext = _datastoreConnection.getDataContext();
        final Column column = _dictionary.getColumn(_datastoreConnection);
        try (DataSet dataSet = dataContext.query().from(column.getTable()).select(column).where(column).eq(value)
                .maxRows(1).execute()) {
            if (dataSet.next()) {
                return true;
            }
            return false;
        }
    }

    @Override
    public Iterator<String> getAllValues() {
        return _dictionary.loadIntoMemory(_datastoreConnection).openConnection(null).getAllValues();
    }

    @Override
    public Iterator<String> getLengthSortedValues() {
        return _dictionary.loadIntoMemory(_datastoreConnection).openConnection(null).getLengthSortedValues();
    }

    @Override
    public void close() {
        _datastoreConnection.close();
    }

}
