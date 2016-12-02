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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import org.apache.metamodel.DataContext;
import org.datacleaner.util.ReadObjectBuilder;
import org.eobjects.metamodel.deebase.DbaseDataContext;

/**
 * Datastore implementation for dBase databases.
 */
public final class DbaseDatastore extends UsageAwareDatastore<DataContext> implements FileDatastore {

    private static final long serialVersionUID = 1L;

    private final String _filename;

    public DbaseDatastore(final String name, final String filename) {
        super(name);
        _filename = filename;
    }

    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        ReadObjectBuilder.create(this, DbaseDatastore.class).readObject(stream);
    }

    @Override
    public String getFilename() {
        return _filename;
    }

    @Override
    protected UsageAwareDatastoreConnection<DataContext> createDatastoreConnection() {
        final DataContext dc = new DbaseDataContext(_filename);
        return new DatastoreConnectionImpl<>(dc, this);
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(false, true);
    }

    @Override
    protected void decorateIdentity(final List<Object> identifiers) {
        super.decorateIdentity(identifiers);
        identifiers.add(_filename);
    }
}
