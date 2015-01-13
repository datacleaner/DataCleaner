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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.metamodel.DataContext;

public class DatastoreConnectionImpl<E extends DataContext> extends UsageAwareDatastoreConnection<E> {

	private static final Logger logger = LoggerFactory.getLogger(DatastoreConnectionImpl.class);

	private final E _dataContext;
	private final SchemaNavigator _schemaNavigator;
	private final Closeable[] _closeables;

	public DatastoreConnectionImpl(E dataContext, Datastore datastore, Closeable... closeables) {
		super(datastore);
		_dataContext = dataContext;
		_schemaNavigator = new SchemaNavigator(dataContext);
		_closeables = closeables;
	}

	@Override
	public final E getDataContext() {
		return _dataContext;
	}

	@Override
	public final SchemaNavigator getSchemaNavigator() {
		return _schemaNavigator;
	}

	@Override
	protected final void closeInternal() {
		for (int i = 0; i < _closeables.length; i++) {
            final Closeable closeable = _closeables[i];
			try {
				closeable.close();
			} catch (IOException e) {
				logger.error("Could not close _closeables[" + i + "]", e);
			}
		}
	}
}
