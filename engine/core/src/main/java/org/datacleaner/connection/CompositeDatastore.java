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
package org.datacleaner.connection;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.DataContextFactory;
import org.datacleaner.util.ReadObjectBuilder;

public final class CompositeDatastore extends UsageAwareDatastore<DataContext> {

    private static final long serialVersionUID = 1L;

    private final List<? extends Datastore> _datastores;

    public CompositeDatastore(final String name, final List<? extends Datastore> datastores) {
        super(name);
        _datastores = datastores;
    }

    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        ReadObjectBuilder.create(this, CompositeDatastore.class).readObject(stream);
    }

    public List<? extends Datastore> getDatastores() {
        return _datastores;
    }

    @Override
    protected UsageAwareDatastoreConnection<DataContext> createDatastoreConnection() {
        final List<DataContext> dataContexts = new ArrayList<>(_datastores.size());
        final List<Closeable> closeables = new ArrayList<>(_datastores.size());
        for (final Datastore datastore : _datastores) {
            final DatastoreConnection con = datastore.openConnection();
            final DataContext dc = con.getDataContext();
            closeables.add(con);
            dataContexts.add(dc);
        }
        final Closeable[] closeablesArray = closeables.toArray(new Closeable[closeables.size()]);
        return new DatastoreConnectionImpl<>(DataContextFactory.createCompositeDataContext(dataContexts), this,
                closeablesArray);
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        boolean queryOptimizationPreferred = true;
        boolean naturalRecordOrderConsistent = true;
        for (final Datastore datastore : _datastores) {
            final PerformanceCharacteristics performanceCharacteristics = datastore.getPerformanceCharacteristics();
            queryOptimizationPreferred =
                    queryOptimizationPreferred && performanceCharacteristics.isQueryOptimizationPreferred();
            naturalRecordOrderConsistent =
                    naturalRecordOrderConsistent && performanceCharacteristics.isNaturalRecordOrderConsistent();
        }
        return new PerformanceCharacteristicsImpl(queryOptimizationPreferred, naturalRecordOrderConsistent);
    }

    @Override
    protected void decorateIdentity(final List<Object> identifiers) {
        super.decorateIdentity(identifiers);
        identifiers.add(_datastores);
    }
}
