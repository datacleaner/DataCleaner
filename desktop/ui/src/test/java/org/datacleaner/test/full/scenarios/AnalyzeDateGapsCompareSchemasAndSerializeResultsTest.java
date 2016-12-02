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

import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.SerializationUtils;
import org.apache.metamodel.util.ObjectComparator;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.beans.dategap.DateGapAnalyzer;
import org.datacleaner.components.convert.ConvertToStringTransformer;
import org.datacleaner.components.maxrows.MaxRowsFilter;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.SimpleDescriptorProvider;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.AnalysisResult;
import org.datacleaner.result.SimpleAnalysisResult;
import org.datacleaner.test.TestHelper;
import org.datacleaner.util.CollectionUtils2;

import junit.framework.TestCase;

public class AnalyzeDateGapsCompareSchemasAndSerializeResultsTest extends TestCase {

    @SuppressWarnings("unchecked")
    public void testScenario() throws Throwable {
        final DataCleanerConfiguration configuration;
        {
            // create configuration
            final SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider();
            descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(DateGapAnalyzer.class));
            descriptorProvider.addFilterBeanDescriptor(Descriptors.ofFilter(MaxRowsFilter.class));
            descriptorProvider
                    .addTransformerBeanDescriptor(Descriptors.ofTransformer(ConvertToStringTransformer.class));
            final Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
            configuration = new DataCleanerConfigurationImpl().withDatastores(datastore)
                    .withEnvironment(new DataCleanerEnvironmentImpl().withDescriptorProvider(descriptorProvider));
        }

        final AnalysisJob job;
        {
            // create job
            final AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(configuration);
            final Datastore datastore = configuration.getDatastoreCatalog().getDatastore("orderdb");
            analysisJobBuilder.setDatastore(datastore);
            analysisJobBuilder.addSourceColumns("PUBLIC.ORDERS.ORDERDATE", "PUBLIC.ORDERS.SHIPPEDDATE",
                    "PUBLIC.ORDERS.CUSTOMERNUMBER");
            assertEquals(3, analysisJobBuilder.getSourceColumns().size());

            final FilterComponentBuilder<MaxRowsFilter, MaxRowsFilter.Category> maxRows =
                    analysisJobBuilder.addFilter(MaxRowsFilter.class);
            maxRows.getComponentInstance().setMaxRows(5);
            analysisJobBuilder.setDefaultRequirement(maxRows.getFilterOutcome(MaxRowsFilter.Category.VALID));

            final TransformerComponentBuilder<ConvertToStringTransformer> convertToNumber =
                    analysisJobBuilder.addTransformer(ConvertToStringTransformer.class);
            convertToNumber.addInputColumn(analysisJobBuilder.getSourceColumnByName("customernumber"));
            final InputColumn<String> customer_no = (InputColumn<String>) convertToNumber.getOutputColumns().get(0);

            final AnalyzerComponentBuilder<DateGapAnalyzer> dateGap =
                    analysisJobBuilder.addAnalyzer(DateGapAnalyzer.class);
            dateGap.setName("date gap job");
            dateGap.getComponentInstance().setSingleDateOverlaps(true);
            dateGap.getComponentInstance()
                    .setFromColumn((InputColumn<Date>) analysisJobBuilder.getSourceColumnByName("orderdate"));
            dateGap.getComponentInstance()
                    .setToColumn((InputColumn<Date>) analysisJobBuilder.getSourceColumnByName("shippeddate"));
            dateGap.getComponentInstance().setGroupColumn(customer_no);

            job = analysisJobBuilder.toAnalysisJob();
            analysisJobBuilder.close();
        }

        final AnalysisResultFuture future = new AnalysisRunnerImpl(configuration).run(job);
        if (future.isErrornous()) {
            throw future.getErrors().get(0);
        }
        assertTrue(future.isSuccessful());

        final SimpleAnalysisResult result1 = new SimpleAnalysisResult(future.getResultMap());
        final byte[] bytes = SerializationUtils.serialize(result1);
        final SimpleAnalysisResult result2 = (SimpleAnalysisResult) SerializationUtils.deserialize(bytes);

        performResultAssertions(job, future);
        performResultAssertions(job, result1);
        performResultAssertions(job, result2);
    }

    private void performResultAssertions(final AnalysisJob job, final AnalysisResult result) {
        assertEquals(1, result.getResults().size());

        Collection<ComponentJob> componentJobs = result.getResultMap().keySet();
        componentJobs = CollectionUtils2.sorted(componentJobs, ObjectComparator.getComparator());

        assertEquals("[ImmutableAnalyzerJob[name=date gap job,analyzer=Date gap analyzer]]", componentJobs.toString());

        // using the original component jobs not only asserts that these exist
        // in the result, but also that the their deserialized clones are equal
        // (otherwise the results cannot be retrieved from the result map).
        final AnalyzerJob analyzerJob = job.getAnalyzerJobs().iterator().next();

        final AnalyzerResult analyzerResult = result.getResult(analyzerJob);
        assertNotNull(analyzerResult);
        assertEquals("DateGapAnalyzerResult[gaps={121=[], 128=[], 141=[], 181=[], 363=[]}]", analyzerResult.toString());
    }

}
