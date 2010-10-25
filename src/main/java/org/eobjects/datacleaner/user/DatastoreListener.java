package org.eobjects.datacleaner.user;

import org.eobjects.analyzer.connection.Datastore;

public interface DatastoreListener {

	public void onAdd(Datastore datastore);
	
	public void onRemove(Datastore datastore);
}
