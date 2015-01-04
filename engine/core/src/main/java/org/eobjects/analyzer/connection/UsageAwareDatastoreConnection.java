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
package org.eobjects.analyzer.connection;

import org.eobjects.analyzer.util.UsageAwareCloseable;
import org.apache.metamodel.DataContext;

/**
 * An abstract pooled {@link DatastoreConnection} that is aware of the amount of
 * times it has been acquired and closed. It encapsulates the closing logic,
 * making sure that it will only close if all usages of the datastore are
 * closed.
 */
public abstract class UsageAwareDatastoreConnection<E extends DataContext> extends UsageAwareCloseable implements DatastoreConnection {

    private final Datastore _datastore;

    public UsageAwareDatastoreConnection(Datastore datastore) {
        _datastore = datastore;
    }

    @Override
    public abstract E getDataContext();


    @Override
    public String toString() {
        return "UsageAwareDatastoreConnection[datastore=" + getDatastoreName() + ",usage=" + getUsageCount() + "]";
    }

    @Override
    public final Datastore getDatastore() {
        return _datastore;
    }

    private String getDatastoreName() {
        if (_datastore != null) {
            return _datastore.getName();
        }
        return "<null>";
    }
    
    @Override
    public int getUsageCount() {
        return super.getUsageCount();
    }
}
