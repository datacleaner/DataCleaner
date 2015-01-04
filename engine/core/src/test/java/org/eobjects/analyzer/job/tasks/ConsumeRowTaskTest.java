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
package org.eobjects.analyzer.job.tasks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.convert.ConvertToNumberTransformer;
import org.eobjects.analyzer.beans.filter.MaxRowsFilter;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.result.ListResult;
import org.eobjects.analyzer.test.MockAnalyzer;

public class ConsumeRowTaskTest extends TestCase {

    @SuppressWarnings("unchecked")
    public void testMultiRowTransformer() throws Throwable {
        AnalyzerBeansConfiguration configuration = new AnalyzerBeansConfigurationImpl();

        final InputColumn<?> countingColumn;
        final AnalysisJob job;

        // build example job
        try (AnalysisJobBuilder builder = new AnalysisJobBuilder(configuration)) {

            // number_col,string_col
            // 3,foo
            // 10,bar
            // 0,baz

            builder.setDatastore(new CsvDatastore("foo", "src/test/resources/multi_row_transformer_test.csv"));
            builder.addSourceColumns("number_col");

            TransformerJobBuilder<ConvertToNumberTransformer> convertTransformer = builder.addTransformer(
                    ConvertToNumberTransformer.class).addInputColumn(builder.getSourceColumnByName("number_col"));
            MutableInputColumn<?> numberColumn = convertTransformer.getOutputColumns().get(0);

            TransformerJobBuilder<MockMultiRowTransformer> multiRowTransformer = builder.addTransformer(
                    MockMultiRowTransformer.class).addInputColumn(numberColumn);

            List<MutableInputColumn<?>> mockTransformerColumns = multiRowTransformer.getOutputColumns();
            countingColumn = mockTransformerColumns.get(0);
            assertEquals("Mock multi row transformer (1)", countingColumn.getName());
            builder.addAnalyzer(MockAnalyzer.class).addInputColumns(mockTransformerColumns);

            job = builder.toAnalysisJob();
        }

        ListResult<InputRow> result;

        // run job
        {
            AnalysisRunner runner = new AnalysisRunnerImpl(configuration);
            AnalysisResultFuture resultFuture = runner.run(job);
            if (resultFuture.isErrornous()) {
                throw resultFuture.getErrors().get(0);
            }
            result = (ListResult<InputRow>) resultFuture.getResults().get(0);
        }

        List<InputRow> list = result.getValues();

        // we expect 13 rows (3 + 10 + 0)
        assertEquals(13, list.size());

        assertEquals(1, list.get(0).getValue(countingColumn));
        assertEquals(2, list.get(1).getValue(countingColumn));
        assertEquals(3, list.get(2).getValue(countingColumn));
        assertEquals(1, list.get(3).getValue(countingColumn));
        assertEquals(2, list.get(4).getValue(countingColumn));
        assertEquals(3, list.get(5).getValue(countingColumn));
        assertEquals(4, list.get(6).getValue(countingColumn));
        assertEquals(5, list.get(7).getValue(countingColumn));
        assertEquals(6, list.get(8).getValue(countingColumn));
        assertEquals(7, list.get(9).getValue(countingColumn));
        assertEquals(8, list.get(10).getValue(countingColumn));
        assertEquals(9, list.get(11).getValue(countingColumn));
        assertEquals(10, list.get(12).getValue(countingColumn));

        // assert that all generated rows have unique ids
        Set<Integer> ids = new HashSet<Integer>();
        for (InputRow row : list) {
            int id = row.getId();
            if (ids.contains(id)) {
                fail("Multiple rows with id " + id);
            }
            ids.add(id);
        }
    }

    @SuppressWarnings("unchecked")
    public void testConsumeRowTaskForComplexJob() throws Throwable {
        AnalyzerBeansConfiguration configuration = new AnalyzerBeansConfigurationImpl();
        final AnalysisJob job;
        // build example job
        try (AnalysisJobBuilder builder = new AnalysisJobBuilder(configuration)) {
            builder.setDatastore(new CsvDatastore("Names", "src/test/resources/example-name-lengths.csv"));
            builder.addSourceColumns("name");
            FilterJobBuilder<MaxRowsFilter, MaxRowsFilter.Category> filterJobBuilder = builder
                    .addFilter(MaxRowsFilter.class);
            filterJobBuilder.setConfiguredProperty("Max rows", 10);

            TransformerJobBuilder<ConvertToNumberTransformer> convertTransformer = builder.addTransformer(
                    ConvertToNumberTransformer.class).addInputColumn(builder.getSourceColumnByName("name"));
            MutableInputColumn<?> numberColumn = convertTransformer.getOutputColumns().get(0);

            convertTransformer.setRequirement(filterJobBuilder, MaxRowsFilter.Category.VALID);
            builder.addAnalyzer(MockAnalyzer.class).addInputColumns(numberColumn);
            job = builder.toAnalysisJob();
        }

        ListResult<InputRow> result;

        // run job
        {
            AnalysisRunner runner = new AnalysisRunnerImpl(configuration);
            AnalysisResultFuture resultFuture = runner.run(job);
            if (resultFuture.isErrornous()) {
                throw resultFuture.getErrors().get(0);
            }
            result = (ListResult<InputRow>) resultFuture.getResults().get(0);
        }

        List<InputRow> list = result.getValues();

        assertEquals(10, list.size());

        // assertEquals(1, list.get(0).getValue(countingColumn));
    }

}
