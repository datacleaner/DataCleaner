package org.eobjects.datacleaner.output;

import java.io.File;

import junit.framework.TestCase;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.datacleaner.output.datastore.DatastoreCreationDelegate;
import org.eobjects.datacleaner.output.datastore.DatastoreOutputWriterFactory;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.schema.Table;

public class DatastoreOutputWriterFactoryTest extends TestCase {

	private boolean _datastoreCreated = false;

	public void testFullScenario() throws Exception {
		final OutputWriterScenarioHelper scenarioHelper = new OutputWriterScenarioHelper();

		DatastoreOutputWriterFactory.setOutputDirectory(new File("target/test-output"));
		DatastoreOutputWriterFactory.setDatastoreCreationDelegate(new DatastoreCreationDelegate() {

			@Override
			public void createDatastore(Datastore datastore) {
				_datastoreCreated = true;
				assertEquals("my datastore", datastore.getName());

				DataContext dc = datastore.getDataContextProvider().getDataContext();

				Table table = dc.getDefaultSchema().getTables()[0];
				Query q = dc.query().from(table).select(table.getColumns()).toQuery();
				DataSet dataSet = dc.executeQuery(q);

				scenarioHelper.performAssertions(dataSet, true);
			}
		});
		OutputWriter writer = DatastoreOutputWriterFactory.getWriter("my datastore", scenarioHelper.getColumns());

		scenarioHelper.writeExampleData(writer);
		writer.close();

		assertTrue(_datastoreCreated);
	}
}
