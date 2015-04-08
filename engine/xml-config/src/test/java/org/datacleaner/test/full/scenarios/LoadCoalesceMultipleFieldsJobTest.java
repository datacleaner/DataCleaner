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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

import org.datacleaner.beans.CompletenessAnalyzer;
import org.datacleaner.beans.CompletenessAnalyzerResult;
import org.datacleaner.beans.coalesce.CoalesceMultipleFieldsTransformer;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.SimpleDescriptorProvider;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.JaxbJobReader;
import org.datacleaner.job.TransformerJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.test.TestHelper;

public class LoadCoalesceMultipleFieldsJobTest extends TestCase {

    public void testScenario() throws Throwable {
        Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
        DatastoreCatalog datastoreCatalog = new DatastoreCatalogImpl(datastore);
        SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider();
        descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(CompletenessAnalyzer.class));
        descriptorProvider.addTransformerBeanDescriptor(Descriptors
                .ofTransformer(CoalesceMultipleFieldsTransformer.class));
        DataCleanerEnvironment environment = new DataCleanerEnvironmentImpl()
                .withDescriptorProvider(descriptorProvider);

        DataCleanerConfigurationImpl configuration = new DataCleanerConfigurationImpl().withDatastoreCatalog(
                datastoreCatalog).withEnvironment(environment);

        JaxbJobReader reader = new JaxbJobReader(configuration);
        AnalysisJobBuilder analysisJobBuilder = reader.create(new File(
                "src/test/resources/example-job-coalesce-completeness.analysis.xml"));

        AnalysisJob job = analysisJobBuilder.toAnalysisJob(true);
        assertNotNull(job);

        Collection<TransformerJob> transformerJobs = job.getTransformerJobs();
        assertEquals(1, transformerJobs.size());

        Collection<AnalyzerJob> analyzerJobs = job.getAnalyzerJobs();
        assertEquals(1, analyzerJobs.size());

        TransformerJob transformerJob = transformerJobs.iterator().next();
        assertEquals(
                "[MetaModelInputColumn[PUBLIC.CUSTOMERS.STATE], MetaModelInputColumn[PUBLIC.CUSTOMERS.COUNTRY], "
                        + "MetaModelInputColumn[PUBLIC.CUSTOMERS.SALESREPEMPLOYEENUMBER], MetaModelInputColumn[PUBLIC.CUSTOMERS.PHONE]]",
                Arrays.toString(transformerJob.getInput()));
        assertEquals("[TransformedInputColumn[id=trans-0001-0002,name=__state_or_country], "
                + "TransformedInputColumn[id=trans-0001-0003,name=__salesrep_or_phone]]",
                Arrays.toString(transformerJob.getOutput()));

        AnalyzerJob analyzerJob = analyzerJobs.iterator().next();
        assertEquals("[TransformedInputColumn[id=trans-0001-0002,name=__state_or_country], "
                + "TransformedInputColumn[id=trans-0001-0003,name=__salesrep_or_phone]]",
                Arrays.toString(analyzerJob.getInput()));

        AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(configuration).run(job);
        resultFuture.await();
        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        CompletenessAnalyzerResult result = (CompletenessAnalyzerResult) resultFuture.getResult(analyzerJob);
        assertEquals(122, result.getTotalRowCount());
        assertEquals(0, result.getInvalidRowCount());
        assertEquals(122, result.getValidRowCount());
    }
}
