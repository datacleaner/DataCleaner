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

import org.datacleaner.util.SchemaNavigator;
import org.apache.metamodel.DataContext;

/**
 * Represents an open connection to a {@link Datastore}.
 * 
 * Connections are often pooled/shared and in those cases, the connection
 * represents a single client of the pool.
 * 
 * {@link DatastoreConnection}s should be closed using the {@link #close()}
 * method.
 */
public interface DatastoreConnection extends Closeable {

	/**
	 * Gets a {@link DataContext} object which provides access to both exploring
	 * schema structure and querying data.
	 */
	public DataContext getDataContext();

	/**
	 * Gets a {@link SchemaNavigator} which provides access to schema
	 * exploration. Note that the schema navigator is preferred over
	 * {@link #getDataContext()} if only schema data is needed.
	 */
	public SchemaNavigator getSchemaNavigator();

	/**
	 * Gets the {@link Datastore} that this is a connection to.
	 */
	public Datastore getDatastore();

	/**
	 * Closes the connection to the datastore.
	 */
	@Override
	public void close();
}
