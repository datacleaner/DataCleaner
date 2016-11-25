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
package org.datacleaner.job.runner;

import java.util.ArrayList;
import java.util.List;

import org.apache.metamodel.pojo.ArrayTableDataProvider;
import org.apache.metamodel.pojo.TableDataProvider;
import org.apache.metamodel.util.SimpleTableDef;
import org.datacleaner.api.ComponentMessage;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.connection.PojoDatastore;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.test.MockAnalyzer;
import org.datacleaner.test.MockTransformer;
import org.datacleaner.test.MockTransformerMessage;

import junit.framework.TestCase;

public class ComponentContextImplTest extends TestCase {

    public void testPublishMessage() throws Throwable {
        // set up a in-memory datastore
        final List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[] { "1", "Kasper" });
        rows.add(new Object[] { "2", "Claudia" });
        final TableDataProvider<?> tableDataProvider =
                new ArrayTableDataProvider(new SimpleTableDef("table", new String[] { "id", "name" }), rows);
        final PojoDatastore datastore = new PojoDatastore("foo", tableDataProvider);

        final DataCleanerConfiguration configuration =
                new DataCleanerConfigurationImpl().withDatastoreCatalog(new DatastoreCatalogImpl(datastore));

        // build a job with the MockTransformer (publishes MockTransformerMessages)
        final AnalysisJob job;
        try (AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration)) {
            jobBuilder.setDatastore(datastore);
            jobBuilder.addSourceColumns("id", "name");

            final TransformerComponentBuilder<MockTransformer> mockTransformer =
                    jobBuilder.addTransformer(MockTransformer.class);
            mockTransformer.addInputColumn(jobBuilder.getSourceColumnByName("name"));
            mockTransformer.setName("FOO");

            final AnalyzerComponentBuilder<MockAnalyzer> analyzer = jobBuilder.addAnalyzer(MockAnalyzer.class);
            analyzer.addInputColumns(mockTransformer.getOutputColumns());

            job = jobBuilder.toAnalysisJob();
        }

        final List<ComponentMessage> messages = new ArrayList<>();
        final AnalysisListener listener = new AnalysisListenerAdaptor() {
            @Override
            public void onComponentMessage(final AnalysisJob jobParameter, final ComponentJob componentJob,
                    final ComponentMessage message) {
                assertSame(job, jobParameter);
                assertEquals("ImmutableTransformerJob[name=FOO,transformer=Mock transformer]", componentJob.toString());
                messages.add(message);
            }
        };

        final AnalysisRunnerImpl runner = new AnalysisRunnerImpl(configuration, listener);
        final AnalysisResultFuture resultFuture = runner.run(job);

        resultFuture.await();

        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        assertEquals(2, messages.size());

        final MockTransformerMessage message = (MockTransformerMessage) messages.get(0);
        assertEquals("MockTransformerMessage[Mocking: Kasper]", message.toString());
        assertEquals("MockTransformerMessage[Mocking: Claudia]", messages.get(1).toString());

        assertEquals("MetaModelInputColumn[foo.table.name]", message.getColumn().toString());
    }
}
