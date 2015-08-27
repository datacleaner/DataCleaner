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
package org.datacleaner.beans;

import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.apache.metamodel.pojo.TableDataProvider;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.util.Resource;
import org.apache.metamodel.util.UrlResource;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.beans.CompletenessAnalyzer.Condition;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.PojoDatastore;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.descriptors.AnalyzerDescriptor;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.OutputDataStreamJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.concurrent.MultiThreadedTaskRunner;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.ListResult;
import org.datacleaner.storage.RowAnnotationFactory;
import org.datacleaner.storage.RowAnnotations;
import org.datacleaner.test.MockAnalyzer;

public class CompletenessAnalyzerTest extends TestCase {

    public void testIsDistributable() throws Exception {
        AnalyzerDescriptor<CompletenessAnalyzer> descriptor = Descriptors.ofAnalyzer(CompletenessAnalyzer.class);
        assertTrue(descriptor.isDistributable());
    }

    public void testAllFieldsEvaluationMode() throws Exception {
        final RowAnnotationFactory annotationFactory = RowAnnotations.getDefaultFactory();

        final InputColumn<?> col1 = new MockInputColumn<String>("foo");
        final InputColumn<?> col2 = new MockInputColumn<String>("bar");

        final CompletenessAnalyzer analyzer = new CompletenessAnalyzer();
        analyzer._evaluationMode = CompletenessAnalyzer.EvaluationMode.ALL_FIELDS;
        analyzer._annotationFactory = annotationFactory;
        analyzer._invalidRecords = annotationFactory.createAnnotation();
        analyzer._valueColumns = new InputColumn[] { col1, col2 };
        analyzer._conditions = new CompletenessAnalyzer.Condition[] { CompletenessAnalyzer.Condition.NOT_NULL,
                CompletenessAnalyzer.Condition.NOT_NULL };

        analyzer.init();

        analyzer.run(new MockInputRow(1001).put(col1, null).put(col2, null), 1);
        analyzer.run(new MockInputRow(1002).put(col1, "hello").put(col2, null), 1);
        analyzer.run(new MockInputRow(1002).put(col1, null).put(col2, "world"), 1);
        analyzer.run(new MockInputRow(1002).put(col1, "hello").put(col2, "world"), 1);

        assertEquals(4, analyzer.getResult().getTotalRowCount());
        assertEquals(1, analyzer.getResult().getInvalidRowCount());
        assertEquals(3, analyzer.getResult().getValidRowCount());
    }

