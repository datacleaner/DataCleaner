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
package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.util.SchemaNavigator;
import org.apache.metamodel.DataContext;

public final class MockDatastoreConnection implements DatastoreConnection {

	@Override
	public DataContext getDataContext() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SchemaNavigator getSchemaNavigator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Datastore getDatastore() {
		return new MockDatastore();
	}

	@Override
	public void close() {
	}

}
