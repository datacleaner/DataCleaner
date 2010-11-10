package org.eobjects.datacleaner.output.datastore;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.util.WindowManager;

/**
 * The default implementation of the DatastoreCreationDelegate.
 * 
 * @author Kasper Sørensen
 */
public class DatastoreCreationDelegateImpl implements DatastoreCreationDelegate {

	@Override
	public void createDatastore(Datastore datastore) {
		AnalyzerBeansConfiguration configuration = WindowManager.getInstance().getMainWindow().getConfiguration();
		MutableDatastoreCatalog datastoreCatalog = (MutableDatastoreCatalog) configuration.getDatastoreCatalog();
		datastoreCatalog.addDatastore(datastore);
	}
}
