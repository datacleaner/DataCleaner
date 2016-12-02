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
package org.datacleaner.job.builder;

import java.util.List;

import org.datacleaner.api.InputColumn;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.test.MockAnalyzer;
import org.datacleaner.test.MockTransformer;
import org.datacleaner.test.TestHelper;

import junit.framework.TestCase;

public class AnalysisJobBuilderImportHelperTest extends TestCase {

    public void testImportTransformersWithBackwardsChaining() throws Exception {
        // build a job with a renamed transformer output column
        final Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
        final DataCleanerConfiguration conf = new DataCleanerConfigurationImpl().withDatastores(datastore);

        final AnalysisJob originalJob;
        try (AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(conf)) {
            jobBuilder.setDatastore(datastore);
            jobBuilder.addSourceColumns("EMPLOYEES.FIRSTNAME");

            final TransformerComponentBuilder<MockTransformer> transformer1 =
                    jobBuilder.addTransformer(MockTransformer.class);
            {
                transformer1.addInputColumn(jobBuilder.getSourceColumnByName("FIRSTNAME"));
                final List<MutableInputColumn<?>> columns = transformer1.getOutputColumns();
                assertEquals("[TransformedInputColumn[id=trans-0001-0002,name=mock output]]", columns.toString());
                final MutableInputColumn<?> renamedColumn = columns.get(0);
                renamedColumn.setName("foo");
            }

            final TransformerComponentBuilder<MockTransformer> transformer2 =
                    jobBuilder.addTransformer(MockTransformer.class);
            {
                transformer2.addInputColumn(jobBuilder.getSourceColumnByName("FIRSTNAME"));
                final List<MutableInputColumn<?>> columns = transformer2.getOutputColumns();
                assertEquals("[TransformedInputColumn[id=trans-0003-0004,name=mock output]]", columns.toString());
                final MutableInputColumn<?> renamedColumn = columns.get(0);
                renamedColumn.setName("bar");
            }

            // re-wire transformer1 to depend on transformer2
            transformer1.clearInputColumns();
            transformer1.addInputColumn(transformer2.getOutputColumnByName("bar"));

            jobBuilder.addAnalyzer(MockAnalyzer.class).addInputColumn(transformer1.getOutputColumnByName("foo"));

            originalJob = jobBuilder.toAnalysisJob();
        }

        try (AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(conf, originalJob)) {

            final AnalyzerComponentBuilder<?> analyzer = jobBuilder.getAnalyzerComponentBuilders().get(0);
            assertEquals("foo", analyzer.getInputColumns().get(0).getName());

            final List<TransformerComponentBuilder<?>> transformers = jobBuilder.getTransformerComponentBuilders();
            assertEquals(2, transformers.size());

            final TransformerComponentBuilder<?> transformer1 = transformers.get(0);
            assertEquals("foo", transformer1.getOutputColumns().get(0).getName());
            assertEquals("bar", transformer1.getInputColumns().get(0).getName());

            final TransformerComponentBuilder<?> transformer2 = transformers.get(1);
            assertEquals("bar", transformer2.getOutputColumns().get(0).getName());
            assertEquals("FIRSTNAME", transformer2.getInputColumns().get(0).getName());

            final List<InputColumn<?>> inputColumns1 = transformer1.getInputColumns();
            final List<MutableInputColumn<?>> outputColumns2 = transformer2.getOutputColumns();
            final InputColumn<?> expected = inputColumns1.get(0);
            final MutableInputColumn<?> actual = outputColumns2.get(0);

            assertEquals(expected, actual);
            assertSame(expected, actual);
        }
    }

    public void testImportTransformerAndAnalyzer() throws Exception {
        // build a job with a renamed transformer output column
        final Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
        final DataCleanerConfiguration conf = new DataCleanerConfigurationImpl().withDatastores(datastore);

        final AnalysisJob originalJob;
        try (AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(conf)) {
            jobBuilder.setDatastore(datastore);
            jobBuilder.addSourceColumns("EMPLOYEES.FIRSTNAME");

            final TransformerComponentBuilder<MockTransformer> transformer =
                    jobBuilder.addTransformer(MockTransformer.class);
            transformer.addInputColumn(jobBuilder.getSourceColumnByName("FIRSTNAME"));
            final List<MutableInputColumn<?>> columns = transformer.getOutputColumns();
            assertEquals("[TransformedInputColumn[id=trans-0001-0002,name=mock output]]", columns.toString());

            final MutableInputColumn<?> renamedColumn = columns.get(0);
            renamedColumn.setName("foobar");

            jobBuilder.addAnalyzer(MockAnalyzer.class).addInputColumn(renamedColumn);

            originalJob = jobBuilder.toAnalysisJob();
        }

        try (AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(conf, originalJob)) {
            final List<TransformerComponentBuilder<?>> transformers = jobBuilder.getTransformerComponentBuilders();
            assertEquals(1, transformers.size());

            final List<MutableInputColumn<?>> outputColumns = transformers.get(0).getOutputColumns();
            assertEquals("foobar", outputColumns.get(0).getName());
        }
    }
}
