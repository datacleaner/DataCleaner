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

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import org.eobjects.analyzer.util.ReadObjectBuilder;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.excel.ExcelConfiguration;
import org.apache.metamodel.excel.ExcelDataContext;
import org.apache.metamodel.util.Resource;
import org.apache.metamodel.util.SerializableRef;

/**
 * Datastore implementation for Excel spreadsheets.
 */
public final class ExcelDatastore extends UsageAwareDatastore<UpdateableDataContext> implements FileDatastore,
        ResourceDatastore, UpdateableDatastore {

    private static final long serialVersionUID = 1L;

    private final String _filename;
    private final SerializableRef<Resource> _resourceRef;

    public ExcelDatastore(String name, Resource resource, String filename) {
        super(name);
        _resourceRef = new SerializableRef<Resource>(resource);
        _filename = filename;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        ReadObjectBuilder.create(this, ExcelDatastore.class).readObject(stream);
    }

    @Override
    public Resource getResource() {
        if (_resourceRef == null) {
            return null;
        }
        return _resourceRef.get();
    }

    @Override
    public String getFilename() {
        return _filename;
    }

    @Override
    protected UsageAwareDatastoreConnection<UpdateableDataContext> createDatastoreConnection() {
        final UpdateableDataContext dc;
        final Resource resource = getResource();
        if (resource == null) {
            dc = new ExcelDataContext(new File(_filename));
        } else {
            dc = new ExcelDataContext(resource, new ExcelConfiguration());
        }
        return new UpdateableDatastoreConnectionImpl<UpdateableDataContext>(dc, this);
    }

    @Override
    public UpdateableDatastoreConnection openConnection() {
        DatastoreConnection connection = super.openConnection();
        return (UpdateableDatastoreConnection) connection;
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(false, true);
    }

    @Override
    protected void decorateIdentity(List<Object> identifiers) {
        super.decorateIdentity(identifiers);
        identifiers.add(_filename);
    }
}