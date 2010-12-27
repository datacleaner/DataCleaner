/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.output;

import java.io.File;

import junit.framework.TestCase;

import org.eobjects.analyzer.connection.DataContextProvider;
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

		File outputDir = new File("target/test-output");
		DatastoreCreationDelegate creationDelegate = new DatastoreCreationDelegate() {

			@Override
			public void createDatastore(Datastore datastore) {
				_datastoreCreated = true;
				assertEquals("my datastore", datastore.getName());

				DataContextProvider dcp = datastore.getDataContextProvider();
				DataContext dc = dcp.getDataContext();

				Table table = dc.getDefaultSchema().getTables()[0];
				Query q = dc.query().from(table).select(table.getColumns()).toQuery();
				DataSet dataSet = dc.executeQuery(q);

				scenarioHelper.performAssertions(dataSet, true);

				dcp.close();
			}
		};
		OutputWriter writer = DatastoreOutputWriterFactory.getWriter(outputDir, creationDelegate, "my datastore",
				scenarioHelper.getColumns());

		scenarioHelper.writeExampleData(writer);
		writer.close();

		assertTrue(_datastoreCreated);
	}
}
