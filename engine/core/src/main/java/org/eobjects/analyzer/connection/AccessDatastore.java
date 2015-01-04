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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import org.apache.metamodel.DataContext;
import org.eobjects.analyzer.util.ReadObjectBuilder;
import org.eobjects.metamodel.access.AccessDataContext;

/**
 * Datastore implementation for MS Access databases.
 */
public final class AccessDatastore extends UsageAwareDatastore<DataContext> implements FileDatastore {

	private static final long serialVersionUID = 1L;
	private final String _filename;

	public AccessDatastore(String name, String filename) {
		super(name);
		_filename = filename;
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		ReadObjectBuilder.create(this, AccessDatastore.class).readObject(stream);
	}

	@Override
	public String getFilename() {
		return _filename;
	}

	@Override
	protected UsageAwareDatastoreConnection<DataContext> createDatastoreConnection() {
		DataContext dc = new AccessDataContext(_filename);
		return new DatastoreConnectionImpl<DataContext>(dc, this);
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
