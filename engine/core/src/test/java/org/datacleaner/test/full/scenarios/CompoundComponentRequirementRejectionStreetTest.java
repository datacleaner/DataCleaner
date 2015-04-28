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
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.MetaModelHelper;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.schema.Table;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputRow;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.CompoundComponentRequirement;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.ListResult;
import org.datacleaner.test.MockAnalyzer;
import org.datacleaner.test.MockTransformer;
import org.datacleaner.test.TestHelper;
import org.datacleaner.test.mock.EvenOddFilter;
import org.datacleaner.test.mock.EvenOddFilter.Category;

/**
 * A test case that simulates a "cleansing street", ie. a series of (two)
 * transformations with filters inbetween. There are two paths: A successful
 * "street" and a "bucket" for all the rejections. The component requirement for
 * the bucket is a {@link CompoundComponentRequirement} consisting of all the
 * non-successful filter outcomes..
 */
public class CompoundComponentRequirementRejectionStreetTest extends TestCase {

    private final Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
    private final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withDatastores(datastore);
    private final int recordsInTable = 122;

    public void testScenario() throws Throwable {
        final AnalysisJob job;

        try (DatastoreConnection connection = datastore.openConnection();) {
            final DataContext dataContext = connection.getDataContext();
            final Table table = dataContext.getTableByQualifiedLabel("PUBLIC.CUSTOMERS");
            final Row row = MetaModelHelper.executeSingleRowQuery(dataContext, dataContext.query().from(table)
                    .selectCount().toQuery());
            assertEquals(recordsInTable, ((Number) row.getValue(0)).intValue());

            try (AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration)) {
                jobBuilder.setDatastore(datastore);
                jobBuilder.addSourceColumns("CUSTOMERS.CONTACTFIRSTNAME");
                jobBuilder.addSourceColumns("CUSTOMERS.CONTACTLASTNAME");

                // although not semantically correct, we pretend that EVEN is
                // the
                // success-state in our cleansing street and that ODD is the
                // reject-state.
                final Category valid = org.datacleaner.test.mock.EvenOddFilter.Category.EVEN;
                final Category invalid = org.datacleaner.test.mock.EvenOddFilter.Category.ODD;

                final TransformerComponentBuilder<MockTransformer> trans1 = jobBuilder
                        .addTransformer(MockTransformer.class);
                trans1.setName("trans1");
                trans1.addInputColumn(jobBuilder.getSourceColumns().get(0));

                final FilterComponentBuilder<EvenOddFilter, org.datacleaner.test.mock.EvenOddFilter.Category> filter1 = jobBuilder
                        .addFilter(EvenOddFilter.class);
                filter1.setName("filter1");
                filter1.addInputColumn(trans1.getOutputColumns().get(0));

                final TransformerComponentBuilder<MockTransformer> trans2 = jobBuilder
                        .addTransformer(MockTransformer.class);
                trans2.setName("trans2");
                trans2.addInputColumn(jobBuilder.getSourceColumns().get(1));
                trans2.setRequirement(filter1, valid);

                final FilterComponentBuilder<EvenOddFilter, org.datacleaner.test.mock.EvenOddFilter.Category> filter2 = jobBuilder
                        .addFilter(EvenOddFilter.class);
                filter2.setName("filter2");
                filter2.addInputColumn(trans2.getOutputColumns().get(0));

                final AnalyzerComponentBuilder<MockAnalyzer> analyzer1 = jobBuilder.addAnalyzer(MockAnalyzer.class);
                analyzer1.setName("success");
                analyzer1.addInputColumn(jobBuilder.getSourceColumns().get(0));
                analyzer1.addInputColumn(jobBuilder.getSourceColumns().get(1));
                analyzer1.addInputColumn(trans1.getOutputColumns().get(0));
                analyzer1.addInputColumn(trans2.getOutputColumns().get(0));
                analyzer1.setRequirement(filter2, valid);

                final FilterOutcome invalid1 = filter1.getFilterOutcome(invalid);
                final FilterOutcome invalid2 = filter2.getFilterOutcome(invalid);
                final AnalyzerComponentBuilder<MockAnalyzer> analyzer2 = jobBuilder.addAnalyzer(MockAnalyzer.class);
                analyzer2.setName("rejects");
                analyzer2.addInputColumn(jobBuilder.getSourceColumns().get(0));
                analyzer2.addInputColumn(jobBuilder.getSourceColumns().get(1));
                analyzer2.setComponentRequirement(new CompoundComponentRequirement(invalid1, invalid2));

                job = jobBuilder.toAnalysisJob();
            }
        }

        final AnalysisRunner runner = new AnalysisRunnerImpl(configuration);
        final AnalysisResultFuture resultFuture = runner.run(job);
        resultFuture.await();

        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        int recordsInResults = 0;

        final Map<ComponentJob, AnalyzerResult> map = resultFuture.getResultMap();
        for (Entry<ComponentJob, AnalyzerResult> entry : map.entrySet()) {
            final ComponentJob componentJob = entry.getKey();
            @SuppressWarnings("unchecked")
            final ListResult<InputRow> result = (ListResult<InputRow>) entry.getValue();
            final List<InputRow> values = result.getValues();
            final int recordsInResult = values.size();
            recordsInResults += recordsInResult;

            switch (componentJob.getName()) {
            case "success":
            case "rejects":
                // expected states
                assertTrue("Expected records in all buckets of the cleansing street, but did not find any in: "
                        + componentJob, recordsInResult > 0);
                assertTrue("Expected records to be distributed across buckets, but found all in: " + componentJob,
                        recordsInResult != recordsInTable);
                break;
            default:
                fail("Unexpected component in result map: " + componentJob);
            }
        }

        assertEquals(recordsInTable, recordsInResults);
    }
}
