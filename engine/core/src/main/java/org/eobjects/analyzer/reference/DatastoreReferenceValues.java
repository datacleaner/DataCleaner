/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.util.CollectionUtils2;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.util.BaseObject;

import com.google.common.cache.Cache;

/**
 * Reference values implementation based on a datastore column.
 * 
 * 
 */
public final class DatastoreReferenceValues extends BaseObject implements ReferenceValues<String> {

    private final Datastore _datastore;
    private final Column _column;

    private transient Cache<String, Boolean> _containsValueCache = CollectionUtils2.createCache(1000, 60);

    public DatastoreReferenceValues(Datastore datastore, Column column) {
        _datastore = datastore;
        _column = column;
    }

    @Override
    protected void decorateIdentity(List<Object> identifiers) {
        identifiers.add(_datastore);
        identifiers.add(_column);
    }

    public void clearCache() {
        _containsValueCache.invalidateAll();
    }

    @Override
    public boolean containsValue(String value) {
        Boolean result = _containsValueCache.getIfPresent(value);
        if (result == null) {
            synchronized (_containsValueCache) {
                result = _containsValueCache.getIfPresent(value);
                if (result == null) {
                    result = false;
                    try (DatastoreConnection con = _datastore.openConnection()) {
                        DataContext dataContext = con.getDataContext();
                        Query q = dataContext.query().from(_column.getTable()).selectCount().where(_column).eq(value)
                                .toQuery();
                        try (DataSet dataSet = dataContext.executeQuery(q)) {
                            if (dataSet.next()) {
                                Row row = dataSet.getRow();
                                if (row != null) {
                                    Number count = (Number) row.getValue(0);
                                    if (count != null && count.intValue() > 0) {
                                        result = true;
                                    }
                                    assert !dataSet.next();
                                }
                            }
                        }
                    }
                    _containsValueCache.put(value, result);
                }
            }
        }
        return result;

    }

    @Override
    public Collection<String> getValues() {
        try (final DatastoreConnection con = _datastore.openConnection()) {
            final DataContext dataContext = con.getDataContext();

            final Query q = dataContext.query().from(_column.getTable()).select(_column).toQuery();
            q.selectDistinct();

            try (final DataSet dataSet = dataContext.executeQuery(q)) {
                final List<String> values = new ArrayList<String>();
                while (dataSet.next()) {
                    final Row row = dataSet.getRow();
                    
                    Object value = row.getValue(0);
                    if (value != null) {
                        value = value.toString();
                    }
                    values.add((String) value);
                }
                return values;
            }
        }
    }
}
