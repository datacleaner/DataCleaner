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

import java.util.ArrayList;
import java.util.List;

import org.apache.metamodel.pojo.ArrayTableDataProvider;
import org.apache.metamodel.util.SimpleTableDef;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.connection.PojoDatastore;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnyComponentRequirement;
import org.datacleaner.job.CompoundComponentRequirement;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.ListResult;
import org.datacleaner.test.MockAnalyzer;
import org.datacleaner.test.MockTransformer;
import org.datacleaner.test.mock.EvenOddFilter;

import junit.framework.TestCase;

public class FilterRequirementMergingTest extends TestCase {

    private DataCleanerConfiguration configuration;
    private PojoDatastore datastore;
    private AnalysisJobBuilder jobBuilder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final SimpleTableDef tableDef = new SimpleTableDef("table", new String[] { "col1" });
        final List<Object[]> rowData = new ArrayList<>();
        rowData.add(new Object[] { "foo" });
        rowData.add(new Object[] { "bar" });
        rowData.add(new Object[] { "baz" });
        rowData.add(new Object[] { "hello" });
        rowData.add(new Object[] { "world" });
        datastore = new PojoDatastore("ds", "sch", new ArrayTableDataProvider(tableDef, rowData));

        final DatastoreCatalog datastoreCatalog = new DatastoreCatalogImpl(datastore);
        configuration = new DataCleanerConfigurationImpl().withDatastoreCatalog(datastoreCatalog);

