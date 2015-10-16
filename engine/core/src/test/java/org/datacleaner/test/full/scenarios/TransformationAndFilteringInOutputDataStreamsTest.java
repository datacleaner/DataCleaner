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
package org.datacleaner.test.full.scenarios;

import java.util.List;

import org.datacleaner.api.Configured;
import org.datacleaner.api.Filter;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.test.MockAnalyzer;
import org.datacleaner.test.MockOutputDataStreamAnalyzer;
import org.datacleaner.test.MockTransformer;
import org.datacleaner.test.TestHelper;
import org.junit.Assert;
import org.junit.Test;

public class TransformationAndFilteringInOutputDataStreamsTest {

    public static enum BespokeCategory {
        VALID, INVALID;
    }

    public static class BespokeNotNullFilter implements Filter<BespokeCategory> {

        @Configured
        InputColumn<?> inputColumn;

        @Override
        public BespokeCategory categorize(InputRow inputRow) {
            Assert.assertTrue(inputRow + " does not contain " + inputColumn, inputRow.containsInputColumn(inputColumn));
            return inputRow.getValue(inputColumn) == null ? BespokeCategory.INVALID : BespokeCategory.VALID;
        }

    }

    @Test
    public void testScenario() throws Throwable {
        final Datastore ds = TestHelper.createSampleDatabaseDatastore("orderdb");
        final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withDatastores(ds);

        final AnalysisJob job;
        try (final AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration)) {
            jobBuilder.setDatastore(ds);
            jobBuilder.addSourceColumns("customers.country");

            final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer1 = jobBuilder
                    .addAnalyzer(MockOutputDataStreamAnalyzer.class);
            analyzer1.addInputColumn(jobBuilder.getSourceColumnByName("country"));

            final AnalysisJobBuilder jobBuilder2 = analyzer1
                    .getOutputDataStreamJobBuilder(MockOutputDataStreamAnalyzer.STREAM_NAME1);
            final InputColumn<?> fooColumn = jobBuilder2.getSourceColumnByName("foo");

            final TransformerComponentBuilder<MockTransformer> transformer1 = jobBuilder2
                    .addTransformer(MockTransformer.class);
            transformer1.addInputColumn(fooColumn);
            final MutableInputColumn<?> transformedColumn = transformer1.getOutputColumns().get(0);

            final FilterComponentBuilder<BespokeNotNullFilter, BespokeCategory> filter1 = jobBuilder2
                    .addFilter(BespokeNotNullFilter.class);
            filter1.addInputColumn(transformedColumn);

            final AnalyzerComponentBuilder<MockAnalyzer> analyzer2 = jobBuilder2.addAnalyzer(MockAnalyzer.class);
            analyzer2.addInputColumn(transformedColumn);
            analyzer2.setRequirement(filter1, BespokeCategory.VALID);

            job = jobBuilder.toAnalysisJob();
        }

        final AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(configuration).run(job);

        resultFuture.await();

        final List<Throwable> errors = resultFuture.getErrors();
        if (!errors.isEmpty()) {
            throw errors.get(0);
        }
    }
}
