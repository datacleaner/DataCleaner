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

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Named;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.Close;
import org.datacleaner.api.Configured;
import org.datacleaner.api.InputRow;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.result.ListResult;
import org.datacleaner.test.MockAnalyzer;
import org.datacleaner.test.MockTransformer;
import org.datacleaner.test.mock.MockTransformerWithAnalyzerResult;

import junit.framework.TestCase;

public class AnalysisRunnerImplTest extends TestCase {

    @Named("Test analyzer")
    public static class TestAnalyzer extends MockAnalyzer {

        @Configured
        boolean produceAnError = false;

        @Configured
        boolean produceAnErrorOnGetResult = false;

        @Override
        public void run(final InputRow row, final int distinctCount) {
            if (produceAnError) {
                throw new IllegalStateException("produceAnError=true");
            }
            super.run(row, distinctCount);
        }

        @Override
        public ListResult<InputRow> getResult() {
            if (produceAnErrorOnGetResult) {
                throw new IllegalStateException("produceAnErrorOnGetResult=true");
            }
            return super.getResult();
        }

    }

    @Named("Test transformer1")
    public static class TestTransformer1 extends MockTransformer {
        @Close(onFailure = false)
        public void closeIfSuccessful() {
            MY_BOOL1.set(true);
        }
    }

    @Named("Test transformer2")
    public static class TestTransformer2 extends MockTransformer {
        @Close(onSuccess = false)
        public void closeIfFailure() {
            MY_BOOL2.set(true);
        }
    }

    @Named("Test transformer3")
    public static class TestTransformer3 extends MockTransformer {
        @Close
        public void closeAlways() {
            MY_BOOL3.set(true);
        }
    }

    private static final AtomicBoolean MY_BOOL1 = new AtomicBoolean(false);
    private static final AtomicBoolean MY_BOOL2 = new AtomicBoolean(false);
    private static final AtomicBoolean MY_BOOL3 = new AtomicBoolean(false);
    private DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl();
    private AnalysisRunner runner = new AnalysisRunnerImpl(configuration);
    private Datastore datastore = new CsvDatastore("ds", "src/test/resources/employees.csv");

    public void testCreateResultFromNonAnalyzer() throws Throwable {
        final AnalysisJob job;

        try (AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration)) {
            jobBuilder.setDatastore(datastore);
            jobBuilder.addSourceColumns("name");

            final TransformerComponentBuilder<MockTransformerWithAnalyzerResult> t =
                    jobBuilder.addTransformer(MockTransformerWithAnalyzerResult.class);
            t.setName("Example");
            t.addInputColumn(jobBuilder.getSourceColumns().get(0));

            job = jobBuilder.toAnalysisJob();
        }

        final AnalysisResultFuture analysisResultFuture = runner.run(job);
        analysisResultFuture.await();

        if (analysisResultFuture.isErrornous()) {
            throw analysisResultFuture.getErrors().get(0);
        }

        final Map<ComponentJob, AnalyzerResult> resultMap = analysisResultFuture.getResultMap();
        assertEquals(1, resultMap.size());

        assertEquals("ImmutableTransformerJob[name=Example,transformer=MockTransformerWithAnalyzerResult]",
                resultMap.keySet().iterator().next().toString());
        assertEquals("7", resultMap.values().iterator().next().toString());
    }

    public void testCloseMethodOnFailure() throws Exception {

        try (AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration)) {
            jobBuilder.setDatastore(datastore);
            jobBuilder.addSourceColumns("name");

            final TransformerComponentBuilder<TestTransformer1> transformer1 =
                    jobBuilder.addTransformer(TestTransformer1.class);
            transformer1.addInputColumn(jobBuilder.getSourceColumnByName("name"));
            final List<MutableInputColumn<?>> outputColumns1 = transformer1.getOutputColumns();

            final TransformerComponentBuilder<TestTransformer2> transformer2 =
                    jobBuilder.addTransformer(TestTransformer2.class);
            transformer2.addInputColumn(jobBuilder.getSourceColumnByName("name"));
            final List<MutableInputColumn<?>> outputColumns2 = transformer2.getOutputColumns();

            final TransformerComponentBuilder<TestTransformer3> transformer3 =
                    jobBuilder.addTransformer(TestTransformer3.class);
            transformer3.addInputColumn(jobBuilder.getSourceColumnByName("name"));
            final List<MutableInputColumn<?>> outputColumns3 = transformer3.getOutputColumns();

            final AnalyzerComponentBuilder<TestAnalyzer> analyzer = jobBuilder.addAnalyzer(TestAnalyzer.class);
            analyzer.addInputColumns(outputColumns1);
            analyzer.addInputColumns(outputColumns2);
            analyzer.addInputColumns(outputColumns3);

            AnalysisJob analysisJob;
            analysisJob = jobBuilder.toAnalysisJob();

            // run a succesful job to show the effect on MY_BOOL
            MY_BOOL1.set(false);
            MY_BOOL2.set(false);
            MY_BOOL3.set(false);
            AnalysisResultFuture resultFuture = runner.run(analysisJob);
            resultFuture.await();
            assertTrue(resultFuture.isSuccessful());
            assertTrue(MY_BOOL1.get());
            assertFalse(MY_BOOL2.get());
            assertTrue(MY_BOOL3.get());

            // modify the job to make it crash
            analyzer.setConfiguredProperty("Produce an error", true);
            analysisJob = jobBuilder.toAnalysisJob();

            // run again but this time produce an error
            MY_BOOL1.set(false);
            MY_BOOL2.set(false);
            MY_BOOL3.set(false);
            resultFuture = runner.run(analysisJob);
            resultFuture.await();
            assertFalse(resultFuture.isSuccessful());
            assertEquals("produceAnError=true", resultFuture.getErrors().get(0).getMessage());
            assertFalse(MY_BOOL1.get());
            assertTrue(MY_BOOL2.get());
            assertTrue(MY_BOOL3.get());

            // Error on get result
            analyzer.setConfiguredProperty("Produce an error", false);
            analyzer.setConfiguredProperty("Produce an error on get result", true);
            analysisJob = jobBuilder.toAnalysisJob();

            // run again but this time produce an error
            MY_BOOL1.set(false);
            MY_BOOL2.set(false);
            MY_BOOL3.set(false);
            resultFuture = runner.run(analysisJob);
            resultFuture.await();
            assertFalse(resultFuture.isSuccessful());
            assertEquals("produceAnErrorOnGetResult=true", resultFuture.getErrors().get(0).getMessage());
            assertFalse(MY_BOOL1.get());
            assertTrue(MY_BOOL2.get());
            assertTrue(MY_BOOL3.get());
        }
    }
}