        jobBuilder = new AnalysisJobBuilder(configuration);
        jobBuilder.setDatastore(datastore);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        jobBuilder.close();
    }

    /**
     * Tests that if two transformations that have different (opposing)
     * requirements both feed into the same component (e.g. an Analyzer), then
     * both requirement states will be accepted by that component.
     */
    public void testMergeFilterRequirementsWhenAnalyzerConsumesInputColumnsWithMultipleRequirements() throws Throwable {
        jobBuilder.addSourceColumns("col1");
        final InputColumn<?> sourceColumn = jobBuilder.getSourceColumnByName("col1");
        final FilterComponentBuilder<EvenOddFilter, EvenOddFilter.Category> filter =
                jobBuilder.addFilter(EvenOddFilter.class).addInputColumn(sourceColumn);

        final FilterOutcome req1 = filter.getFilterOutcome(EvenOddFilter.Category.EVEN);
        final FilterOutcome req2 = filter.getFilterOutcome(EvenOddFilter.Category.ODD);

        final TransformerComponentBuilder<MockTransformer> transformer1 =
                jobBuilder.addTransformer(MockTransformer.class).addInputColumn(sourceColumn);
        transformer1.setRequirement(req1);
        final MutableInputColumn<?> outputColumn1 = transformer1.getOutputColumns().get(0);
        outputColumn1.setName("outputColumn1");

        final TransformerComponentBuilder<MockTransformer> transformer2 =
                jobBuilder.addTransformer(MockTransformer.class).addInputColumn(sourceColumn);
        transformer2.setRequirement(req2);
        final MutableInputColumn<?> outputColumn2 = transformer2.getOutputColumns().get(0);
        outputColumn2.setName("outputColumn2");

        final AnalyzerComponentBuilder<MockAnalyzer> analyzer = jobBuilder.addAnalyzer(MockAnalyzer.class);
        // add outputcolumn 1+2 - they have opposite requirements
        analyzer.addInputColumns(sourceColumn, outputColumn1, outputColumn2);

        final AnalysisJob job = jobBuilder.toAnalysisJob();

        final AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(configuration).run(job);

        resultFuture.await();

        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        @SuppressWarnings("unchecked") final ListResult<InputRow> listResult =
                (ListResult<InputRow>) resultFuture.getResults().get(0);
        final List<InputRow> list = listResult.getValues();

        assertFalse("List is empty - this indicates that no records passed through the 'multiple requirements' rule",
                list.isEmpty());
        assertEquals("[foo, null, mocked: foo]",
                list.get(0).getValues(sourceColumn, outputColumn1, outputColumn2).toString());
        assertEquals("[bar, mocked: bar, null]",
                list.get(1).getValues(sourceColumn, outputColumn1, outputColumn2).toString());
        assertEquals("[baz, null, mocked: baz]",
                list.get(2).getValues(sourceColumn, outputColumn1, outputColumn2).toString());
        assertEquals("[hello, mocked: hello, null]",
                list.get(3).getValues(sourceColumn, outputColumn1, outputColumn2).toString());
        assertEquals("[world, null, mocked: world]",
                list.get(4).getValues(sourceColumn, outputColumn1, outputColumn2).toString());
        assertEquals(5, list.size());
    }

    /**
     * Tests that if a single transformations that has a requirements feed into
     * the another component (e.g. an Analyzer), then that component will
     * respect the transformation's requirement.
     */
    public void testDontMergeFilterRequirementWhenAnalyzerConsumesInputColumnsWithSingleRequirement() throws Throwable {
        jobBuilder.addSourceColumns("col1");
        final InputColumn<?> sourceColumn = jobBuilder.getSourceColumnByName("col1");
        final FilterComponentBuilder<EvenOddFilter, EvenOddFilter.Category> filter =
                jobBuilder.addFilter(EvenOddFilter.class).addInputColumn(sourceColumn);

        final FilterOutcome req1 = filter.getFilterOutcome(EvenOddFilter.Category.EVEN);
        final FilterOutcome req2 = filter.getFilterOutcome(EvenOddFilter.Category.ODD);

        final TransformerComponentBuilder<MockTransformer> transformer1 =
                jobBuilder.addTransformer(MockTransformer.class).addInputColumn(sourceColumn);
        transformer1.setRequirement(req1);
        final MutableInputColumn<?> outputColumn1 = transformer1.getOutputColumns().get(0);
        outputColumn1.setName("outputColumn1");

        final TransformerComponentBuilder<MockTransformer> transformer2 =
                jobBuilder.addTransformer(MockTransformer.class).addInputColumn(sourceColumn);
        transformer2.setRequirement(req2);
        final MutableInputColumn<?> outputColumn2 = transformer2.getOutputColumns().get(0);
        outputColumn2.setName("outputColumn2");

        final AnalyzerComponentBuilder<MockAnalyzer> analyzer = jobBuilder.addAnalyzer(MockAnalyzer.class);

        // add only outputcolumn 1 - it has a single requirement
        analyzer.addInputColumns(sourceColumn, outputColumn1);

        final AnalysisJob job = jobBuilder.toAnalysisJob();

        final AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(configuration).run(job);

        resultFuture.await();

        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        @SuppressWarnings("unchecked") final ListResult<InputRow> listResult =
                (ListResult<InputRow>) resultFuture.getResults().get(0);
        final List<InputRow> list = listResult.getValues();

        assertFalse("List is empty - this indicates that no records passed through the 'single requirements' rule",
                list.isEmpty());
        assertEquals("[bar, mocked: bar, null]",
                list.get(0).getValues(sourceColumn, outputColumn1, outputColumn2).toString());
        assertEquals("[hello, mocked: hello, null]",
                list.get(1).getValues(sourceColumn, outputColumn1, outputColumn2).toString());
        assertEquals(2, list.size());
    }

    /**
     * Tests the use of the '_any_' requirement. This should override the
     * transitive requirement behaviour
     */
    public void testConsumeRecordsWhenAnyOutcomeRequirementIsSet() throws Throwable {
        jobBuilder.addSourceColumns("col1");
        final InputColumn<?> sourceColumn = jobBuilder.getSourceColumnByName("col1");
        final FilterComponentBuilder<EvenOddFilter, EvenOddFilter.Category> filter =
                jobBuilder.addFilter(EvenOddFilter.class).addInputColumn(sourceColumn);

        final FilterOutcome req1 = filter.getFilterOutcome(EvenOddFilter.Category.EVEN);
        final FilterOutcome req2 = filter.getFilterOutcome(EvenOddFilter.Category.ODD);

        final TransformerComponentBuilder<MockTransformer> transformer1 =
                jobBuilder.addTransformer(MockTransformer.class).addInputColumn(sourceColumn);
        transformer1.setRequirement(req1);
        final MutableInputColumn<?> outputColumn1 = transformer1.getOutputColumns().get(0);
        outputColumn1.setName("outputColumn1");

        final TransformerComponentBuilder<MockTransformer> transformer2 =
                jobBuilder.addTransformer(MockTransformer.class).addInputColumn(sourceColumn);
        transformer2.setRequirement(req2);
        final MutableInputColumn<?> outputColumn2 = transformer2.getOutputColumns().get(0);
        outputColumn2.setName("outputColumn2");

        final AnalyzerComponentBuilder<MockAnalyzer> analyzer = jobBuilder.addAnalyzer(MockAnalyzer.class);

        // add only outputcolumn 1 - it has a single requirement
        analyzer.addInputColumns(sourceColumn, outputColumn1);
        analyzer.setComponentRequirement(AnyComponentRequirement.get());

        final AnalysisJob job = jobBuilder.toAnalysisJob();

        final AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(configuration).run(job);

        resultFuture.await();

        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        @SuppressWarnings("unchecked") final ListResult<InputRow> listResult =
                (ListResult<InputRow>) resultFuture.getResults().get(0);
        final List<InputRow> list = listResult.getValues();

        assertFalse("List is empty - this indicates that no records passed through the 'any requirements' rule",
                list.isEmpty());

        assertEquals("[foo, null, mocked: foo]",
                list.get(0).getValues(sourceColumn, outputColumn1, outputColumn2).toString());
        assertEquals("[bar, mocked: bar, null]",
                list.get(1).getValues(sourceColumn, outputColumn1, outputColumn2).toString());
        assertEquals("[baz, null, mocked: baz]",
                list.get(2).getValues(sourceColumn, outputColumn1, outputColumn2).toString());
        assertEquals("[hello, mocked: hello, null]",
                list.get(3).getValues(sourceColumn, outputColumn1, outputColumn2).toString());
        assertEquals("[world, null, mocked: world]",
                list.get(4).getValues(sourceColumn, outputColumn1, outputColumn2).toString());

        assertEquals(5, list.size());
    }

    /**
     * Tests the use of the '_any_' requirement. This should override the
     * transitive requirement behaviour
     */
    public void testConsumeRecordsWhenCompoundOutcomeRequirementIsSet() throws Throwable {
        jobBuilder.addSourceColumns("col1");
        final InputColumn<?> sourceColumn = jobBuilder.getSourceColumnByName("col1");
        final FilterComponentBuilder<EvenOddFilter, EvenOddFilter.Category> filter =
                jobBuilder.addFilter(EvenOddFilter.class).addInputColumn(sourceColumn);

        final FilterOutcome req1 = filter.getFilterOutcome(EvenOddFilter.Category.EVEN);
        final FilterOutcome req2 = filter.getFilterOutcome(EvenOddFilter.Category.ODD);

        final TransformerComponentBuilder<MockTransformer> transformer1 =
                jobBuilder.addTransformer(MockTransformer.class).addInputColumn(sourceColumn);
        transformer1.setRequirement(req1);
        final MutableInputColumn<?> outputColumn1 = transformer1.getOutputColumns().get(0);
        outputColumn1.setName("outputColumn1");

        final TransformerComponentBuilder<MockTransformer> transformer2 =
                jobBuilder.addTransformer(MockTransformer.class).addInputColumn(sourceColumn);
        transformer2.setRequirement(req2);
        final MutableInputColumn<?> outputColumn2 = transformer2.getOutputColumns().get(0);
        outputColumn2.setName("outputColumn2");

        final AnalyzerComponentBuilder<MockAnalyzer> analyzer = jobBuilder.addAnalyzer(MockAnalyzer.class);

        // add only outputcolumn 1 - it has a single requirement
        analyzer.addInputColumns(sourceColumn, outputColumn1);
        analyzer.setComponentRequirement(new CompoundComponentRequirement(req1, req2));

        final AnalysisJob job = jobBuilder.toAnalysisJob();

        final AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(configuration).run(job);

        resultFuture.await();

        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        @SuppressWarnings("unchecked") final ListResult<InputRow> listResult =
                (ListResult<InputRow>) resultFuture.getResults().get(0);
        final List<InputRow> list = listResult.getValues();

        assertFalse("List is empty - this indicates that no records passed through the 'any requirements' rule",
                list.isEmpty());

        assertEquals("[foo, null, mocked: foo]",
                list.get(0).getValues(sourceColumn, outputColumn1, outputColumn2).toString());
        assertEquals("[bar, mocked: bar, null]",
                list.get(1).getValues(sourceColumn, outputColumn1, outputColumn2).toString());
        assertEquals("[baz, null, mocked: baz]",
                list.get(2).getValues(sourceColumn, outputColumn1, outputColumn2).toString());
        assertEquals("[hello, mocked: hello, null]",
                list.get(3).getValues(sourceColumn, outputColumn1, outputColumn2).toString());
        assertEquals("[world, null, mocked: world]",
                list.get(4).getValues(sourceColumn, outputColumn1, outputColumn2).toString());

        assertEquals(5, list.size());
    }
}
