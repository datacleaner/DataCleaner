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

import java.util.concurrent.atomic.AtomicBoolean;

import org.eobjects.analyzer.util.SchemaNavigator;
import org.apache.metamodel.DataContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A datastore connection lease that ensures that the connection can only be
 * closed once by a particular user. The underlying (delegated) connection can
 * thus be shared safely without risking premature closing by other parties.
 */
public class DatastoreConnectionLease implements DatastoreConnection {

	private static final Logger logger = LoggerFactory.getLogger(DatastoreConnectionLease.class);

	private final DatastoreConnection _delegate;
	private final AtomicBoolean _closed;

	public DatastoreConnectionLease(DatastoreConnection delegate) {
		_delegate = delegate;
		_closed = new AtomicBoolean(false);
	}

	public DatastoreConnection getDelegate() {
		return _delegate;
	}

	public boolean isClosed() {
		return _closed.get();
	}

	@Override
	public DataContext getDataContext() {
		return _delegate.getDataContext();
	}

	@Override
	public SchemaNavigator getSchemaNavigator() {
		return _delegate.getSchemaNavigator();
	}

	@Override
	public Datastore getDatastore() {
		return _delegate.getDatastore();
	}

	@Override
	public void close() {
		boolean changed = _closed.compareAndSet(false, true);
		if (changed) {
			_delegate.close();
		} else {
			logger.warn("Connection is already closed, but close() was invoked!", new Throwable());
		}
	}
	
	@Override
	public String toString() {
		return "DatastoreConnectionLease[" + _delegate + "]";
	}
}
