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
package org.eobjects.analyzer.connection;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.pojo.PojoDataContext;
import org.apache.metamodel.pojo.TableDataProvider;

/**
 * A {@link Datastore} that works entirely on in-memory Java objects.
 */
public class PojoDatastore implements UpdateableDatastore, Serializable {

    private static final long serialVersionUID = 1L;

    private final PojoDataContext _dataContext;
    private final String _datastoreName;
    private String _description;

    public PojoDatastore(String name, List<TableDataProvider<?>> tableDataProviders) {
        this(name, name, tableDataProviders);
    }

    public PojoDatastore(String name, TableDataProvider<?>... tableDataProviders) {
        this(name, name, Arrays.asList(tableDataProviders));
    }

    public PojoDatastore(String name, String schemaName, TableDataProvider<?>... tableDataProviders) {
        this(name, schemaName, Arrays.asList(tableDataProviders));
    }

    public PojoDatastore(String datastoreName, String schemaName, List<TableDataProvider<?>> tableDataProviders) {
        _datastoreName = datastoreName;
        _dataContext = new PojoDataContext(schemaName, tableDataProviders);
    }

    @Override
    public String getName() {
        return _datastoreName;
    }

    @Override
    public String getDescription() {
        return _description;
    }

    @Override
    public void setDescription(String description) {
        _description = description;
    }

    @Override
    public UpdateableDatastoreConnection openConnection() {
        return new UpdateableDatastoreConnectionImpl<UpdateableDataContext>(_dataContext, this);
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(false, true);
    }

}
