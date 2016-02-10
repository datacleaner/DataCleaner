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
package org.datacleaner.components.composition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.metamodel.util.FileResource;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.beans.composition.InvokeChildAnalysisJobTransformer;
import org.datacleaner.beans.filter.EqualsFilter;
import org.datacleaner.beans.standardize.CountryStandardizationTransformer;
import org.datacleaner.beans.transform.ConcatenatorTransformer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.SimpleDescriptorProvider;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.ListResult;
import org.datacleaner.test.MockAnalyzer;
import org.datacleaner.test.TestHelper;
import org.junit.Test;

public class InvokeChildAnalysisJobTransformerTest {

    @Test
    public void testIntegrationScenario() throws Throwable {
        final Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
        final SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider(true);
        descriptorProvider.addTransformerBeanDescriptor(Descriptors.ofTransformer(
                CountryStandardizationTransformer.class));
        descriptorProvider.addFilterBeanDescriptor(Descriptors.ofFilter(EqualsFilter.class));
        descriptorProvider.addTransformerBeanDescriptor(Descriptors.ofTransformer(ConcatenatorTransformer.class));
        final DataCleanerEnvironment environment = new DataCleanerEnvironmentImpl().withDescriptorProvider(
                descriptorProvider);
        final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withDatastores(datastore)
                .withEnvironment(environment);

        final AnalysisJob job;
        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration)) {
            ajb.setDatastore(datastore);
            ajb.addSourceColumns("CUSTOMERS.CONTACTLASTNAME", "CUSTOMERS.CONTACTFIRSTNAME", "CUSTOMERS.COUNTRY");

            final TransformerComponentBuilder<InvokeChildAnalysisJobTransformer> transformer = ajb.addTransformer(
                    InvokeChildAnalysisJobTransformer.class);
            transformer.setConfiguredProperty(InvokeChildAnalysisJobTransformer.PROPERTY_JOB_RESOURCE, new FileResource(
                    "src/test/resources/childjob.analysis.xml"));
            transformer.addInputColumns(ajb.getSourceColumns());

            assertTrue(transformer.isConfigured());
            
            assertEquals(2, transformer.getOutputColumns().size());
            assertEquals("country (standardized)", transformer.getOutputColumns().get(0).getName());
            assertEquals("fullname", transformer.getOutputColumns().get(1).getName());

            final AnalyzerComponentBuilder<MockAnalyzer> analyzer = ajb.addAnalyzer(MockAnalyzer.class);
            analyzer.addInputColumns(transformer.getOutputColumns());

            job = ajb.toAnalysisJob();
        }

        final AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(configuration).run(job);
        resultFuture.await();

        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        @SuppressWarnings("unchecked")
        final ListResult<InputRow> result = (ListResult<InputRow>) resultFuture.getResults().get(0);
        final List<InputRow> list = result.getValues();

        assertEquals(214, list.size());
        
        final InputColumn<?>[] outputColumns = job.getTransformerJobs().get(0).getOutput();
        
        int rowNum = 0;
        assertEquals("[FR, null]", list.get(rowNum).getValues(outputColumns).toString());
        
        rowNum++;
        assertEquals("[US, Sue King]", list.get(rowNum).getValues(outputColumns).toString());
        
        rowNum++;
        assertEquals("[AU, null]", list.get(rowNum).getValues(outputColumns).toString());
    }

}
