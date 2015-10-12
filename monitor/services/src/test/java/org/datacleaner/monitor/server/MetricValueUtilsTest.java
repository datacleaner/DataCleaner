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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.metamodel.util.ImmutableRef;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.AnalyzerResultFuture;
import org.datacleaner.api.AnalyzerResultFutureImpl;
import org.datacleaner.api.Metric;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.MetricDescriptor;
import org.datacleaner.descriptors.MetricParameters;
import org.datacleaner.descriptors.ResultDescriptor;
import org.datacleaner.descriptors.SimpleDescriptorProvider;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.monitor.job.MetricJobContext;
import org.datacleaner.monitor.server.job.DataCleanerJobEngine;
import org.datacleaner.monitor.shared.model.MetricIdentifier;
import org.datacleaner.result.AnalysisResult;
import org.datacleaner.result.SimpleAnalysisResult;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.Test;
import org.junit.runner.RunWith;

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
}
