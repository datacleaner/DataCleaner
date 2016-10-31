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
package org.datacleaner.monitor.server;

import org.apache.metamodel.util.ImmutableRef;
import org.datacleaner.api.*;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.descriptors.*;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.monitor.job.MetricJobContext;
import org.datacleaner.monitor.server.job.DataCleanerJobEngine;
import org.datacleaner.monitor.shared.model.MetricGroup;
import org.datacleaner.monitor.shared.model.MetricIdentifier;
import org.datacleaner.result.AnalysisResult;
import org.datacleaner.result.SimpleAnalysisResult;
import org.datacleaner.test.MockAnalyzer;
import org.datacleaner.test.MockOutputDataStreamAnalyzer;
import org.datacleaner.test.TestHelper;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(EasyMockRunner.class)
public class MetricValueUtilsTest extends EasyMockSupport {

    @Mock
    MetricJobContext jobContext;

    @Mock
    MetricIdentifier metricIdentifier;

    @Mock
    ComponentJob componentJob;

    private static class MyResultClass implements AnalyzerResult {

        private static final long serialVersionUID = 1L;

        @Metric("My metric")
        public int getMyMetric() {
            return 42;
        }

        @Metric("My parameterized metric")
        public int getMyMetric(String param) {
            return param.length();
        }
    }

    private final MyResultClass analyzerResult = new MyResultClass();
    private final DataCleanerJobEngine jobEngine = new DataCleanerJobEngine(null, new SimpleDescriptorProvider(true),
            null);
    private final AnalysisJob analysisJob = null;
    private final MetricValueUtils utils = new MetricValueUtils();
    private final ResultDescriptor resultDescriptor = Descriptors.ofResult(MyResultClass.class);

    @Test
    public void testGetResultFromAnalyzerResultFuture() throws Exception {
        final MetricDescriptor metricDescriptor = resultDescriptor.getResultMetric("My metric");
        final MetricParameters parameters = new MetricParameters();

        final AnalyzerResultFuture<MyResultClass> analyzerResultFuture = new AnalyzerResultFutureImpl<>("my result",
                new ImmutableRef<MyResultClass>(analyzerResult));

        EasyMock.expect(metricIdentifier.isFormulaBased()).andReturn(false).atLeastOnce();

        final Map<ComponentJob, AnalyzerResult> results = new HashMap<>();
        final AnalysisResult analysisResult = new SimpleAnalysisResult(results);
        results.put(componentJob, analyzerResultFuture);

        replayAll();

        final Number value = utils.getMetricValue(jobEngine, jobContext, metricIdentifier, metricDescriptor,
                analysisJob, componentJob, analysisResult, parameters);
        assertEquals(42, value);

        verifyAll();
    }

    @Test
    public void testGetResultFromParameterizedMetric() throws Exception {
        final MetricDescriptor metricDescriptor = resultDescriptor.getResultMetric("My parameterized metric");

        EasyMock.expect(metricIdentifier.isFormulaBased()).andReturn(false).atLeastOnce();

        final Map<ComponentJob, AnalyzerResult> results = new HashMap<>();
        final AnalysisResult analysisResult = new SimpleAnalysisResult(results);
        results.put(componentJob, analyzerResult);

        replayAll();

        final Number value1 = utils.getMetricValue(jobEngine, jobContext, metricIdentifier, metricDescriptor,
                analysisJob, componentJob, analysisResult, new MetricParameters("foo"));
        assertEquals(3, value1);

        final Number value2 = utils.getMetricValue(jobEngine, jobContext, metricIdentifier, metricDescriptor,
                analysisJob, componentJob, analysisResult, new MetricParameters("foo bar"));
        assertEquals(7, value2);

        verifyAll();
    }

    @Test
    public void testGetMetricGroupsFromAnalysisJob() throws URISyntaxException, FileNotFoundException {
        final Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
        final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withDatastores(datastore);

        try (final AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration)) {
            ajb.setDatastore(datastore);

            ajb.addSourceColumns("customers.contactfirstname");
            ajb.addSourceColumns("customers.contactlastname");
            ajb.addSourceColumns("customers.city");

            final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer1 = ajb
                    .addAnalyzer(MockOutputDataStreamAnalyzer.class);

            final List<MetaModelInputColumn> sourceColumns = ajb.getSourceColumns();
            analyzer1.setName("analyzer1");
            analyzer1.addInputColumn(sourceColumns.get(0));
            final OutputDataStream outputDataStream = analyzer1.getOutputDataStreams().get(0);
            final AnalysisJobBuilder outputDataStreamJobBuilder =
                    analyzer1.getOutputDataStreamJobBuilder(outputDataStream);
            final List<MetaModelInputColumn> outputDataStreamColumns = outputDataStreamJobBuilder.getSourceColumns();


            final AnalyzerComponentBuilder<MockAnalyzer> analyzer2 = outputDataStreamJobBuilder
                    .addAnalyzer(MockAnalyzer.class);
            analyzer2.addInputColumns(outputDataStreamColumns);
            analyzer2.setName("analyzer2");

            final MetricValueUtils metricValueUtils = new MetricValueUtils();
            final List<MetricGroup> metricGroups = metricValueUtils.getMetricGroups(jobContext, ajb.toAnalysisJob());
            assertEquals(2, metricGroups.size()); // One from each analyzer
        }
    }
}
