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
package org.datacleaner.output.datastore;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Table;
import org.datacleaner.api.InputColumn;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.output.OutputWriter;
import org.datacleaner.output.OutputWriterScenarioHelper;

import junit.framework.TestCase;

public class DatastoreOutputWriterFactoryTest extends TestCase {

    private static final File OUTPUT_DIR = new File("target/test-output");

    private boolean _datastoreCreated = false;
    private volatile Exception _exception;
    private Datastore _datastore;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (OUTPUT_DIR.exists()) {
            final File[] files = OUTPUT_DIR.listFiles();
            for (final File file : files) {
                file.delete();
            }
        }
        _exception = null;
    }

    public void testMultiThreadedWriting() throws Exception {
        final AtomicInteger datastoreCount = new AtomicInteger();
        final OutputWriterScenarioHelper scenarioHelper = new OutputWriterScenarioHelper();

        final DatastoreCreationDelegate creationDelegate = new DatastoreCreationDelegate() {

            @Override
            public synchronized void createDatastore(final Datastore datastore) {
                if (_datastore != null) {
                    assertEquals(_datastore, datastore);
                }
                _datastore = datastore;
                datastoreCount.incrementAndGet();
            }
        };

        final InputColumn<?>[] columns = scenarioHelper.getColumns().toArray(new InputColumn[0]);

        // creating 9 similar writers that all write at the same time
        final Thread[] threads = new Thread[9];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread() {
                @Override
                public void run() {
                    try {
                        final OutputWriter writer = DatastoreOutputWriterFactory
                                .getWriter(OUTPUT_DIR, creationDelegate, "ds", "tab", false, columns);
                        scenarioHelper.writeExampleData(writer);
                    } catch (final RuntimeException e) {
                        _exception = e;
                    }
                }

            };
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

        if (_exception != null) {
            throw _exception;
        }
        assertEquals(9, datastoreCount.get());

        assertNotNull(_datastore);
        try (DatastoreConnection connection = _datastore.openConnection()) {
            final DataContext dc = connection.getDataContext();
            dc.refreshSchemas();
            final List<String> tableNames = dc.getDefaultSchema().getTableNames();

            assertEquals("[TAB_1, TAB_2, TAB_3, TAB_4, TAB_5, TAB_6, TAB_7, TAB_8, TAB_9]",
                    tableNames.stream().sorted().collect(Collectors.toList()).toString());
        }
    }

    public void testFullScenario() throws Exception {
        final OutputWriterScenarioHelper scenarioHelper = new OutputWriterScenarioHelper();

        final DatastoreCreationDelegate creationDelegate = datastore -> {
            _datastoreCreated = true;
            assertEquals("my datastore", datastore.getName());

            try (DatastoreConnection con = datastore.openConnection()) {
                final DataContext dc = con.getDataContext();

                final Table table = dc.getDefaultSchema().getTable(0);
                final Query q = dc.query().from(table).select(table.getColumns()).toQuery();
                final DataSet dataSet = dc.executeQuery(q);

                scenarioHelper.performAssertions(dataSet, true);
            }
        };
        final OutputWriter writer = DatastoreOutputWriterFactory
                .getWriter(OUTPUT_DIR, creationDelegate, "my datastore", "my dataset",
                        scenarioHelper.getColumns().toArray(new InputColumn[0]));

        scenarioHelper.writeExampleData(writer);

        assertEquals("my_dataset", DatastoreOutputWriterFactory.getActualTableName(writer));

        assertTrue(_datastoreCreated);
    }
}
