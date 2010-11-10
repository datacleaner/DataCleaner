package org.eobjects.datacleaner.output.datastore;

import org.eobjects.analyzer.connection.Datastore;

public interface DatastoreCreationDelegate {

	public void createDatastore(Datastore datastore);
}
