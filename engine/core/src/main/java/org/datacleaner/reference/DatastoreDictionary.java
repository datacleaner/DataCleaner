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
package org.datacleaner.reference;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;

import org.datacleaner.beans.api.Close;
import org.datacleaner.beans.api.Initialize;
import org.datacleaner.beans.api.Provided;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.util.ReadObjectBuilder;
import org.datacleaner.util.SchemaNavigator;
import org.apache.metamodel.schema.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A dictionary backed by a column in a datastore.
 * 
 * Note that even though this datastore <i>is</i> serializable it is not
 * entirely able to gracefully deserialize. The user of the dictionary will have
 * to inject the DatastoreCatalog using the setter method for this.
 * 
 * 
 */
public final class DatastoreDictionary extends AbstractReferenceData implements Dictionary {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(DatastoreDictionary.class);
	
	private transient ReferenceValues<String> _cachedRefValues;
	private transient BlockingQueue<DatastoreConnection> _datastoreConnections = new LinkedBlockingQueue<DatastoreConnection>();
	private final String _datastoreName;
	private final String _qualifiedColumnName;
	
	@Inject
	@Provided
	transient DatastoreCatalog _datastoreCatalog;

	public DatastoreDictionary(String name, String datastoreName, String qualifiedColumnName) {
		super(name);
		_datastoreName = datastoreName;
		_qualifiedColumnName = qualifiedColumnName;
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		ReadObjectBuilder.create(this, DatastoreDictionary.class).readObject(stream);
	}

	@Override
	protected void decorateIdentity(List<Object> identifiers) {
		super.decorateIdentity(identifiers);
		identifiers.add(_datastoreName);
		identifiers.add(_qualifiedColumnName);
	}

	private BlockingQueue<DatastoreConnection> getDatastoreConnections() {
		if (_datastoreConnections == null) {
			synchronized (this) {
				if (_datastoreConnections == null) {
					_datastoreConnections = new LinkedBlockingQueue<DatastoreConnection>();
				}
			}
		}
		return _datastoreConnections;
	}

	/**
	 * Initializes a DatastoreConnection, which will keep the connection open
	 */
	@Initialize
	public void init() {
		logger.info("Initializing dictionary: {}", this);
		Datastore datastore = getDatastore();
		DatastoreConnection con = datastore.openConnection();
		getDatastoreConnections().add(con);
	}

	/**
	 * Closes a DatastoreConnection, potentially closing the connection (if no
	 * other DatastoreConnections are open).
	 */
	@Close
	public void close() {
        DatastoreConnection con = getDatastoreConnections().poll();
		if (con != null) {
			logger.info("Closing dictionary: {}", this);
			con.close();
		}
	}

	private Datastore getDatastore() {
		Datastore datastore = _datastoreCatalog.getDatastore(_datastoreName);
		if (datastore == null) {
			throw new IllegalStateException("Could not resolve datastore " + _datastoreName);
		}
		return datastore;
	}

	public DatastoreCatalog getDatastoreCatalog() {
		return _datastoreCatalog;
	}

	public String getDatastoreName() {
		return _datastoreName;
	}

	public String getQualifiedColumnName() {
		return _qualifiedColumnName;
	}

	@Override
	public boolean containsValue(String value) {
		// note that caching IS enabled because the ReferenceValues object
		// returned by getValues() contains a cache!
		return getValues().containsValue(value);
	}

	public ReferenceValues<String> getValues() {
		if (_cachedRefValues == null) {
			synchronized (this) {
				if (_cachedRefValues == null) {
					Datastore datastore = getDatastore();

					DatastoreConnection datastoreConnection = datastore.openConnection();
					SchemaNavigator schemaNavigator = datastoreConnection.getSchemaNavigator();
					Column column = schemaNavigator.convertToColumns(new String[] { _qualifiedColumnName })[0];
					if (column == null) {
						throw new IllegalStateException("Could not resolve column " + _qualifiedColumnName);
					}
					_cachedRefValues = new DatastoreReferenceValues(datastore, column);
					datastoreConnection.close();
				}
			}
		}
		return _cachedRefValues;
	}

}
