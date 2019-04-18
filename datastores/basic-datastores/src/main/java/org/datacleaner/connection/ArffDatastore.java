/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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
package org.datacleaner.connection;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.arff.ArffDataContext;
import org.apache.metamodel.util.Resource;

/**
 * Datastore implementation for .arff files.
 */
public class ArffDatastore extends UsageAwareDatastore<DataContext> implements ResourceDatastore {

    private static final long serialVersionUID = 1L;
    
    private final Resource _resource;
    
    public ArffDatastore(String name, Resource resource) {
        super(name);
        _resource = resource;
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(false, true);
    }
    
    @Override
    public Resource getResource() {
        return _resource;
    }

    @Override
    protected UsageAwareDatastoreConnection<DataContext> createDatastoreConnection() {
        final DataContext dataContext = new ArffDataContext(getResource());
        return new DatastoreConnectionImpl<DataContext>(dataContext, this);
    }
}