    public void testConfigurableBeanConfiguration() throws Exception {
        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(new DataCleanerConfigurationImpl())) {
            List<TableDataProvider<?>> tableDataProviders = Collections.emptyList();
            ajb.setDatastore(new PojoDatastore("ds", tableDataProviders));
            ajb.addSourceColumn(new MutableColumn("foo", ColumnType.VARCHAR));

            AnalyzerComponentBuilder<CompletenessAnalyzer> analyzer = ajb.addAnalyzer(CompletenessAnalyzer.class);
            analyzer.getComponentInstance().setValueColumns(ajb.getSourceColumns().toArray(new InputColumn[0]));
            analyzer.getComponentInstance().fillAllConditions(Condition.NOT_BLANK_OR_NULL);

            assertTrue(analyzer.isConfigured(true));
        }
    }

    public void testSimpleScenario() throws Exception {
        final RowAnnotationFactory annotationFactory = RowAnnotations.getDefaultFactory();

        final InputColumn<?> col1 = new MockInputColumn<String>("foo");
        final InputColumn<?> col2 = new MockInputColumn<String>("bar");
        final InputColumn<?> col3 = new MockInputColumn<String>("baz");

        final CompletenessAnalyzer analyzer = new CompletenessAnalyzer();
        analyzer._annotationFactory = annotationFactory;
        analyzer._invalidRecords = annotationFactory.createAnnotation();
        analyzer._valueColumns = new InputColumn[] { col1, col2, col3 };
        analyzer._conditions = new CompletenessAnalyzer.Condition[] { CompletenessAnalyzer.Condition.NOT_NULL,
                CompletenessAnalyzer.Condition.NOT_BLANK_OR_NULL, CompletenessAnalyzer.Condition.NOT_NULL };

        analyzer.init();

        analyzer.run(new MockInputRow(1001).put(col1, null).put(col2, null).put(col3, null), 1);
        analyzer.run(new MockInputRow(1002).put(col1, "").put(col2, "").put(col3, ""), 1);

        assertEquals(2, analyzer.getResult().getTotalRowCount());
        assertEquals(0, analyzer.getResult().getValidRowCount());
        assertEquals(2, analyzer.getResult().getInvalidRowCount());

        analyzer.run(new MockInputRow(1002).put(col1, "").put(col2, "not blank").put(col3, ""), 1);
        analyzer.run(new MockInputRow(1002).put(col1, "not blank").put(col2, "not blank").put(col3, "not blank"), 1);

        assertEquals(4, analyzer.getResult().getTotalRowCount());
        assertEquals(2, analyzer.getResult().getValidRowCount());
        assertEquals(2, analyzer.getResult().getInvalidRowCount());

        analyzer.run(new MockInputRow(1002).put(col1, null).put(col2, "not blank").put(col3, ""), 1);

        assertEquals(5, analyzer.getResult().getTotalRowCount());
        assertEquals(2, analyzer.getResult().getValidRowCount());
        assertEquals(3, analyzer.getResult().getInvalidRowCount());
    }

    public void testOutputDataStream() throws Throwable {
        final MultiThreadedTaskRunner taskRunner = new MultiThreadedTaskRunner(16);
        final DataCleanerEnvironment environment = new DataCleanerEnvironmentImpl()
                .withTaskRunner(taskRunner);
        final Resource file = new UrlResource(this.getClass().getResource("/completeness_output_stream_test.csv"));
        final Datastore datastore = new CsvDatastore("testoutputdatastream", file);
        final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withDatastores(datastore)
                .withEnvironment(environment);

        final AnalysisJob job;
        try (final AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration)) {
            ajb.setDatastore(datastore);
            ajb.addSourceColumns("A");
            ajb.addSourceColumns("B");
            ajb.addSourceColumns("C");

            final AnalyzerComponentBuilder<CompletenessAnalyzer> analyzer1 = ajb
                    .addAnalyzer(CompletenessAnalyzer.class);

            final List<MetaModelInputColumn> sourceColumns = ajb.getSourceColumns();
            analyzer1.setName("analyzer1");
            analyzer1.addInputColumns(sourceColumns);
            analyzer1.setConfiguredProperty(CompletenessAnalyzer.PROPERTY_EVALUATION_MODE, CompletenessAnalyzer.EvaluationMode.ANY_FIELD);
            analyzer1.setConfiguredProperty(CompletenessAnalyzer.PROPERTY_CONDITIONS, new CompletenessAnalyzer.Condition[] {
                    Condition.NOT_BLANK_OR_NULL, Condition.NOT_BLANK_OR_NULL, Condition.NOT_BLANK_OR_NULL });

            assertTrue(analyzer1.isConfigured());
            final OutputDataStream completeStream = analyzer1.getOutputDataStream(CompletenessAnalyzer.OUTPUT_STREAM_COMPLETE);
            assertNotNull(completeStream);
            final OutputDataStream incompleteStream = analyzer1.getOutputDataStream(CompletenessAnalyzer.OUTPUT_STREAM_INCOMPLETE);
            assertNotNull(incompleteStream);

            final AnalysisJobBuilder completeDataStreamJobBuilder = analyzer1.getOutputDataStreamJobBuilder(completeStream);
            final List<MetaModelInputColumn> completeDataStreamColumns = completeDataStreamJobBuilder.getSourceColumns();
            assertEquals(3, completeDataStreamColumns.size());
            assertEquals("MetaModelInputColumn[" + CompletenessAnalyzer.OUTPUT_STREAM_COMPLETE +
                    ".A]", completeDataStreamColumns.get(0).toString());
            assertEquals("MetaModelInputColumn[" + CompletenessAnalyzer.OUTPUT_STREAM_COMPLETE +
                    ".B]", completeDataStreamColumns.get(1).toString());
            assertEquals("MetaModelInputColumn[" + CompletenessAnalyzer.OUTPUT_STREAM_COMPLETE +
                    ".C]", completeDataStreamColumns.get(2).toString());

            final AnalysisJobBuilder incompleteDataStreamJobBuilder = analyzer1.getOutputDataStreamJobBuilder(incompleteStream);
            final List<MetaModelInputColumn> incompleteDataStreamColumns = incompleteDataStreamJobBuilder.getSourceColumns();
            assertEquals(3, incompleteDataStreamColumns.size());
            assertEquals("MetaModelInputColumn[" + CompletenessAnalyzer.OUTPUT_STREAM_INCOMPLETE +
                    ".A]", incompleteDataStreamColumns.get(0).toString());
            assertEquals("MetaModelInputColumn[" + CompletenessAnalyzer.OUTPUT_STREAM_INCOMPLETE +
                    ".B]", incompleteDataStreamColumns.get(1).toString());
            assertEquals("MetaModelInputColumn[" + CompletenessAnalyzer.OUTPUT_STREAM_INCOMPLETE +
                    ".C]", incompleteDataStreamColumns.get(2).toString());
            
            final AnalyzerComponentBuilder<MockAnalyzer> analyzer2 = completeDataStreamJobBuilder
                    .addAnalyzer(MockAnalyzer.class);
            analyzer2.addInputColumns(completeDataStreamColumns);
            analyzer2.setName("analyzer2");
            assertTrue(analyzer2.isConfigured());
            assertTrue(analyzer1.isOutputDataStreamConsumed(completeStream));

            final AnalyzerComponentBuilder<MockAnalyzer> analyzer3 = incompleteDataStreamJobBuilder
                    .addAnalyzer(MockAnalyzer.class);
            analyzer3.addInputColumns(incompleteDataStreamColumns);
            analyzer3.setName("analyzer2");
            assertTrue(analyzer2.isConfigured());
            assertTrue(analyzer1.isOutputDataStreamConsumed(incompleteStream));

            job = ajb.toAnalysisJob();
        }

        final AnalyzerJob analyzerJob1 = job.getAnalyzerJobs().get(0);
        final OutputDataStreamJob[] outputDataStreamJobs = analyzerJob1.getOutputDataStreamJobs();
        final AnalyzerJob analyzerJob2 = outputDataStreamJobs[0].getJob().getAnalyzerJobs().get(0);
        final AnalyzerJob analyzerJob3 = outputDataStreamJobs[1].getJob().getAnalyzerJobs().get(0);

        // now run the job(s)
        final AnalysisRunnerImpl runner = new AnalysisRunnerImpl(configuration);
        final AnalysisResultFuture resultFuture = runner.run(job);
        resultFuture.await();

        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        assertEquals(3, resultFuture.getResults().size());

        final CompletenessAnalyzerResult result1 = (CompletenessAnalyzerResult) resultFuture.getResult(analyzerJob1);
        assertNotNull(result1);
        assertEquals(1, result1.getValidRowCount());
        assertEquals(1, result1.getInvalidRowCount());
        final ListResult<?> result2 = (ListResult<?>) resultFuture.getResult(analyzerJob2);
        assertNotNull(result2);
        assertEquals(1, result2.getValues().size());
        assertEquals("MetaModelInputRow[Row[values=[a, b, c]]]", result2.getValues().get(0).toString());

        final ListResult<?> result3 = (ListResult<?>) resultFuture.getResult(analyzerJob3);
        assertNotNull(result3);
        assertEquals(1, result3.getValues().size());
        assertEquals("MetaModelInputRow[Row[values=[, , ]]]", result3.getValues().get(0).toString());
    }

}
